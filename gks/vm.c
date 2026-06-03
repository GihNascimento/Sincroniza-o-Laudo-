#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include <ctype.h>
#include <errno.h>
#include <limits.h>

#define TLB_SIZE      16
#define PAGE_SIZE     256
#define NUM_PAGES     256
#define NUM_FRAMES    128
#define BACKING_STORE "BACKING_STORE.bin"

#define ALG_FIFO 0
#define ALG_LRU  1

typedef struct {
    int       page;
    int       frame;
    int       valid;
    long long inserted;
    long long accessed;
} TLBEntry;

typedef struct {
    int frame;
    int valid;
} PageEntry;

typedef struct {
    int idx;
    int query;
} TLBThreadArg;

static TLBEntry    tlb[TLB_SIZE];
static PageEntry   pt[NUM_PAGES];
static signed char pmem[NUM_FRAMES][PAGE_SIZE];
static int         frame_page[NUM_FRAMES];
static long long   frame_loaded[NUM_FRAMES];
static long long   frame_used[NUM_FRAMES];

static int       n_frames    = 0;
static int       fifo_fptr   = 0;
static int       tlb_count   = 0;
static long long gclk        = 0;
static int       page_algo;
static int       tlb_algo;

static int             n_translated = 0;
static int             n_faults     = 0;
static int             n_tlb_hits   = 0;

/* -1 = miss, >= 0 = frame encontrado pelas threads */
static int             tlb_result;
static pthread_mutex_t tlb_mutex;

static void *tlb_search_fn(void *arg)
{
    TLBThreadArg *a = (TLBThreadArg *)arg;
    int i = a->idx;

    if (tlb[i].valid && tlb[i].page == a->query) {
        int rc = pthread_mutex_lock(&tlb_mutex);
        if (rc != 0) { fprintf(stderr, "pthread_mutex_lock: %s\n", strerror(rc)); return NULL; }
        if (tlb_result < 0)
            tlb_result = tlb[i].frame;
        pthread_mutex_unlock(&tlb_mutex);
    }
    return NULL;
}

static int tlb_search(int page)
{
    pthread_t    tids[TLB_SIZE];
    TLBThreadArg args[TLB_SIZE];
    int i, rc;

    tlb_result = -1;

    for (i = 0; i < TLB_SIZE; i++) {
        args[i].idx   = i;
        args[i].query = page;
        rc = pthread_create(&tids[i], NULL, tlb_search_fn, &args[i]);
        if (rc != 0) {
            fprintf(stderr, "pthread_create: %s\n", strerror(rc));
            for (int j = 0; j < i; j++) pthread_join(tids[j], NULL);
            exit(EXIT_FAILURE);
        }
    }

    for (i = 0; i < TLB_SIZE; i++) {
        rc = pthread_join(tids[i], NULL);
        if (rc != 0) { fprintf(stderr, "pthread_join: %s\n", strerror(rc)); exit(EXIT_FAILURE); }
    }

    return tlb_result;
}

static void tlb_invalidate(int page)
{
    for (int i = 0; i < TLB_SIZE; i++) {
        if (tlb[i].valid && tlb[i].page == page) {
            tlb[i].valid = 0;
            if (tlb_count > 0) tlb_count--;
        }
    }
}

static void tlb_touch(int page)
{
    for (int i = 0; i < TLB_SIZE; i++) {
        if (tlb[i].valid && tlb[i].page == page) {
            tlb[i].accessed = ++gclk;
            return;
        }
    }
}

static void tlb_insert(int page, int frame)
{
    int target;

    for (int i = 0; i < TLB_SIZE; i++) {
        if (tlb[i].valid && tlb[i].page == page) {
            tlb[i].frame    = frame;
            tlb[i].accessed = ++gclk;
            return;
        }
    }

    if (tlb_count < TLB_SIZE) {
        if (tlb_algo == ALG_FIFO) {
            /* slot inválido com menor inserted mantém a ordem FIFO após invalidações */
            long long mn = LLONG_MAX;
            target = -1;
            for (int i = 0; i < TLB_SIZE; i++) {
                if (!tlb[i].valid && tlb[i].inserted < mn) {
                    mn = tlb[i].inserted;
                    target = i;
                }
            }
        } else {
            target = 0;
            for (int i = 0; i < TLB_SIZE; i++) {
                if (!tlb[i].valid) { target = i; break; }
            }
        }
        tlb_count++;
    } else {
        if (tlb_algo == ALG_FIFO) {
            long long mn = tlb[0].inserted;
            target = 0;
            for (int i = 1; i < TLB_SIZE; i++) {
                if (tlb[i].inserted < mn) { mn = tlb[i].inserted; target = i; }
            }
        } else {
            long long mn = tlb[0].accessed;
            target = 0;
            for (int i = 1; i < TLB_SIZE; i++) {
                if (tlb[i].accessed < mn) { mn = tlb[i].accessed; target = i; }
            }
        }
    }

    tlb[target].page     = page;
    tlb[target].frame    = frame;
    tlb[target].valid    = 1;
    tlb[target].inserted = ++gclk;
    tlb[target].accessed = ++gclk;
}

static int alloc_frame(void)
{
    if (n_frames < NUM_FRAMES)
        return n_frames++;

    int victim;

    if (page_algo == ALG_FIFO) {
        victim    = fifo_fptr;
        fifo_fptr = (fifo_fptr + 1) % NUM_FRAMES;
    } else {
        long long mn = frame_used[0];
        victim = 0;
        for (int i = 1; i < NUM_FRAMES; i++) {
            if (frame_used[i] < mn) { mn = frame_used[i]; victim = i; }
        }
    }

    int old_page = frame_page[victim];
    if (old_page >= 0 && old_page < NUM_PAGES) {
        pt[old_page].valid = 0;
        tlb_invalidate(old_page);
    }

    return victim;
}

static int translate(int laddr, FILE *bs, int *paddr_out, signed char *val_out)
{
    int page   = (laddr >> 8) & 0xFF;
    int offset =  laddr       & 0xFF;
    int frame  = -1;
    int hit    =  0;

    ++gclk;

    frame = tlb_search(page);

    if (frame >= 0) {
        hit = 1;
        n_tlb_hits++;
        if (tlb_algo == ALG_LRU) tlb_touch(page);
        frame_used[frame] = ++gclk;
    } else {
        if (pt[page].valid) {
            frame = pt[page].frame;
            frame_used[frame] = ++gclk;
        } else {
            n_faults++;
            frame = alloc_frame();

            if (fseek(bs, (long)page * PAGE_SIZE, SEEK_SET) != 0) {
                fprintf(stderr, "fseek falhou para página %d: %s\n", page, strerror(errno));
                exit(EXIT_FAILURE);
            }
            if ((int)fread(pmem[frame], 1, PAGE_SIZE, bs) != PAGE_SIZE) {
                fprintf(stderr, "fread falhou para página %d\n", page);
                exit(EXIT_FAILURE);
            }

            frame_page[frame]   = page;
            frame_loaded[frame] = ++gclk;
            frame_used[frame]   = ++gclk;

            pt[page].frame = frame;
            pt[page].valid = 1;
        }

        tlb_insert(page, frame);
    }

    *paddr_out = (frame << 8) | offset;
    *val_out   = pmem[frame][offset];

    return hit;
}

static int parse_algo(const char *s)
{
    char lo[8];
    int  i;
    for (i = 0; s[i] && i < 7; i++)
        lo[i] = (char)tolower((unsigned char)s[i]);
    lo[i] = '\0';
    if (strcmp(lo, "fifo") == 0) return ALG_FIFO;
    if (strcmp(lo, "lru")  == 0) return ALG_LRU;
    return -1;
}

static int parse_line(const char *line, int *out)
{
    const char *p = line;
    char       *end;
    long        v;

    while (isspace((unsigned char)*p)) p++;
    if (*p == '\0') return 0;

    errno = 0;
    v     = strtol(p, &end, 10);

    while (isspace((unsigned char)*end)) end++;
    if (*end != '\0') return 0;
    if (errno == ERANGE) return 0;
    if (v < 0 || v > 65535) return 0;

    *out = (int)v;
    return 1;
}

int main(int argc, char *argv[])
{
    if (argc != 4) {
        fprintf(stderr,
            "Uso: %s <arquivo_enderecos> <algo_pagina> <algo_tlb>\n"
            "  algo_pagina: fifo | lru\n"
            "  algo_tlb:    fifo | lru\n",
            argv[0]);
        return EXIT_FAILURE;
    }

    page_algo = parse_algo(argv[2]);
    if (page_algo < 0) {
        fprintf(stderr, "Erro: algoritmo de páginas inválido '%s'. Use 'fifo' ou 'lru'.\n", argv[2]);
        return EXIT_FAILURE;
    }

    tlb_algo = parse_algo(argv[3]);
    if (tlb_algo < 0) {
        fprintf(stderr, "Erro: algoritmo de TLB inválido '%s'. Use 'fifo' ou 'lru'.\n", argv[3]);
        return EXIT_FAILURE;
    }

    FILE *afile = fopen(argv[1], "r");
    if (!afile) {
        fprintf(stderr, "Erro: não foi possível abrir '%s': %s\n", argv[1], strerror(errno));
        return EXIT_FAILURE;
    }

    FILE *bs = fopen(BACKING_STORE, "rb");
    if (!bs) {
        fprintf(stderr, "Erro: não foi possível abrir '" BACKING_STORE "': %s\n", strerror(errno));
        fclose(afile);
        return EXIT_FAILURE;
    }

    int rc = pthread_mutex_init(&tlb_mutex, NULL);
    if (rc != 0) {
        fprintf(stderr, "pthread_mutex_init: %s\n", strerror(rc));
        fclose(afile); fclose(bs);
        return EXIT_FAILURE;
    }

    memset(tlb,  0, sizeof(tlb));
    memset(pt,   0, sizeof(pt));
    memset(pmem, 0, sizeof(pmem));
    for (int i = 0; i < NUM_FRAMES; i++) frame_page[i] = -1;
    memset(frame_loaded, 0, sizeof(frame_loaded));
    memset(frame_used,   0, sizeof(frame_used));

    char        line[512];
    int         laddr, paddr;
    signed char val;
    int         tlb_hit;

    while (fgets(line, sizeof(line), afile)) {
        if (!parse_line(line, &laddr)) continue;
        n_translated++;
        tlb_hit = translate(laddr, bs, &paddr, &val);
        printf("Virtual address: %d TLB: %d Physical address: %d Value: %d\n",
               laddr, tlb_hit, paddr, (int)val);
    }

    printf("\nNumber of Translated Addresses = %d\n", n_translated);
    printf("Page Faults = %d\n", n_faults);

    if (n_translated > 0) {
        printf("Page Fault Rate = %.3f\n", (double)n_faults / n_translated);
        printf("TLB Hits = %d\n", n_tlb_hits);
        printf("TLB Hit Rate = %.3f\n", (double)n_tlb_hits / n_translated);
    } else {
        printf("Page Fault Rate = 0.000\n");
        printf("TLB Hits = 0\n");
        printf("TLB Hit Rate = 0.000\n");
    }

    fclose(afile);
    fclose(bs);
    pthread_mutex_destroy(&tlb_mutex);

    return EXIT_SUCCESS;
}

/*
 * vm.c — Virtual Memory Manager
 *
 * Silberschatz, "Operating System Concepts", 10th ed.
 * Programming Project: Designing a Virtual Memory Manager
 *
 * Adaptações do professor:
 *   - Memória física: 128 frames × 256 bytes
 *   - Substituição de páginas: FIFO ou LRU
 *   - Substituição de TLB:    FIFO ou LRU
 *   - Busca na TLB via uma thread por entrada (pthreads)
 *
 * Uso: ./vm <arquivo_enderecos> <algo_pagina> <algo_tlb>
 *      <algo_pagina> e <algo_tlb>: fifo | lru
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include <ctype.h>
#include <errno.h>
#include <limits.h>

/* ================================================================
   CONSTANTES
   ================================================================ */

#define TLB_SIZE      16
#define PAGE_SIZE     256
#define NUM_PAGES     256
#define NUM_FRAMES    128
#define BACKING_STORE "BACKING_STORE.bin"

#define ALG_FIFO 0
#define ALG_LRU  1

/* ================================================================
   ESTRUTURAS DE DADOS
   ================================================================ */

typedef struct {
    int       page;
    int       frame;
    int       valid;
    long long inserted;   /* clock na inserção  — usado pelo FIFO */
    long long accessed;   /* clock no último acesso — usado pelo LRU */
} TLBEntry;

typedef struct {
    int frame;
    int valid;
} PageEntry;

/* Argumento passado para cada thread de busca na TLB */
typedef struct {
    int idx;        /* índice da entrada de TLB responsabilidade desta thread */
    int query;      /* número de página sendo buscado                         */
} TLBThreadArg;

/* ================================================================
   VARIÁVEIS GLOBAIS
   ================================================================ */

static TLBEntry    tlb[TLB_SIZE];
static PageEntry   pt[NUM_PAGES];
static signed char pmem[NUM_FRAMES][PAGE_SIZE];  /* memória física         */
static int         frame_page[NUM_FRAMES];       /* frame → página mapeada */
static long long   frame_loaded[NUM_FRAMES];     /* clock de carregamento  */
static long long   frame_used[NUM_FRAMES];       /* clock do último acesso */

static int  n_frames  = 0;    /* frames alocados até agora                 */
static int  fifo_fptr = 0;    /* ponteiro FIFO para substituição de frames  */
static int  tlb_count = 0;    /* entradas válidas na TLB                    */
/* tlb_fptr removido: FIFO da TLB usa timestamps (correto com invalidações) */
static long long gclk = 0;    /* relógio lógico global                      */

static int page_algo;         /* ALG_FIFO ou ALG_LRU para páginas           */
static int tlb_algo;          /* ALG_FIFO ou ALG_LRU para TLB               */

/* Estatísticas */
static int n_translated = 0;
static int n_faults     = 0;
static int n_tlb_hits   = 0;

/* Sincronização das threads de TLB */
static int             tlb_result;  /* -1 = miss, >= 0 = número do frame   */
static pthread_mutex_t tlb_mutex;   /* protege tlb_result                   */

/* ================================================================
   FUNÇÃO DAS THREADS DE BUSCA NA TLB
   ================================================================ */

/*
 * Cada thread verifica exatamente uma entrada da TLB.
 * Se a entrada for válida e a página coincidir, registra o frame em
 * tlb_result (protegido por mutex para evitar condição de corrida).
 */
static void *tlb_search_fn(void *arg)
{
    TLBThreadArg *a = (TLBThreadArg *)arg;
    int i = a->idx;

    if (tlb[i].valid && tlb[i].page == a->query) {
        int rc = pthread_mutex_lock(&tlb_mutex);
        if (rc != 0) {
            fprintf(stderr, "pthread_mutex_lock: %s\n", strerror(rc));
            return NULL;
        }
        if (tlb_result < 0)
            tlb_result = tlb[i].frame;
        pthread_mutex_unlock(&tlb_mutex);
    }
    return NULL;
}

/* ================================================================
   OPERAÇÕES DA TLB
   ================================================================ */

/*
 * Busca uma página na TLB criando uma thread por entrada.
 * Retorna o número do frame se houver hit, -1 caso contrário.
 */
static int tlb_search(int page)
{
    pthread_t    tids[TLB_SIZE];
    TLBThreadArg args[TLB_SIZE];
    int i, rc;

    tlb_result = -1;

    /* Cria TLB_SIZE threads — uma por entrada */
    for (i = 0; i < TLB_SIZE; i++) {
        args[i].idx   = i;
        args[i].query = page;
        rc = pthread_create(&tids[i], NULL, tlb_search_fn, &args[i]);
        if (rc != 0) {
            fprintf(stderr, "pthread_create: %s\n", strerror(rc));
            for (int j = 0; j < i; j++)
                pthread_join(tids[j], NULL);
            exit(EXIT_FAILURE);
        }
    }

    /* Aguarda todas as threads terminarem */
    for (i = 0; i < TLB_SIZE; i++) {
        rc = pthread_join(tids[i], NULL);
        if (rc != 0) {
            fprintf(stderr, "pthread_join: %s\n", strerror(rc));
            exit(EXIT_FAILURE);
        }
    }

    return tlb_result;
}

/* Invalida entrada de TLB para uma página (chamado ao evictar frame) */
static void tlb_invalidate(int page)
{
    for (int i = 0; i < TLB_SIZE; i++) {
        if (tlb[i].valid && tlb[i].page == page) {
            tlb[i].valid = 0;
            if (tlb_count > 0) tlb_count--;
        }
    }
}

/* Atualiza o timestamp LRU de uma página já presente na TLB */
static void tlb_touch(int page)
{
    for (int i = 0; i < TLB_SIZE; i++) {
        if (tlb[i].valid && tlb[i].page == page) {
            tlb[i].accessed = ++gclk;
            return;
        }
    }
}

/* Insere (page → frame) na TLB, evictando se necessário */
static void tlb_insert(int page, int frame)
{
    int target;

    /* Já presente? Atualiza frame e timestamps */
    for (int i = 0; i < TLB_SIZE; i++) {
        if (tlb[i].valid && tlb[i].page == page) {
            tlb[i].frame    = frame;
            tlb[i].accessed = ++gclk;
            return;
        }
    }

    if (tlb_count < TLB_SIZE) {
        /*
         * Há slot inválido disponível.
         * Para FIFO: usamos o slot inválido com menor 'inserted'
         *   (ou 0 se nunca foi usado) para não quebrar a ordem de evicção.
         * Para LRU: qualquer slot inválido serve.
         */
        if (tlb_algo == ALG_FIFO) {
            /* Escolhe o inválido com menor inserted (mais antigo ou nunca usado) */
            long long mn = LLONG_MAX;
            target = -1;
            for (int i = 0; i < TLB_SIZE; i++) {
                if (!tlb[i].valid && tlb[i].inserted < mn) {
                    mn = tlb[i].inserted;
                    target = i;
                }
            }
        } else {
            /* LRU: primeiro slot inválido */
            target = 0;
            for (int i = 0; i < TLB_SIZE; i++) {
                if (!tlb[i].valid) { target = i; break; }
            }
        }
        tlb_count++;
    } else {
        /* Evicção necessária */
        if (tlb_algo == ALG_FIFO) {
            /* FIFO baseado em timestamp de inserção (correto mesmo após invalidações) */
            long long mn = tlb[0].inserted;
            target = 0;
            for (int i = 1; i < TLB_SIZE; i++) {
                if (tlb[i].inserted < mn) {
                    mn     = tlb[i].inserted;
                    target = i;
                }
            }
        } else {
            /* LRU: entrada com menor timestamp de acesso */
            long long mn = tlb[0].accessed;
            target = 0;
            for (int i = 1; i < TLB_SIZE; i++) {
                if (tlb[i].accessed < mn) {
                    mn     = tlb[i].accessed;
                    target = i;
                }
            }
        }
    }

    tlb[target].page     = page;
    tlb[target].frame    = frame;
    tlb[target].valid    = 1;
    tlb[target].inserted = ++gclk;
    tlb[target].accessed = ++gclk;
}

/* ================================================================
   ALOCAÇÃO DE FRAMES / SUBSTITUIÇÃO DE PÁGINAS
   ================================================================ */

/*
 * Retorna um frame disponível para carregar uma nova página.
 * Se todos os 128 frames estiverem ocupados, evicta o mais antigo
 * segundo o algoritmo de substituição escolhido.
 */
static int alloc_frame(void)
{
    if (n_frames < NUM_FRAMES)
        return n_frames++;   /* preenche frames 0 → 127 em ordem */

    int victim;

    if (page_algo == ALG_FIFO) {
        victim   = fifo_fptr;
        fifo_fptr = (fifo_fptr + 1) % NUM_FRAMES;
    } else {
        /* LRU: frame com menor timestamp de último acesso */
        long long mn = frame_used[0];
        victim = 0;
        for (int i = 1; i < NUM_FRAMES; i++) {
            if (frame_used[i] < mn) {
                mn     = frame_used[i];
                victim = i;
            }
        }
    }

    /* Invalida mapeamento antigo */
    int old_page = frame_page[victim];
    if (old_page >= 0 && old_page < NUM_PAGES) {
        pt[old_page].valid = 0;
        tlb_invalidate(old_page);
    }

    return victim;
}

/* ================================================================
   TRADUÇÃO DE ENDEREÇOS
   ================================================================ */

/*
 * Traduz um endereço lógico para físico seguindo o fluxo:
 *   1. Consulta TLB  (via threads)
 *   2. Consulta Page Table
 *   3. Page Fault → lê BACKING_STORE
 *   4. Atualiza memória física, Page Table e TLB
 *   5. Calcula endereço físico e obtém valor
 *
 * Retorna 1 se TLB hit, 0 se TLB miss.
 */
static int translate(int laddr, FILE *bs, int *paddr_out, signed char *val_out)
{
    int page   = (laddr >> 8) & 0xFF;
    int offset =  laddr       & 0xFF;
    int frame  = -1;
    int hit    =  0;

    ++gclk;

    /* ---- 1. Consulta TLB ---- */
    frame = tlb_search(page);

    if (frame >= 0) {
        /* TLB Hit */
        hit = 1;
        n_tlb_hits++;

        if (tlb_algo == ALG_LRU)
            tlb_touch(page);

        frame_used[frame] = ++gclk;

    } else {
        /* TLB Miss — ---- 2. Consulta Page Table ---- */
        if (pt[page].valid) {
            frame = pt[page].frame;
            frame_used[frame] = ++gclk;

        } else {
            /* ---- 3. Page Fault ---- */
            n_faults++;

            frame = alloc_frame();

            /* ---- 4. Lê página do BACKING_STORE ---- */
            if (fseek(bs, (long)page * PAGE_SIZE, SEEK_SET) != 0) {
                fprintf(stderr, "fseek falhou para página %d: %s\n",
                        page, strerror(errno));
                exit(EXIT_FAILURE);
            }
            if ((int)fread(pmem[frame], 1, PAGE_SIZE, bs) != PAGE_SIZE) {
                fprintf(stderr, "fread falhou para página %d\n", page);
                exit(EXIT_FAILURE);
            }

            /* ---- 5. Atualiza memória física ---- */
            frame_page[frame]   = page;
            frame_loaded[frame] = ++gclk;
            frame_used[frame]   = ++gclk;

            /* ---- 6. Atualiza Page Table ---- */
            pt[page].frame = frame;
            pt[page].valid = 1;
        }

        /* ---- 7. Atualiza TLB ---- */
        tlb_insert(page, frame);
    }

    /* ---- 8. Endereço físico ---- */
    *paddr_out = (frame << 8) | offset;

    /* ---- 9. Valor armazenado ---- */
    *val_out = pmem[frame][offset];

    return hit;
}

/* ================================================================
   VALIDAÇÃO DE ENTRADA
   ================================================================ */

/* Retorna ALG_FIFO, ALG_LRU ou -1 se inválido (case-insensitive) */
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

/*
 * Analisa uma linha e extrai o endereço lógico.
 * Retorna  1  e preenche *out se válido.
 * Retorna  0  para linhas em branco ou inválidas (ignorar silenciosamente).
 */
static int parse_line(const char *line, int *out)
{
    const char *p = line;
    char       *end;
    long        v;

    /* Ignora espaços iniciais */
    while (isspace((unsigned char)*p)) p++;
    if (*p == '\0') return 0;   /* linha em branco */

    errno = 0;
    v     = strtol(p, &end, 10);

    /* Verifica lixo após o número */
    while (isspace((unsigned char)*end)) end++;
    if (*end != '\0') return 0;        /* caracteres inválidos */

    if (errno == ERANGE) return 0;     /* overflow              */
    if (v < 0 || v > 65535) return 0; /* fora do intervalo      */

    *out = (int)v;
    return 1;
}

/* ================================================================
   MAIN
   ================================================================ */

int main(int argc, char *argv[])
{
    /* ---- Validação de argumentos ---- */
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
        fprintf(stderr,
            "Erro: algoritmo de páginas inválido '%s'. Use 'fifo' ou 'lru'.\n",
            argv[2]);
        return EXIT_FAILURE;
    }

    tlb_algo = parse_algo(argv[3]);
    if (tlb_algo < 0) {
        fprintf(stderr,
            "Erro: algoritmo de TLB inválido '%s'. Use 'fifo' ou 'lru'.\n",
            argv[3]);
        return EXIT_FAILURE;
    }

    /* ---- Abre arquivo de endereços ---- */
    FILE *afile = fopen(argv[1], "r");
    if (!afile) {
        fprintf(stderr, "Erro: não foi possível abrir '%s': %s\n",
                argv[1], strerror(errno));
        return EXIT_FAILURE;
    }

    /* ---- Abre BACKING_STORE ---- */
    FILE *bs = fopen(BACKING_STORE, "rb");
    if (!bs) {
        fprintf(stderr,
                "Erro: não foi possível abrir '" BACKING_STORE "': %s\n",
                strerror(errno));
        fclose(afile);
        return EXIT_FAILURE;
    }

    /* ---- Inicializa mutex ---- */
    int rc = pthread_mutex_init(&tlb_mutex, NULL);
    if (rc != 0) {
        fprintf(stderr, "pthread_mutex_init: %s\n", strerror(rc));
        fclose(afile);
        fclose(bs);
        return EXIT_FAILURE;
    }

    /* ---- Inicializa estruturas ---- */
    memset(tlb,   0, sizeof(tlb));
    memset(pt,    0, sizeof(pt));
    memset(pmem,  0, sizeof(pmem));
    for (int i = 0; i < NUM_FRAMES; i++) frame_page[i] = -1;
    memset(frame_loaded, 0, sizeof(frame_loaded));
    memset(frame_used,   0, sizeof(frame_used));

    /* ---- Processa endereços ---- */
    char        line[512];
    int         laddr, paddr;
    signed char val;
    int         tlb_hit;

    while (fgets(line, sizeof(line), afile)) {
        int r = parse_line(line, &laddr);
        if (r == 0) continue;  /* linha em branco / inválida: ignora */

        n_translated++;
        tlb_hit = translate(laddr, bs, &paddr, &val);

        printf("Virtual address: %d TLB: %d Physical address: %d Value: %d\n",
               laddr, tlb_hit, paddr, (int)val);
    }

    /* ---- Estatísticas ---- */
    printf("\nNumber of Translated Addresses = %d\n", n_translated);
    printf("Page Faults = %d\n", n_faults);

    if (n_translated > 0) {
        printf("Page Fault Rate = %.3f\n",
               (double)n_faults / n_translated);
        printf("TLB Hits = %d\n", n_tlb_hits);
        printf("TLB Hit Rate = %.3f\n",
               (double)n_tlb_hits / n_translated);
    } else {
        printf("Page Fault Rate = 0.000\n");
        printf("TLB Hits = 0\n");
        printf("TLB Hit Rate = 0.000\n");
    }

    /* ---- Limpeza ---- */
    fclose(afile);
    fclose(bs);
    pthread_mutex_destroy(&tlb_mutex);

    return EXIT_SUCCESS;
}

# Virtual Memory Manager

Implementação do projeto *Designing a Virtual Memory Manager*  
(Silberschatz, *Operating System Concepts*, 10ª ed.)

---

## Objetivo

Simular um gerenciador de memória virtual que:

- Traduz endereços lógicos de 16 bits em endereços físicos
- Mantém uma TLB de 16 entradas para acelerar a tradução
- Gerencia 128 frames de memória física (256 bytes cada)
- Carrega páginas ausentes a partir do arquivo `BACKING_STORE.bin`
- Aplica algoritmos FIFO ou LRU na substituição de páginas e de entradas da TLB
- Realiza a busca na TLB usando **uma thread pthread por entrada**

---

## Arquivos

| Arquivo           | Descrição                                    |
|-------------------|----------------------------------------------|
| `vm.c`            | Implementação completa em C                  |
| `Makefile`        | Compilação, limpeza e recompilação           |
| `README.md`       | Este documento                               |
| `addresses.txt`   | Arquivo de endereços lógicos (fornecido)     |
| `BACKING_STORE.bin` | Armazenamento de páginas em disco (fornecido) |
| `correct.txt`     | Saída de referência para validação           |

---

## Compilação

```bash
make          # compila → gera o executável 'vm'
make clean    # remove o executável
make rebuild  # make clean && make all
```

Flags de compilação: `-Wall -Wextra -Wpedantic -pthread -O2`

---

## Execução

```
./vm <arquivo_enderecos> <algo_pagina> <algo_tlb>
```

Combinações válidas:

```bash
./vm addresses.txt fifo fifo
./vm addresses.txt fifo lru
./vm addresses.txt lru  fifo
./vm addresses.txt lru  lru
```

Qualquer outra combinação é rejeitada com mensagem de erro.

### Comparar com a referência

```bash
./vm addresses.txt fifo fifo > output.txt
diff output.txt correct.txt
```

---

## Formato de saída

Cada endereço gera uma linha:

```
Virtual address: X TLB: Y Physical address: Z Value: W
```

- `X` — endereço lógico original
- `Y` — `1` se TLB hit, `0` se TLB miss
- `Z` — endereço físico calculado
- `W` — valor (byte com sinal) armazenado naquele endereço

Ao final, as estatísticas:

```
Number of Translated Addresses = N
Page Faults = N
Page Fault Rate = X.XXX
TLB Hits = N
TLB Hit Rate = X.XXX
```

---

## Parâmetros de memória

| Parâmetro           | Valor |
|---------------------|-------|
| Páginas lógicas     | 256   |
| Tamanho da página   | 256 B |
| Frames físicos      | 128   |
| Tamanho do frame    | 256 B |
| Entradas na TLB     | 16    |

---

## Algoritmo FIFO

**Page Table — FIFO:**  
Os 128 frames são preenchidos na ordem 0 → 127. Quando todos estão ocupados,
o frame carregado há mais tempo (o mais "antigo") é substituído. Um ponteiro
circular avança a cada evicção: `0 → 1 → … → 127 → 0 → …`

**TLB — FIFO:**  
As 16 entradas são preenchidas na ordem 0 → 15. Quando a TLB está cheia,
a entrada inserida há mais tempo é substituída pelo mesmo mecanismo de
ponteiro circular.

---

## Algoritmo LRU

**Page Table — LRU:**  
Cada frame possui um timestamp de último acesso (`frame_used`). Quando é
necessário substituir, o frame com o menor timestamp (menos recentemente
usado) é escolhido como vítima.

**TLB — LRU:**  
Cada entrada de TLB possui um timestamp de último acesso (`accessed`). Em
substituição, a entrada com menor `accessed` é removida. O timestamp é
atualizado em todo hit na TLB.

---

## Threads — busca na TLB

A cada tradução de endereço, a busca na TLB é realizada criando
**TLB_SIZE (16) threads pthread**, uma para cada entrada:

```c
pthread_create(&tids[i], NULL, tlb_search_fn, &args[i]);
```

Cada thread verifica sua entrada com exclusividade e registra o resultado
em uma variável compartilhada protegida por `pthread_mutex_t`. Após o
`pthread_join` de todas as threads, o resultado está disponível para o
fluxo principal.

---

## Estrutura interna (vm.c)

| Componente         | Tipo / Tamanho                   | Descrição                        |
|--------------------|----------------------------------|----------------------------------|
| `tlb[]`            | `TLBEntry[16]`                   | TLB (page, frame, valid, clocks) |
| `pt[]`             | `PageEntry[256]`                 | Page Table                       |
| `pmem[][]`         | `signed char[128][256]`          | Memória física                   |
| `frame_page[]`     | `int[128]`                       | Mapeamento inverso frame→página  |
| `frame_used[]`     | `long long[128]`                 | Timestamp de acesso (LRU)        |
| `frame_loaded[]`   | `long long[128]`                 | Timestamp de carga (FIFO ref.)   |
| `gclk`             | `long long`                      | Relógio lógico global            |
| `tlb_mutex`        | `pthread_mutex_t`                | Mutex para resultado de threads  |

---

## Tratamento de erros

O programa valida e trata:

- Número incorreto de argumentos
- Algoritmo de páginas ou TLB inválido
- Arquivo de endereços inexistente ou sem permissão
- Falha ao abrir `BACKING_STORE.bin`
- Linhas em branco no arquivo de endereços (ignoradas)
- Linhas com caracteres inválidos (ignoradas)
- Números negativos (ignorados)
- Números acima de 65535 (ignorados)
- Falha em `pthread_create` / `pthread_join`
- Falha em `fseek` / `fread` no backing store
- Falha em `pthread_mutex_init`

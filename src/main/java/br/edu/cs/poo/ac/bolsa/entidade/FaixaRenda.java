package br.edu.cs.poo.ac.bolsa.entidade;

import java.math.BigDecimal;

public enum FaixaRenda {
    REGULAR(new BigDecimal("0")),
    DIFERENCIADA(new BigDecimal("5000")),
    PREMIUM(new BigDecimal("10000"));

    private BigDecimal valorInicial;

    FaixaRenda(BigDecimal valorInicial) {
        this.valorInicial = valorInicial;
    }

    public BigDecimal getValorInicial() { return valorInicial; }
}

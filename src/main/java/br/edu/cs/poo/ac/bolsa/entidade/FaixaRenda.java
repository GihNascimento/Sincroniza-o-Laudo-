package br.edu.cs.poo.ac.bolsa.entidade;

import java.math.BigDecimal;

public enum FaixaRenda {
    A(new BigDecimal("0")),
    B(new BigDecimal("2000")),
    C(new BigDecimal("5000")),
    D(new BigDecimal("10000")),
    E(new BigDecimal("20000"));

    private BigDecimal valorInicial;

    FaixaRenda(BigDecimal valorInicial) {
        this.valorInicial = valorInicial;
    }

    public BigDecimal getValorInicial() { return valorInicial; }
}

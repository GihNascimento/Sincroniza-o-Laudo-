package br.edu.cs.poo.ac.bolsa.entidade;

import br.edu.cs.poo.ac.bolsa.util.Registro;
import java.math.BigDecimal;

public class Ativo extends Registro {
    private long codigo;
    private String nome;
    private BigDecimal valorMinimoAplicacao;
    private BigDecimal valorMaximoAplicacao;
    private int prazoEmMeses;
    private BigDecimal taxaMensalMinima;
    private BigDecimal taxaMensalMaxima;
    private FaixaRenda faixaMinimaPermitida;

    public Ativo(long codigo, String nome, BigDecimal valorMinimoAplicacao,
                 BigDecimal valorMaximoAplicacao, int prazoEmMeses,
                 BigDecimal taxaMensalMinima, BigDecimal taxaMensalMaxima,
                 FaixaRenda faixaMinimaPermitida) {
        this.codigo = codigo;
        this.nome = nome;
        this.valorMinimoAplicacao = valorMinimoAplicacao;
        this.valorMaximoAplicacao = valorMaximoAplicacao;
        this.prazoEmMeses = prazoEmMeses;
        this.taxaMensalMinima = taxaMensalMinima;
        this.taxaMensalMaxima = taxaMensalMaxima;
        this.faixaMinimaPermitida = faixaMinimaPermitida;
    }

    @Override
    public String getIdentificador() {
        return String.valueOf(codigo);
    }

    public long getCodigo() { return codigo; }
    public void setCodigo(long codigo) { this.codigo = codigo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public BigDecimal getValorMinimoAplicacao() { return valorMinimoAplicacao; }
    public void setValorMinimoAplicacao(BigDecimal valorMinimoAplicacao) { this.valorMinimoAplicacao = valorMinimoAplicacao; }

    public BigDecimal getValorMaximoAplicacao() { return valorMaximoAplicacao; }
    public void setValorMaximoAplicacao(BigDecimal valorMaximoAplicacao) { this.valorMaximoAplicacao = valorMaximoAplicacao; }

    public int getPrazoEmMeses() { return prazoEmMeses; }
    public void setPrazoEmMeses(int prazoEmMeses) { this.prazoEmMeses = prazoEmMeses; }

    public BigDecimal getTaxaMensalMinima() { return taxaMensalMinima; }
    public void setTaxaMensalMinima(BigDecimal taxaMensalMinima) { this.taxaMensalMinima = taxaMensalMinima; }

    public BigDecimal getTaxaMensalMaxima() { return taxaMensalMaxima; }
    public void setTaxaMensalMaxima(BigDecimal taxaMensalMaxima) { this.taxaMensalMaxima = taxaMensalMaxima; }

    public FaixaRenda getFaixaMinimaPermitida() { return faixaMinimaPermitida; }
    public void setFaixaMinimaPermitida(FaixaRenda faixaMinimaPermitida) { this.faixaMinimaPermitida = faixaMinimaPermitida; }
}

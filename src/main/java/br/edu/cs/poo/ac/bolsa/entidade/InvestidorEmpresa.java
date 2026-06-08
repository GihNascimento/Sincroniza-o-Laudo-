package br.edu.cs.poo.ac.bolsa.entidade;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InvestidorEmpresa extends Investidor {
    private String cnpj;
    private double faturamento;
    private LocalDate dataFundacao;

    public InvestidorEmpresa() {
        super();
    }

    public InvestidorEmpresa(String nome, Endereco endereco, LocalDate dataFundacao,
                             BigDecimal bonus, Contatos contatos,
                             String cnpj, double faturamento) {
        super(nome, endereco, bonus, contatos);
        this.dataFundacao = dataFundacao;
        this.cnpj = cnpj;
        this.faturamento = faturamento;
    }

    @Override
    public String getIdentificador() {
        return cnpj;
    }

    @Override
    public BigDecimal getEntradaFinanceira() {
        return new BigDecimal(faturamento);
    }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public double getFaturamento() { return faturamento; }
    public void setFaturamento(double faturamento) { this.faturamento = faturamento; }

    public LocalDate getDataFundacao() { return dataFundacao; }
    public void setDataFundacao(LocalDate dataFundacao) { this.dataFundacao = dataFundacao; }
}

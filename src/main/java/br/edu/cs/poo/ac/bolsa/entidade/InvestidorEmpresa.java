package br.edu.cs.poo.ac.bolsa.entidade;

import java.math.BigDecimal;

public class InvestidorEmpresa extends Investidor {
    private String cnpj;
    private double faturamento;

    public InvestidorEmpresa(String cnpj, String nome, double faturamento,
                             FaixaRenda faixaRenda, Contatos contatos, Endereco endereco) {
        super(nome, faixaRenda, contatos, endereco);
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
}

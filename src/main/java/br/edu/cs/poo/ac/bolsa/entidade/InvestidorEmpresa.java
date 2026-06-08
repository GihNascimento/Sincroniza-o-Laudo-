package br.edu.cs.poo.ac.bolsa.entidade;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InvestidorEmpresa extends Investidor {
    private String cnpj;
    private double faturamento;
    private LocalDate dataAbertura;

    public InvestidorEmpresa() {
        super();
    }

    public InvestidorEmpresa(String nome, Endereco endereco, LocalDate dataAbertura,
                             BigDecimal bonus, Contatos contatos,
                             String cnpj, double faturamento) {
        super(nome, endereco, bonus, contatos);
        this.dataAbertura = dataAbertura;
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

    public LocalDate getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(LocalDate dataAbertura) { this.dataAbertura = dataAbertura; }
}

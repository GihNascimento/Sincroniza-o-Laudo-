package br.edu.cs.poo.ac.bolsa.entidade;

import br.edu.cs.poo.ac.bolsa.util.Registro;
import java.math.BigDecimal;

public abstract class Investidor extends Registro {
    private String nome;
    private FaixaRenda faixaRenda;
    private Contatos contatos;
    private Endereco endereco;
    private double saldo;

    public Investidor(String nome, FaixaRenda faixaRenda, Contatos contatos, Endereco endereco) {
        this.nome = nome;
        this.faixaRenda = faixaRenda;
        this.contatos = contatos;
        this.endereco = endereco;
        this.saldo = 0;
    }

    public abstract BigDecimal getEntradaFinanceira();

    public void creditarBonus(double valor) { saldo += valor; }

    public void debitarBonus(double valor) { saldo -= valor; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public FaixaRenda getFaixaRenda() { return faixaRenda; }
    public void setFaixaRenda(FaixaRenda faixaRenda) { this.faixaRenda = faixaRenda; }

    public Contatos getContatos() { return contatos; }
    public void setContatos(Contatos contatos) { this.contatos = contatos; }

    public Endereco getEndereco() { return endereco; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }

    public double getSaldo() { return saldo; }
    public void setSaldo(double saldo) { this.saldo = saldo; }
}

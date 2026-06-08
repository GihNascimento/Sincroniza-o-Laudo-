package br.edu.cs.poo.ac.bolsa.entidade;

import br.edu.cs.poo.ac.bolsa.util.Comparavel;
import java.math.BigDecimal;
import java.time.LocalDate;

public class InvestidorPessoa extends Investidor implements Comparavel {
    private String cpf;
    private double renda;
    private FaixaRenda faixaRenda;
    private LocalDate dataNascimento;

    public InvestidorPessoa() {
        super();
    }

    public InvestidorPessoa(String nome, Endereco endereco, LocalDate dataNascimento,
                            BigDecimal bonus, Contatos contatos,
                            String cpf, double renda, FaixaRenda faixaRenda) {
        super(nome, endereco, bonus, contatos);
        this.dataNascimento = dataNascimento;
        this.cpf = cpf;
        this.renda = renda;
        this.faixaRenda = faixaRenda;
    }

    @Override
    public String getIdentificador() {
        return cpf;
    }

    @Override
    public BigDecimal getEntradaFinanceira() {
        return new BigDecimal(renda);
    }

    @Override
    public int comparar(Comparavel c) {
        if (!(c instanceof InvestidorPessoa)) {
            throw new RuntimeException("O argumento nao e do tipo InvestidorPessoa");
        }
        return this.getNome().compareTo(((InvestidorPessoa) c).getNome());
    }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public double getRenda() { return renda; }
    public void setRenda(double renda) { this.renda = renda; }

    public FaixaRenda getFaixaRenda() { return faixaRenda; }
    public void setFaixaRenda(FaixaRenda faixaRenda) { this.faixaRenda = faixaRenda; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
}

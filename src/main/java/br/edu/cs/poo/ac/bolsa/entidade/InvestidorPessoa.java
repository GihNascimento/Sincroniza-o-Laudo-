package br.edu.cs.poo.ac.bolsa.entidade;

import br.edu.cs.poo.ac.bolsa.util.Comparavel;
import java.math.BigDecimal;

public class InvestidorPessoa extends Investidor implements Comparavel {
    private String cpf;
    private double renda;

    public InvestidorPessoa(String cpf, String nome, double renda,
                            FaixaRenda faixaRenda, Contatos contatos, Endereco endereco) {
        super(nome, faixaRenda, contatos, endereco);
        this.cpf = cpf;
        this.renda = renda;
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
        InvestidorPessoa outro = (InvestidorPessoa) c;
        return this.getNome().compareTo(outro.getNome());
    }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public double getRenda() { return renda; }
    public void setRenda(double renda) { this.renda = renda; }
}

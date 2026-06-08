package br.edu.cs.poo.ac.bolsa.entidade;

import br.edu.cs.poo.ac.bolsa.util.Registro;
import java.math.BigDecimal;

public abstract class Investidor extends Registro {
    private String nome;
    private Endereco endereco;
    private BigDecimal bonus;
    private Contatos contatos;

    public Investidor() {
        this.bonus = BigDecimal.ZERO;
    }

    public Investidor(String nome, Endereco endereco, BigDecimal bonus, Contatos contatos) {
        this.nome = nome;
        this.endereco = endereco;
        this.bonus = (bonus != null) ? bonus : BigDecimal.ZERO;
        this.contatos = contatos;
    }

    public abstract BigDecimal getEntradaFinanceira();

    public void creditarBonus(BigDecimal valor) {
        if (valor == null) return;
        bonus = bonus.add(valor);
    }

    public void debitarBonus(BigDecimal valor) {
        if (valor == null) return;
        bonus = bonus.subtract(valor);
    }

    public BigDecimal getBonus() { return bonus; }
    public void setBonus(BigDecimal bonus) { this.bonus = bonus; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Endereco getEndereco() { return endereco; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }

    public Contatos getContatos() { return contatos; }
    public void setContatos(Contatos contatos) { this.contatos = contatos; }
}

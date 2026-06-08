package br.edu.cs.poo.ac.bolsa.negocio;

import br.edu.cs.poo.ac.bolsa.entidade.Contatos;
import br.edu.cs.poo.ac.bolsa.entidade.Endereco;
import br.edu.cs.poo.ac.bolsa.entidade.FaixaRenda;

public class DadosInvestidor {
    private String cpfOuCnpj;
    private String nome;
    private double rendaOuFaturamento;
    private FaixaRenda faixaRenda;
    private Contatos contatos;
    private Endereco endereco;

    public DadosInvestidor(String cpfOuCnpj, String nome, double rendaOuFaturamento,
                           FaixaRenda faixaRenda, Contatos contatos, Endereco endereco) {
        this.cpfOuCnpj = cpfOuCnpj;
        this.nome = nome;
        this.rendaOuFaturamento = rendaOuFaturamento;
        this.faixaRenda = faixaRenda;
        this.contatos = contatos;
        this.endereco = endereco;
    }

    public String getCpfOuCnpj() { return cpfOuCnpj; }
    public void setCpfOuCnpj(String cpfOuCnpj) { this.cpfOuCnpj = cpfOuCnpj; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public double getRendaOuFaturamento() { return rendaOuFaturamento; }
    public void setRendaOuFaturamento(double rendaOuFaturamento) { this.rendaOuFaturamento = rendaOuFaturamento; }

    public FaixaRenda getFaixaRenda() { return faixaRenda; }
    public void setFaixaRenda(FaixaRenda faixaRenda) { this.faixaRenda = faixaRenda; }

    public Contatos getContatos() { return contatos; }
    public void setContatos(Contatos contatos) { this.contatos = contatos; }

    public Endereco getEndereco() { return endereco; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }
}

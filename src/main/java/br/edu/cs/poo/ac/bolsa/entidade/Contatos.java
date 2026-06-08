package br.edu.cs.poo.ac.bolsa.entidade;

import java.io.Serializable;

public class Contatos implements Serializable {
    private String telefoneCelular;
    private String telefoneResidencial;
    private String telefoneComercial;
    private String email;
    private String nomeContato;

    public Contatos() {}

    public Contatos(String telefoneCelular, String telefoneResidencial, String telefoneComercial,
                    String email, String nomeContato) {
        this.telefoneCelular = telefoneCelular;
        this.telefoneResidencial = telefoneResidencial;
        this.telefoneComercial = telefoneComercial;
        this.email = email;
        this.nomeContato = nomeContato;
    }

    public String getTelefoneCelular() { return telefoneCelular; }
    public void setTelefoneCelular(String telefoneCelular) { this.telefoneCelular = telefoneCelular; }

    public String getTelefoneResidencial() { return telefoneResidencial; }
    public void setTelefoneResidencial(String telefoneResidencial) { this.telefoneResidencial = telefoneResidencial; }

    public String getTelefoneComercial() { return telefoneComercial; }
    public void setTelefoneComercial(String telefoneComercial) { this.telefoneComercial = telefoneComercial; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNomeContato() { return nomeContato; }
    public void setNomeContato(String nomeContato) { this.nomeContato = nomeContato; }
}

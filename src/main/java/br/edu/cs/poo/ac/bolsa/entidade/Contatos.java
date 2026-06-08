package br.edu.cs.poo.ac.bolsa.entidade;

import java.io.Serializable;

public class Contatos implements Serializable {
    private String email;
    private String telefone;

    public Contatos(String email, String telefone) {
        this.email = email;
        this.telefone = telefone;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
}

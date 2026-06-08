package br.edu.cs.poo.ac.bolsa.dao;

import br.edu.cs.poo.ac.bolsa.util.Registro;

public class DAORegistro extends DAOGenerico {

    public DAORegistro(Class<?> tipo) {
        inicializarCadastro(tipo);
    }

    public Registro buscar(String id) {
        return (Registro) cadastro.buscar(id);
    }

    public boolean incluir(Registro r) {
        if (buscar(r.getIdentificador()) != null) return false;
        cadastro.incluir(r, r.getIdentificador());
        return true;
    }

    public boolean alterar(Registro r) {
        if (buscar(r.getIdentificador()) == null) return false;
        cadastro.alterar(r, r.getIdentificador());
        return true;
    }

    public boolean excluir(String id) {
        if (buscar(id) == null) return false;
        cadastro.excluir(id);
        return true;
    }
}

package br.edu.cs.poo.ac.bolsa.dao;

import br.edu.cs.poo.ac.bolsa.entidade.InvestidorPessoa;

public class DAOInvestidorPessoa extends DAORegistro {

    public DAOInvestidorPessoa() {
        super(InvestidorPessoa.class);
    }

    public InvestidorPessoa buscar(String cpf) {
        return (InvestidorPessoa) super.buscar(cpf);
    }

    public boolean incluir(InvestidorPessoa investidor) {
        return super.incluir(investidor);
    }

    public boolean alterar(InvestidorPessoa investidor) {
        return super.alterar(investidor);
    }

    public boolean excluir(String cpf) {
        return super.excluir(cpf);
    }

    public InvestidorPessoa[] consultarTodos() {
        Object[] todos = cadastro.buscarTodos();
        if (todos == null) return null;
        InvestidorPessoa[] resultado = new InvestidorPessoa[todos.length];
        for (int i = 0; i < todos.length; i++) {
            resultado[i] = (InvestidorPessoa) todos[i];
        }
        return resultado;
    }
}

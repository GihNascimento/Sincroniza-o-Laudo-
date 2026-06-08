package br.edu.cs.poo.ac.bolsa.dao;

import br.edu.cesarschool.next.oo.persistenciaobjetos.CadastroObjetos;
import br.edu.cs.poo.ac.bolsa.util.ExcecaoObjetoJaExistente;
import br.edu.cs.poo.ac.bolsa.util.ExcecaoOobjetoNaoExistente;
import br.edu.cs.poo.ac.bolsa.util.Registro;

public class DAO<T extends Registro> {

    private CadastroObjetos cadastro;

    public DAO(Class<T> tipo) {
        cadastro = new CadastroObjetos(tipo);
    }

    @SuppressWarnings("unchecked")
    public T buscar(String id) {
        return (T) cadastro.buscar(id);
    }

    public void incluir(T obj) {
        if (buscar(obj.getIdentificador()) != null) {
            throw new ExcecaoObjetoJaExistente();
        }
        cadastro.incluir(obj);
    }

    public void alterar(T obj) {
        if (buscar(obj.getIdentificador()) == null) {
            throw new ExcecaoOobjetoNaoExistente();
        }
        cadastro.alterar(obj);
    }

    public void excluir(String id) {
        if (buscar(id) == null) {
            throw new ExcecaoOobjetoNaoExistente();
        }
        cadastro.excluir(id);
    }

    @SuppressWarnings("unchecked")
    public T[] buscarTodos() {
        Object[] todos = cadastro.buscarTodos();
        if (todos == null) return (T[]) new Registro[0];
        T[] resultado = (T[]) new Registro[todos.length];
        for (int i = 0; i < todos.length; i++) {
            resultado[i] = (T) todos[i];
        }
        return resultado;
    }
}

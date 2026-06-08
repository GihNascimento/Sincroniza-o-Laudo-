package br.edu.cs.poo.ac.bolsa.dao;

import br.edu.cesarschool.next.oo.persistenciaobjetos.CadastroObjetos;
import br.edu.cs.poo.ac.bolsa.util.ExcecaoObjetoJaExistente;
import br.edu.cs.poo.ac.bolsa.util.ExcecaoOobjetoNaoExistente;
import br.edu.cs.poo.ac.bolsa.util.Registro;
import java.lang.reflect.Array;

public class DAO<T extends Registro> {

    private CadastroObjetos cadastro;
    private Class<T> tipo;

    public DAO(Class<T> tipo) {
        this.tipo = tipo;
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
        cadastro.incluir(obj, obj.getIdentificador());
    }

    public void alterar(T obj) {
        if (buscar(obj.getIdentificador()) == null) {
            throw new ExcecaoOobjetoNaoExistente();
        }
        cadastro.alterar(obj, obj.getIdentificador());
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
        if (todos == null) return (T[]) Array.newInstance(tipo, 0);
        T[] resultado = (T[]) Array.newInstance(tipo, todos.length);
        for (int i = 0; i < todos.length; i++) resultado[i] = (T) todos[i];
        return resultado;
    }
}

package br.edu.cs.poo.ac.bolsa.util;

public class ExcecaoObjetoJaExistente extends RuntimeException {
    public ExcecaoObjetoJaExistente() { super("Objeto já existente"); }
    public ExcecaoObjetoJaExistente(String mensagem) { super(mensagem); }
}

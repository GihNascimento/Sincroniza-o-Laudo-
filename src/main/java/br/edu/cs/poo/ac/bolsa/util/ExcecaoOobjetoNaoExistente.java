package br.edu.cs.poo.ac.bolsa.util;

public class ExcecaoOobjetoNaoExistente extends RuntimeException {
    public ExcecaoOobjetoNaoExistente() { super("Objeto não existente"); }
    public ExcecaoOobjetoNaoExistente(String mensagem) { super(mensagem); }
}

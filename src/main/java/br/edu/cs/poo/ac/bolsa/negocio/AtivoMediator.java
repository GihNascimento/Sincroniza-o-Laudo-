package br.edu.cs.poo.ac.bolsa.negocio;

import br.edu.cs.poo.ac.bolsa.dao.DAOAtivo;
import br.edu.cs.poo.ac.bolsa.entidade.Ativo;
import br.edu.cs.poo.ac.bolsa.util.MensagensValidacao;

public class AtivoMediator {

    private static AtivoMediator instancia;
    private DAOAtivo daoAtivo = new DAOAtivo();

    private AtivoMediator() {}

    public static AtivoMediator getInstancia() {
        if (instancia == null) instancia = new AtivoMediator();
        return instancia;
    }

    public MensagensValidacao incluirAtivo(Ativo ativo) {
        MensagensValidacao msgs = new MensagensValidacao();
        if (ativo == null) { msgs.adicionar("Ativo não pode ser nulo"); return msgs; }
        if (!daoAtivo.incluir(ativo)) msgs.adicionar("Ativo já cadastrado");
        return msgs;
    }

    public Ativo buscarAtivo(long codigo) {
        return daoAtivo.buscar(codigo);
    }

    public MensagensValidacao alterarAtivo(Ativo ativo) {
        MensagensValidacao msgs = new MensagensValidacao();
        if (ativo == null) { msgs.adicionar("Ativo não pode ser nulo"); return msgs; }
        if (!daoAtivo.alterar(ativo)) msgs.adicionar("Ativo não cadastrado");
        return msgs;
    }

    public MensagensValidacao excluirAtivo(long codigo) {
        MensagensValidacao msgs = new MensagensValidacao();
        if (!daoAtivo.excluir(codigo)) msgs.adicionar("Ativo não cadastrado");
        return msgs;
    }
}

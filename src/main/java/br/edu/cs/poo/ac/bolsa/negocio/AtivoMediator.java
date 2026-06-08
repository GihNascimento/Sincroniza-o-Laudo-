package br.edu.cs.poo.ac.bolsa.negocio;

import br.edu.cs.poo.ac.bolsa.dao.DAOAtivo;
import br.edu.cs.poo.ac.bolsa.entidade.Ativo;
import br.edu.cs.poo.ac.bolsa.util.MensagensValidacao;

public class AtivoMediator {

    private static AtivoMediator instancia;
    private DAOAtivo daoAtivo;

    public AtivoMediator() {
        daoAtivo = new DAOAtivo();
    }

    public static AtivoMediator getInstancia() {
        if (instancia == null) instancia = new AtivoMediator();
        return instancia;
    }

    public MensagensValidacao incluir(Ativo ativo) {
        MensagensValidacao msgs = new MensagensValidacao();
        if (ativo.getCodigo() <= 0) {
            msgs.adicionar("Código deve ser maior que zero.");
            return msgs;
        }
        if (!daoAtivo.incluir(ativo)) {
            msgs.adicionar("Ativo já existente.");
        }
        return msgs;
    }

    public Ativo buscar(long codigo) {
        if (codigo <= 0) return null;
        return daoAtivo.buscar(codigo);
    }

    public MensagensValidacao alterar(Ativo ativo) {
        MensagensValidacao msgs = new MensagensValidacao();
        if (ativo.getDescricao() == null || ativo.getDescricao().trim().isEmpty()) {
            msgs.adicionar("Descrição é obrigatória.");
            return msgs;
        }
        if (!daoAtivo.alterar(ativo)) {
            msgs.adicionar("Ativo não existente.");
        }
        return msgs;
    }

    public MensagensValidacao excluir(long codigo) {
        MensagensValidacao msgs = new MensagensValidacao();
        if (codigo <= 0) {
            msgs.adicionar("Código deve ser maior que zero.");
            return msgs;
        }
        if (!daoAtivo.excluir(codigo)) {
            msgs.adicionar("Ativo não existente.");
        }
        return msgs;
    }
}

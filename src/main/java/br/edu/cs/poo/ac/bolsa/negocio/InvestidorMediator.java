package br.edu.cs.poo.ac.bolsa.negocio;

import br.edu.cs.poo.ac.bolsa.dao.DAOInvestidorEmpresa;
import br.edu.cs.poo.ac.bolsa.dao.DAOInvestidorPessoa;
import br.edu.cs.poo.ac.bolsa.entidade.*;
import br.edu.cs.poo.ac.bolsa.util.*;

public class InvestidorMediator {

    private static InvestidorMediator instancia;
    private DAOInvestidorPessoa daoInvPes;
    private DAOInvestidorEmpresa daoInvEmp;

    public InvestidorMediator() {
        daoInvPes = new DAOInvestidorPessoa();
        daoInvEmp = new DAOInvestidorEmpresa();
    }

    public static InvestidorMediator getInstancia() {
        if (instancia == null) instancia = new InvestidorMediator();
        return instancia;
    }

    public MensagensValidacao incluirInvestidorPessoa(InvestidorPessoa investidor) {
        MensagensValidacao msgs = new MensagensValidacao();
        if (!daoInvPes.incluirInvestidorPessoa(investidor)) msgs.adicionar("Investidor já cadastrado");
        return msgs;
    }

    public InvestidorPessoa buscarInvestidorPessoa(String cpf) {
        return daoInvPes.buscarInvestidorPessoa(cpf);
    }

    public MensagensValidacao alterarInvestidorPessoa(InvestidorPessoa investidor) {
        MensagensValidacao msgs = new MensagensValidacao();
        if (!daoInvPes.alterarInvestidorPessoa(investidor)) msgs.adicionar("Investidor não cadastrado");
        return msgs;
    }

    public MensagensValidacao excluirInvestidorPessoa(String cpf) {
        MensagensValidacao msgs = new MensagensValidacao();
        if (!daoInvPes.excluirInvestidorPessoa(cpf)) msgs.adicionar("Investidor não cadastrado");
        return msgs;
    }

    public MensagensValidacao incluirInvestidorEmpresa(InvestidorEmpresa investidor) {
        MensagensValidacao msgs = new MensagensValidacao();
        if (!daoInvEmp.incluirInvestidorEmpresa(investidor)) msgs.adicionar("Investidor já cadastrado");
        return msgs;
    }

    public InvestidorEmpresa buscarInvestidorEmpresa(String cnpj) {
        return daoInvEmp.buscarInvestidorEmpresa(cnpj);
    }

    public MensagensValidacao alterarInvestidorEmpresa(InvestidorEmpresa investidor) {
        MensagensValidacao msgs = new MensagensValidacao();
        if (!daoInvEmp.alterarInvestidorEmpresa(investidor)) msgs.adicionar("Investidor não cadastrado");
        return msgs;
    }

    public MensagensValidacao excluirInvestidorEmpresa(String cnpj) {
        MensagensValidacao msgs = new MensagensValidacao();
        if (!daoInvEmp.excluirInvestidorEmpresa(cnpj)) msgs.adicionar("Investidor não cadastrado");
        return msgs;
    }

    public Investidor buscarInvestidor(String identificador) {
        if (identificador == null) return null;
        String digits = identificador.replaceAll("[^0-9]", "");
        if (digits.length() == 11) return buscarInvestidorPessoa(identificador);
        if (digits.length() == 14) return buscarInvestidorEmpresa(identificador);
        if (identificador.length() == 11) return buscarInvestidorPessoa(identificador);
        if (identificador.length() == 14) return buscarInvestidorEmpresa(identificador);
        return null;
    }

    public MensagensValidacao alterarInvestidor(Investidor investidor) {
        if (investidor instanceof InvestidorPessoa) return alterarInvestidorPessoa((InvestidorPessoa) investidor);
        if (investidor instanceof InvestidorEmpresa) return alterarInvestidorEmpresa((InvestidorEmpresa) investidor);
        MensagensValidacao msgs = new MensagensValidacao();
        msgs.adicionar("Tipo de investidor desconhecido");
        return msgs;
    }

    public InvestidorPessoa[] consultarInvestidorPessoa(OrdenacaoInvestidorPessoa criterio) {
        InvestidorPessoa[] todos = daoInvPes.consultarTodos();
        if (todos == null || todos.length == 0) return todos;
        Comparavel[] comps = new Comparavel[todos.length];
        for (int i = 0; i < todos.length; i++) comps[i] = todos[i];
        Comparador comparador = (criterio == OrdenacaoInvestidorPessoa.NOME)
                ? new ComparadorGenerico()
                : new br.edu.cs.poo.ac.bolsa.entidade.ComparadorInvestidorPessoaRenda();
        Ordenador.ordenar(comps, comparador);
        InvestidorPessoa[] resultado = new InvestidorPessoa[comps.length];
        for (int i = 0; i < comps.length; i++) resultado[i] = (InvestidorPessoa) comps[i];
        return resultado;
    }
}

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
        MensagensValidacao msgs = validarInvestidorPessoa(investidor);
        if (!msgs.estaVazio()) return msgs;
        if (!daoInvPes.incluirInvestidorPessoa(investidor))
            msgs.adicionar("Investidor Pessoa já existente.");
        return msgs;
    }

    public InvestidorPessoa buscarInvestidorPessoa(String cpf) {
        return daoInvPes.buscarInvestidorPessoa(cpf);
    }

    public MensagensValidacao alterarInvestidorPessoa(InvestidorPessoa investidor) {
        MensagensValidacao msgs = validarInvestidorPessoa(investidor);
        if (!msgs.estaVazio()) return msgs;
        if (!daoInvPes.alterarInvestidorPessoa(investidor))
            msgs.adicionar("Investidor Pessoa não existente.");
        return msgs;
    }

    public MensagensValidacao excluirInvestidorPessoa(String cpf) {
        MensagensValidacao msgs = new MensagensValidacao();
        if (!daoInvPes.excluirInvestidorPessoa(cpf))
            msgs.adicionar("Investidor Pessoa não existente.");
        return msgs;
    }

    public MensagensValidacao incluirInvestidorEmpresa(InvestidorEmpresa investidor) {
        MensagensValidacao msgs = validarInvestidorEmpresa(investidor);
        if (!msgs.estaVazio()) return msgs;
        if (!daoInvEmp.incluirInvestidorEmpresa(investidor))
            msgs.adicionar("Investidor Empresa já existente.");
        return msgs;
    }

    public InvestidorEmpresa buscarInvestidorEmpresa(String cnpj) {
        return daoInvEmp.buscarInvestidorEmpresa(cnpj);
    }

    public MensagensValidacao alterarInvestidorEmpresa(InvestidorEmpresa investidor) {
        MensagensValidacao msgs = validarInvestidorEmpresa(investidor);
        if (!msgs.estaVazio()) return msgs;
        if (!daoInvEmp.alterarInvestidorEmpresa(investidor))
            msgs.adicionar("Investidor Empresa não existente.");
        return msgs;
    }

    public MensagensValidacao excluirInvestidorEmpresa(String cnpj) {
        MensagensValidacao msgs = new MensagensValidacao();
        if (!daoInvEmp.excluirInvestidorEmpresa(cnpj))
            msgs.adicionar("Investidor Empresa não existente.");
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
        MensagensValidacao msgs = new MensagensValidacao();
        if (investidor instanceof InvestidorPessoa) {
            if (!daoInvPes.alterarInvestidorPessoa((InvestidorPessoa) investidor))
                msgs.adicionar("Investidor Pessoa não existente.");
            return msgs;
        }
        if (investidor instanceof InvestidorEmpresa) {
            if (!daoInvEmp.alterarInvestidorEmpresa((InvestidorEmpresa) investidor))
                msgs.adicionar("Investidor Empresa não existente.");
            return msgs;
        }
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

    // -------------------------------------------------------------------------
    // Validações privadas
    // -------------------------------------------------------------------------

    private MensagensValidacao validarInvestidorPessoa(InvestidorPessoa inv) {
        MensagensValidacao msgs = new MensagensValidacao();
        validarEndereco(inv == null ? null : inv.getEndereco(), msgs);
        validarContatos(inv == null ? null : inv.getContatos(), false, msgs);
        return msgs;
    }

    private MensagensValidacao validarInvestidorEmpresa(InvestidorEmpresa inv) {
        MensagensValidacao msgs = new MensagensValidacao();
        validarEndereco(inv == null ? null : inv.getEndereco(), msgs);
        validarContatos(inv == null ? null : inv.getContatos(), true, msgs);
        return msgs;
    }

    private void validarEndereco(Endereco end, MensagensValidacao msgs) {
        String logradouro = end == null ? null : end.getLogradouro();
        if (logradouro == null || logradouro.trim().isEmpty())
            msgs.adicionar("Logradouro não informado.");

        String numero = end == null ? null : end.getNumero();
        if (numero == null || numero.trim().isEmpty())
            msgs.adicionar("Número não informado.");

        String cidade = end == null ? null : end.getCidade();
        if (cidade == null || cidade.trim().isEmpty())
            msgs.adicionar("Cidade não informada.");

        String estado = end == null ? null : end.getEstado();
        if (estado == null || estado.trim().isEmpty())
            msgs.adicionar("Estado não informado.");

        String pais = end == null ? null : end.getPais();
        if (pais == null || pais.trim().isEmpty())
            msgs.adicionar("País não informado.");
    }

    private void validarContatos(Contatos cont, boolean pj, MensagensValidacao msgs) {
        String email = cont == null ? null : cont.getEmail();
        if (email == null || email.trim().isEmpty() || !email.contains("@"))
            msgs.adicionar("E-mail inválido.");

        String telefone = cont == null ? null : cont.getTelefoneCelular();
        if (pj) telefone = cont == null ? null : cont.getTelefoneComercial();

        if (telefone == null || telefone.trim().isEmpty()) {
            msgs.adicionar("Telefone não informado.");
        } else if (!telefone.matches("[0-9]+")) {
            msgs.adicionar("Telefone inválido.");
        }

        if (pj) {
            String nomeContato = cont == null ? null : cont.getNomeContato();
            if (nomeContato == null || nomeContato.trim().isEmpty())
                msgs.adicionar("Nome do contato não informado.");
        }
    }
}

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
        MensagensValidacao msgs = validarCamposInvestidorPessoa(investidor);
        if (!msgs.estaVazio()) return msgs;
        if (!daoInvPes.incluirInvestidorPessoa(investidor))
            msgs.adicionar("Investidor Pessoa já existente.");
        return msgs;
    }

    public InvestidorPessoa buscarInvestidorPessoa(String cpf) {
        return daoInvPes.buscarInvestidorPessoa(cpf);
    }

    public MensagensValidacao alterarInvestidorPessoa(InvestidorPessoa investidor) {
        MensagensValidacao msgs = validarCamposInvestidorPessoa(investidor);
        if (!msgs.estaVazio()) return msgs;
        if (!daoInvPes.alterarInvestidorPessoa(investidor))
            msgs.adicionar("Investidor Pessoa não existente.");
        return msgs;
    }

    public MensagensValidacao excluirInvestidorPessoa(String cpf) {
        MensagensValidacao msgs = new MensagensValidacao();
        ResultadoValidacao r = ValidadorCpfCnpj.validarCpf(cpf);
        if (r == ResultadoValidacao.NAO_INFORMADO) {
            msgs.adicionar("CPF do investidor pessoa não informado.");
            return msgs;
        }
        if (r != null) {
            msgs.adicionar("CPF do investidor pessoa inválido.");
            return msgs;
        }
        if (!daoInvPes.excluirInvestidorPessoa(cpf))
            msgs.adicionar("Investidor Pessoa não existente.");
        return msgs;
    }

    public MensagensValidacao incluirInvestidorEmpresa(InvestidorEmpresa investidor) {
        MensagensValidacao msgs = validarCamposInvestidorEmpresa(investidor);
        if (!msgs.estaVazio()) return msgs;
        if (!daoInvEmp.incluirInvestidorEmpresa(investidor))
            msgs.adicionar("Investidor Empresa já existente.");
        return msgs;
    }

    public InvestidorEmpresa buscarInvestidorEmpresa(String cnpj) {
        return daoInvEmp.buscarInvestidorEmpresa(cnpj);
    }

    public MensagensValidacao alterarInvestidorEmpresa(InvestidorEmpresa investidor) {
        MensagensValidacao msgs = validarCamposInvestidorEmpresa(investidor);
        if (!msgs.estaVazio()) return msgs;
        if (!daoInvEmp.alterarInvestidorEmpresa(investidor))
            msgs.adicionar("Investidor Empresa não existente.");
        return msgs;
    }

    public MensagensValidacao excluirInvestidorEmpresa(String cnpj) {
        MensagensValidacao msgs = new MensagensValidacao();
        ResultadoValidacao r = ValidadorCpfCnpj.validarCnpj(cnpj);
        if (r == ResultadoValidacao.NAO_INFORMADO) {
            msgs.adicionar("CNPJ do investidor empresa não informado.");
            return msgs;
        }
        if (r != null) {
            msgs.adicionar("CNPJ do investidor empresa inválido.");
            return msgs;
        }
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

    private MensagensValidacao validarCamposInvestidorPessoa(InvestidorPessoa investidor) {
        MensagensValidacao msgs = new MensagensValidacao();

        ResultadoValidacao cpfResult = ValidadorCpfCnpj.validarCpf(
                investidor != null ? investidor.getCpf() : null);
        if (cpfResult == ResultadoValidacao.NAO_INFORMADO) {
            msgs.adicionar("CPF do investidor pessoa não informado.");
        } else if (cpfResult != null) {
            msgs.adicionar("CPF do investidor pessoa inválido.");
        }

        if (investidor == null || investidor.getNome() == null || investidor.getNome().trim().isEmpty()) {
            msgs.adicionar("Nome do investidor pessoa não informado.");
        }

        if (investidor == null || investidor.getEndereco() == null
                || investidor.getEndereco().getLogradouro() == null
                || investidor.getEndereco().getLogradouro().trim().isEmpty()) {
            msgs.adicionar("Logradouro do investidor pessoa não informado.");
        }

        if (investidor == null || investidor.getContatos() == null
                || investidor.getContatos().getTelefoneCelular() == null
                || investidor.getContatos().getTelefoneCelular().trim().isEmpty()) {
            msgs.adicionar("Telefone celular do investidor pessoa não informado.");
        }

        if (investidor == null || investidor.getContatos() == null
                || investidor.getContatos().getEmail() == null
                || investidor.getContatos().getEmail().trim().isEmpty()) {
            msgs.adicionar("E-mail do investidor pessoa não informado.");
        }

        return msgs;
    }

    private MensagensValidacao validarCamposInvestidorEmpresa(InvestidorEmpresa investidor) {
        MensagensValidacao msgs = new MensagensValidacao();

        ResultadoValidacao cnpjResult = ValidadorCpfCnpj.validarCnpj(
                investidor != null ? investidor.getCnpj() : null);
        if (cnpjResult == ResultadoValidacao.NAO_INFORMADO) {
            msgs.adicionar("CNPJ do investidor empresa não informado.");
        } else if (cnpjResult != null) {
            msgs.adicionar("CNPJ do investidor empresa inválido.");
        }

        if (investidor == null || investidor.getNome() == null || investidor.getNome().trim().isEmpty()) {
            msgs.adicionar("Nome do investidor empresa não informado.");
        }

        if (investidor == null || investidor.getEndereco() == null
                || investidor.getEndereco().getLogradouro() == null
                || investidor.getEndereco().getLogradouro().trim().isEmpty()) {
            msgs.adicionar("Logradouro do investidor empresa não informado.");
        }

        if (investidor == null || investidor.getContatos() == null
                || investidor.getContatos().getTelefoneComercial() == null
                || investidor.getContatos().getTelefoneComercial().trim().isEmpty()) {
            msgs.adicionar("Telefone comercial do investidor empresa não informado.");
        }

        if (investidor == null || investidor.getContatos() == null
                || investidor.getContatos().getEmail() == null
                || investidor.getContatos().getEmail().trim().isEmpty()) {
            msgs.adicionar("E-mail do investidor empresa não informado.");
        }

        return msgs;
    }
}

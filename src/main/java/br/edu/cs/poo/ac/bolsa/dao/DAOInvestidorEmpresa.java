package br.edu.cs.poo.ac.bolsa.dao;

import br.edu.cs.poo.ac.bolsa.entidade.InvestidorEmpresa;

public class DAOInvestidorEmpresa extends DAORegistro {

    public DAOInvestidorEmpresa() {
        super(InvestidorEmpresa.class);
    }

    public InvestidorEmpresa buscarInvestidorEmpresa(String cnpj) {
        return (InvestidorEmpresa) super.buscar(cnpj);
    }

    public boolean incluirInvestidorEmpresa(InvestidorEmpresa investidor) {
        return super.incluir(investidor);
    }

    public boolean alterarInvestidorEmpresa(InvestidorEmpresa investidor) {
        return super.alterar(investidor);
    }

    public boolean excluirInvestidorEmpresa(String cnpj) {
        return super.excluir(cnpj);
    }
}

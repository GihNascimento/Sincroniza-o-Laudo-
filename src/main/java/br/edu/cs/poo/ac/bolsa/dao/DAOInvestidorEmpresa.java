package br.edu.cs.poo.ac.bolsa.dao;

import br.edu.cs.poo.ac.bolsa.entidade.InvestidorEmpresa;

public class DAOInvestidorEmpresa extends DAORegistro {

    public DAOInvestidorEmpresa() {
        super(InvestidorEmpresa.class);
    }

    public InvestidorEmpresa buscar(String cnpj) {
        return (InvestidorEmpresa) super.buscar(cnpj);
    }

    public boolean incluir(InvestidorEmpresa investidor) {
        return super.incluir(investidor);
    }

    public boolean alterar(InvestidorEmpresa investidor) {
        return super.alterar(investidor);
    }

    public boolean excluir(String cnpj) {
        return super.excluir(cnpj);
    }
}

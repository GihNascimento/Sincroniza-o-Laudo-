package br.edu.cs.poo.ac.bolsa.entidade;

import br.edu.cs.poo.ac.bolsa.util.Registro;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Titulo extends Registro {
    private Investidor investidor;
    private Ativo ativo;
    private BigDecimal valorInvestido;
    private BigDecimal valorAtual;
    private BigDecimal taxaDiaria;
    private LocalDate dataAplicacao;
    private LocalDate dataVencimento;
    private LocalDate dataUltimoRendimento;
    private StatusTitulo status;

    // Novo construtor com Investidor genérico (9 parâmetros)
    public Titulo(Investidor investidor, Ativo ativo, BigDecimal valorInvestido,
                  BigDecimal valorAtual, BigDecimal taxaDiaria, LocalDate dataAplicacao,
                  LocalDate dataVencimento, LocalDate dataUltimoRendimento, StatusTitulo status) {
        this.investidor = investidor;
        this.ativo = ativo;
        this.valorInvestido = valorInvestido;
        this.valorAtual = valorAtual;
        this.taxaDiaria = taxaDiaria;
        this.dataAplicacao = dataAplicacao;
        this.dataVencimento = dataVencimento;
        this.dataUltimoRendimento = dataUltimoRendimento;
        this.status = status;
    }

    // Construtor antigo com InvestidorPessoa e InvestidorEmpresa (10 parâmetros) - compatibilidade
    public Titulo(InvestidorPessoa investidorPessoa, InvestidorEmpresa investidorEmpresa,
                  Ativo ativo, BigDecimal valorInvestido, BigDecimal valorAtual,
                  BigDecimal taxaDiaria, LocalDate dataAplicacao, LocalDate dataVencimento,
                  LocalDate dataUltimoRendimento, StatusTitulo status) {
        this.investidor = (investidorPessoa != null) ? investidorPessoa : investidorEmpresa;
        this.ativo = ativo;
        this.valorInvestido = valorInvestido;
        this.valorAtual = valorAtual;
        this.taxaDiaria = taxaDiaria;
        this.dataAplicacao = dataAplicacao;
        this.dataVencimento = dataVencimento;
        this.dataUltimoRendimento = dataUltimoRendimento;
        this.status = status;
    }

    @Override
    public String getIdentificador() {
        return getNumero();
    }

    public String getNumero() {
        String dataFormatada = dataAplicacao.atStartOfDay()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        if (investidor instanceof InvestidorPessoa) {
            String cpf = ((InvestidorPessoa) investidor).getCpf();
            return "000" + cpf + ativo.getCodigo() + dataFormatada;
        } else {
            String cnpj = ((InvestidorEmpresa) investidor).getCnpj();
            return cnpj + ativo.getCodigo() + dataFormatada;
        }
    }

    public boolean render() {
        BigDecimal fator = BigDecimal.ONE.add(taxaDiaria.divide(new BigDecimal("100"), 10, java.math.RoundingMode.HALF_UP));
        valorAtual = valorAtual.multiply(fator).setScale(2, java.math.RoundingMode.HALF_UP);

        LocalDate hoje = LocalDate.now();
        if (dataUltimoRendimento == null || !dataUltimoRendimento.plusMonths(1).isAfter(hoje)) {
            dataUltimoRendimento = hoje;
            return true;
        }
        return false;
    }

    public Investidor getInvestidor() { return investidor; }
    public void setInvestidor(Investidor investidor) { this.investidor = investidor; }

    public Ativo getAtivo() { return ativo; }
    public void setAtivo(Ativo ativo) { this.ativo = ativo; }

    public BigDecimal getValorInvestido() { return valorInvestido; }
    public void setValorInvestido(BigDecimal valorInvestido) { this.valorInvestido = valorInvestido; }

    public BigDecimal getValorAtual() { return valorAtual; }
    public void setValorAtual(BigDecimal valorAtual) { this.valorAtual = valorAtual; }

    public BigDecimal getTaxaDiaria() { return taxaDiaria; }
    public void setTaxaDiaria(BigDecimal taxaDiaria) { this.taxaDiaria = taxaDiaria; }

    public LocalDate getDataAplicacao() { return dataAplicacao; }
    public void setDataAplicacao(LocalDate dataAplicacao) { this.dataAplicacao = dataAplicacao; }

    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }

    public LocalDate getDataUltimoRendimento() { return dataUltimoRendimento; }
    public void setDataUltimoRendimento(LocalDate dataUltimoRendimento) { this.dataUltimoRendimento = dataUltimoRendimento; }

    public StatusTitulo getStatus() { return status; }
    public void setStatus(StatusTitulo status) { this.status = status; }
}

package br.edu.cs.poo.ac.bolsa.negocio;

import br.edu.cs.poo.ac.bolsa.dao.DAO;
import br.edu.cs.poo.ac.bolsa.entidade.*;
import br.edu.cs.poo.ac.bolsa.util.ExcecaoNegocio;
import br.edu.cs.poo.ac.bolsa.util.MensagensValidacao;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;

public class TituloMediator {

    private static TituloMediator instancia;
    private DAO<Titulo> daoTitulo = new DAO<>(Titulo.class);
    private AtivoMediator ativoMediator = AtivoMediator.getInstancia();
    private InvestidorMediator investidorMediator = InvestidorMediator.getInstancia();

    private TituloMediator() {}

    public static TituloMediator getInstancia() {
        if (instancia == null) instancia = new TituloMediator();
        return instancia;
    }

    public void incluir(DadosTitulo dados) throws ExcecaoNegocio {
        MensagensValidacao msgs = new MensagensValidacao();

        String cpfOuCnpj = dados.getCpfOuCnpj();
        if (cpfOuCnpj == null || cpfOuCnpj.trim().isEmpty()) {
            msgs.adicionar("CPF/CNPJ inválido");
        }

        if (dados.getCodigoAtivo() <= 0) {
            msgs.adicionar("Código do ativo inválido");
        }

        if (dados.getValorInvestido() == null) {
            msgs.adicionar("Valor investido não pode ser nulo");
        }

        if (dados.getTaxaDiaria() == null) {
            msgs.adicionar("Taxa diária não pode ser nula");
        }

        Ativo ativo = null;
        if (dados.getCodigoAtivo() > 0) {
            ativo = ativoMediator.buscar(dados.getCodigoAtivo());
            if (ativo == null) msgs.adicionar("Ativo não encontrado");
        }

        Investidor investidor = null;
        if (cpfOuCnpj != null && !cpfOuCnpj.trim().isEmpty()) {
            investidor = investidorMediator.buscarInvestidor(cpfOuCnpj);
            if (investidor == null) msgs.adicionar("Investidor não encontrado");
        }

        if (ativo != null && dados.getValorInvestido() != null) {
            BigDecimal minimo = BigDecimal.valueOf(ativo.getValorMinimoAplicacao());
            BigDecimal maximo = BigDecimal.valueOf(ativo.getValorMaximoAplicacao());
            if (dados.getValorInvestido().compareTo(minimo) < 0
                    || dados.getValorInvestido().compareTo(maximo) > 0) {
                msgs.adicionar("Valor investido fora da faixa permitida");
            }
        }

        if (ativo != null && dados.getTaxaDiaria() != null) {
            BigDecimal cem = new BigDecimal("100");
            BigDecimal fator = BigDecimal.ONE.add(
                    dados.getTaxaDiaria().divide(cem, 10, RoundingMode.HALF_UP));
            BigDecimal fator30 = fator.pow(30, new MathContext(10, RoundingMode.HALF_UP));
            BigDecimal taxaMensal = cem.multiply(fator30.subtract(BigDecimal.ONE));
            if (taxaMensal.compareTo(BigDecimal.valueOf(ativo.getTaxaMensalMinima())) < 0
                    || taxaMensal.compareTo(BigDecimal.valueOf(ativo.getTaxaMensalMaxima())) > 0) {
                msgs.adicionar("Taxa diária fora da faixa permitida");
            }
        }

        if (ativo != null && investidor != null) {
            if (investidor.getEntradaFinanceira()
                    .compareTo(ativo.getFaixaMinimaPermitida().getValorInicial()) < 0) {
                msgs.adicionar("Entrada financeira do investidor abaixo da faixa mínima");
            }
        }

        if (!msgs.estaVazio()) throw new ExcecaoNegocio(msgs);

        LocalDate dataAplicacao = LocalDate.now();
        Titulo titulo = new Titulo(investidor, ativo,
                dados.getValorInvestido(), dados.getValorInvestido(),
                dados.getTaxaDiaria(), dataAplicacao,
                dataAplicacao.plusMonths(ativo.getPrazoEmMeses()),
                null, StatusTitulo.ATIVO);
        daoTitulo.incluir(titulo);
    }

    public void processarRendimentos() {
        Titulo[] titulos = daoTitulo.buscarTodos();
        if (titulos == null) return;
        for (Titulo titulo : titulos) {
            if (titulo.getStatus() != StatusTitulo.ATIVO) continue;
            boolean rendeu = titulo.render();
            if (rendeu) {
                BigDecimal bonus = titulo.getValorAtual()
                        .subtract(titulo.getValorInvestido())
                        .multiply(new BigDecimal("0.0001"));
                Investidor inv = investidorMediator.buscarInvestidor(
                        titulo.getInvestidor().getIdentificador());
                if (inv != null) {
                    inv.creditarBonus(bonus);
                    investidorMediator.alterarInvestidor(inv);
                }
            }
            if (!titulo.getDataVencimento().isAfter(LocalDate.now())) {
                titulo.setStatus(StatusTitulo.VENCIDO);
            }
            daoTitulo.alterar(titulo);
        }
    }

    public void cancelarTitulo(String numero) throws ExcecaoNegocio {
        MensagensValidacao msgs = new MensagensValidacao();

        Titulo titulo = daoTitulo.buscar(numero);
        if (titulo == null) {
            msgs.adicionar("Título não encontrado");
            throw new ExcecaoNegocio(msgs);
        }

        if (titulo.getStatus() == StatusTitulo.VENCIDO
                || titulo.getStatus() == StatusTitulo.CANCELADO) {
            msgs.adicionar("Título não pode ser cancelado");
            throw new ExcecaoNegocio(msgs);
        }

        titulo.setStatus(StatusTitulo.CANCELADO);
        daoTitulo.alterar(titulo);

        Investidor inv = investidorMediator.buscarInvestidor(
                titulo.getInvestidor().getIdentificador());
        if (inv != null) {
            BigDecimal debito = inv.getBonus().multiply(new BigDecimal("0.7"));
            inv.debitarBonus(debito);
            investidorMediator.alterarInvestidor(inv);
        }
    }
}

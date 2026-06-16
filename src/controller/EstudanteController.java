package controller;

import common.ConfigApp;
import bll.EstudanteBLL;
import bll.PagamentoBLL;
import dal.HistoricoDAL;
import dal.HistoricoDALFile;
import dal.HistoricoDALSql;
import model.Estudante;
import model.RepositorioDados;
import utils.Consola;
import view.EstudanteView;

import java.util.List;

/**
 * Controlador responsável por gerir o painel do Estudante.
 * Liga a EstudanteView às BLLs correspondentes, sem aceder a DALs
 * nem a ficheiros CSV diretamente.
  */
public class EstudanteController {

    private final RepositorioDados repositorio;
    private final Estudante estudanteAtivo;
    private final EstudanteView view;
    private final EstudanteBLL estudanteBll;
    private final PagamentoBLL pagamentoBll;
    private final HistoricoDAL historicoDAL =
            ConfigApp.isModoSql() ? new HistoricoDALSql() : new HistoricoDALFile();

    public EstudanteController(RepositorioDados repositorio, Estudante estudanteAtivo) {
        this.repositorio = repositorio;
        this.estudanteAtivo = estudanteAtivo;
        this.view = new EstudanteView();
        this.estudanteBll = new EstudanteBLL();
        this.pagamentoBll = new PagamentoBLL();
        this.historicoDAL.inicializar();
    }

    public void iniciar() {
        boolean aExecutar = true;
        while (aExecutar) {
            try {
                int opcao = view.mostrarMenuPrincipal();
                switch (opcao) {
                    case 1: visualizarDadosPessoais();
                        break;
                    case 2: atualizarMorada();
                        break;
                    case 3: alterarPassword();
                        break;
                    case 4: consultarDadosFinanceiros();
                        break;
                    case 5: verUcsInscritas();
                        break;
                    case 6: verNotas();
                        break;
                    case 7: consultarHistorico();
                        break;
                    case 0:
                        view.mostrarDespedida();
                        repositorio.limparSessao();
                        aExecutar = false;
                        break;
                    default:
                        view.mostrarOpcaoInvalida();
                }
            } catch (Exception e) {
                view.mostrarErroLeitura();
            }
        }
    }

    /**
     * Mostra os dados pessoais e o percurso académico do estudante.
     */
    private void visualizarDadosPessoais() {
        view.mostrarDadosPessoais(estudanteAtivo);
    }

    private void atualizarMorada() {
        String novaMorada = view.pedirNovaMorada();
        if (!novaMorada.isEmpty()) {
            estudanteBll.atualizarMorada(estudanteAtivo, novaMorada);
            view.mostrarSucessoAtualizacaoMorada();
        } else {
            view.mostrarSemAlteracaoMorada();
        }
    }

    private void alterarPassword() {
        String novaPass = view.pedirNovaPassword();
        if (!novaPass.isEmpty()) {
            estudanteBll.alterarPassword(estudanteAtivo, novaPass);
            view.mostrarSucessoAtualizacaoPassword();
        } else {
            view.mostrarCancelamentoPassword();
        }
    }

    /**
     * Mostra o histórico de pagamentos e o saldo devedor.
     * Se existir dívida, oferece as opções de pagamento total ou parcial.
     */
    private void consultarDadosFinanceiros() {
        view.mostrarDadosFinanceiros(estudanteAtivo);

        double divida = estudanteAtivo.getSaldoDevedor();
        if (divida <= 0) {
            view.mostrarSemPagamentosPendentes();
            return;
        }
        int opcao = view.pedirTipoPagamento(divida);
        double valorAPagar;

        switch (opcao) {
            case 1:
                valorAPagar = divida; // pagamento total
                break;
            case 2:
                valorAPagar = view.pedirValorPagamentoParcial(divida); // pagamento parcial
                break;
            default:
                return; // cancelar
        }

        if (valorAPagar <= 0) return;

        boolean sucesso = pagamentoBll.processarPagamento(estudanteAtivo, valorAPagar);
        if (sucesso) {
            view.mostrarSucessoPagamento(valorAPagar, estudanteAtivo.getSaldoDevedor());
        } else {
            view.mostrarErroValorInvalido();
        }
    }

    private void verUcsInscritas() {
        String info = estudanteBll.obterInfoInscricoes(estudanteAtivo);
        view.mostrarInscricoes(info);
    }

    private void verNotas() {
        String notas = estudanteBll.obterNotasDoEstudante(estudanteAtivo);
        Consola.imprimirTitulo("Minhas Notas");
        System.out.println(notas);
        Consola.pausar();
    }
    private void consultarHistorico() {
        try {
            Consola.imprimirTitulo("O Meu Histórico Académico");


            int ano = utils.Consola.lerInt("Introduza o Ano Letivo que deseja consultar");

            java.util.List<String> historico = historicoDAL.consultarHistoricoPorAluno(estudanteAtivo.getNumeroMecanografico());

            boolean encontrou = false;
            for (String registo : historico) {
                String[] p = registo.split(";");
                // Só mostra se o ano do ficheiro for igual ao ano digitado!
                if (p.length >= 5 && Integer.parseInt(p[0].trim()) == ano) {
                    System.out.printf("  Ano: %-5s | UC: %-6s | Notas: %-15s | %s%n", p[0], p[2], p[3], p[4]);
                    encontrou = true;
                }
            }

            if (!encontrou) {
                System.out.println("  Não foram encontrados registos para o ano " + ano);
            }
            Consola.pausar();
        } catch (utils.CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }
}
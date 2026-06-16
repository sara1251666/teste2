package controller;

import bll.AnoLetivoBLL;
import bll.EstadoInvalidoException;
import model.AnoLetivo;
import model.RepositorioDados;
import utils.CancelamentoException;
import utils.Consola;
import view.AnoLetivoView;
import view.GestorView;

import java.util.List;

/**
 * Controlador do módulo Ano Letivo — Sprint 6.
 *
 * Alterações face à Sprint 5:
 *   - Removidas as opções Editar e Eliminar (tarefa 1).
 *   - iniciarAno() passa a usar o novo fluxo interativo com relatório de aptidão (tarefa 2/3).
 *   - fechar() exibe pendências separadas por categoria (notas e propinas) (tarefa 4).
 *   - Novo método alterarMomentosUc() (tarefa 5).
 *   - Novo método verHistorico() (tarefa 6).
 */
public class AnoLetivoController {

    private final RepositorioDados repo;
    private final AnoLetivoView view;
    private final AnoLetivoBLL bll;

    public AnoLetivoController(RepositorioDados repo) {
        this.repo = repo;
        this.view = new AnoLetivoView();
        this.bll  = new AnoLetivoBLL();
    }

    /**
     * Loop principal do menu Ano Letivo.
     * Chamado pelo GestorController.
     *
     * Menu Sprint 6 (sem Editar nem Eliminar):
     *   1 - Criar Ano Letivo
     *   2 - Iniciar Ano Letivo
     *   3 - Fechar Ano Letivo
     *   4 - Avançar Ano Letivo
     *   5 - Listar Anos Letivos
     *   6 - Estado do Ano Atual
     *   7 - Histórico de Anos Fechados
     *   8 - Alterar Momentos de Avaliação de UC
     *   0 - Voltar
     */
    public void iniciar(GestorView gestorView) {
        boolean correr = true;
        while (correr) {
            int opcao = view.mostrarMenu();
            switch (opcao) {
                case 1: criar();                      break;
                case 2: iniciarAno();                 break;
                case 3: fechar();                     break;
                case 4: avancar(gestorView);          break;
                case 5: listar();                     break;
                case 6: estado();                     break;
                case 7: verHistorico();               break;
                case 8: alterarMomentosUc();          break;
                case 0: correr = false;               break;
                default: view.mostrarErro("Opção inválida.");
            }
        }
    }

    // ----------------------------------------------------------------
    // AÇÕES
    // ----------------------------------------------------------------

    private void criar() {
        try {
            int ano = view.pedirAno("Ano letivo a criar (ex: 2027)");
            bll.criar(ano);
            view.mostrarSucesso("Ano letivo " + ano + " criado em PLANEAMENTO.");
        } catch (CancelamentoException e) {
            view.mostrarMensagem("Operação cancelada.");
        } catch (EstadoInvalidoException e) {
            view.mostrarErro(e.getMessage());
        }
    }

    /**
     * Inicia o ano letivo com relatório de aptidão interativo.
     * O gestor pode inativar cursos não aptos e prosseguir, ou cancelar.
     */
    private void iniciarAno() {
        try {
            Consola.imprimirCabecalho("Iniciar Ano Letivo");
            List<AnoLetivo> anos = bll.listar().stream()
                    .filter(a -> a.getEstado() == model.EstadoAnoLetivo.PLANEAMENTO)
                    .collect(java.util.stream.Collectors.toList());
            AnoLetivo selecionado = view.pedirSelecaoAno(anos);
            if (selecionado == null) return;
            int ano = selecionado.getAno();

            List<String> cursosInativados = bll.iniciar(ano, view);

            if (!cursosInativados.isEmpty()) {
                view.mostrarSucesso("Ano letivo " + ano + " iniciado.\n"
                        + "  Cursos inativados durante o arranque: " + String.join(", ", cursosInativados));
            } else {
                view.mostrarSucesso("Ano letivo " + ano + " iniciado com sucesso. Todos os cursos aptos.");
            }
        } catch (CancelamentoException e) {
            view.mostrarMensagem("Operação cancelada.");
        } catch (EstadoInvalidoException e) {
            view.mostrarErro(e.getMessage());
        }
    }

    /**
     * Fecha o ano letivo com verificação rigorosa de notas e propinas.
     * Se existirem pendências, são apresentadas por categoria e o fecho é bloqueado.
     */
    private void fechar() {
        try {
            int ano = view.pedirAno("Ano letivo a fechar");
            bll.fechar(ano, view);
            view.mostrarSucesso("Ano letivo " + ano + " fechado e arquivado no histórico.");
        } catch (CancelamentoException e) {
            view.mostrarMensagem("Operação cancelada.");
        } catch (EstadoInvalidoException e) {
            // A BLL já exibiu as pendências via view antes de lançar a exceção;
            // aqui apenas apresentamos a mensagem de bloqueio resumida.
            view.mostrarErro(e.getMessage());
        }
    }

    private void avancar(GestorView gestorView) {
        try {
            bll.avancar(repo, gestorView);
            view.mostrarSucesso("Ano letivo avançado com sucesso.");
        } catch (EstadoInvalidoException e) {
            view.mostrarErro(e.getMessage());
        }
    }

    private void listar() {
        List<AnoLetivo> anos = bll.listar();
        view.mostrarListaAnos(anos);
    }

    private void estado() {
        List<String> resumo = bll.obterEstadoResumo();
        view.mostrarResumo(resumo);
    }

    /**
     * Mostra o histórico de anos letivos já fechados (arquivados).
     * Tarefa 6 do card Sprint 6.
     */
    private void verHistorico() {
        List<String> linhas = bll.obterHistoricoAnos();
        view.mostrarHistoricoAnos(linhas);
    }

    /**
     * Permite alterar o número de momentos de avaliação de uma UC
     * enquanto o ano letivo ainda está em PLANEAMENTO.
     * Tarefa 5 do card Sprint 6.
     */
    private void alterarMomentosUc() {
        try {
            String siglaUc = view.pedirSiglaUc("Sigla da UC a alterar");

            int momentosAtuais = bll.obterMomentosUc(siglaUc);
            view.mostrarMomentosUc(siglaUc, momentosAtuais);

            int novos = view.pedirNumeroMomentos();
            bll.alterarMomentosUc(siglaUc, novos);
            view.mostrarSucessoMomentos(siglaUc, novos);

        } catch (CancelamentoException e) {
            view.mostrarMensagem("Operação cancelada.");
        } catch (EstadoInvalidoException e) {
            view.mostrarErro(e.getMessage());
        }
    }
}

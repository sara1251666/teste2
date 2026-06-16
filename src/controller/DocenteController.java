package controller;

import common.ConfigApp;
import model.*;
import utils.Consola;
import view.DocenteView;
import bll.DocenteBLL;
import utils.CancelamentoException;

import java.util.ArrayList;
import java.util.List;
import dal.InscricaoDAL;
import dal.EstudanteDAL;
import dal.EstudanteDALFile;
import dal.EstudanteDALSql;
import dal.HistoricoDAL;
import dal.HistoricoDALFile;
import dal.HistoricoDALSql;
import model.Estudante;

/**
 * Controlador responsável por gerir as interações do Docente.
 * Atua como intermediário entre a interface (DocenteView) e a lógica de negócio (DocenteBLL).
 */
public class DocenteController {

    private final RepositorioDados repo;
    private final Docente docente;
    private final DocenteView view;
    private final DocenteBLL docenteBll;
    private final EstudanteDAL estudanteDAL = ConfigApp.isModoSql() ? new EstudanteDALSql() : new EstudanteDALFile();
    private final HistoricoDAL historicoDAL =
            ConfigApp.isModoSql() ? new HistoricoDALSql() : new HistoricoDALFile();

    public DocenteController(RepositorioDados repo, Docente docente) {
        this.repo        = repo;
        this.docente     = docente;
        this.view        = new DocenteView();
        this.docenteBll  = new DocenteBLL();
        this.historicoDAL.inicializar();
    }

    public void iniciar() {
        verificarMomentosPendentes();
        boolean correr = true;
        while (correr) {
            try {
                int opcao = view.mostrarMenu();
                switch (opcao) {
                    case 1: listarMeusAlunos(); break;
                    case 2: executarLancamentoNotas(); break;
                    case 3: executarLancamentoNotasLote(); break;
                    case 4: alterarPassword(); break;
                    case 5: verDadosPessoais(); break;
                    case 6: verMinhasUcs(); break;
                    case 7: consultarHistoricoAluno(); break;
                    case 8: definirMomentosAvaliacao(); break;
                    case 0:
                        view.mostrarDespedida();
                        repo.limparSessao();
                        correr = false;
                        break;
                    default:
                        view.mostrarOpcaoInvalida();
                }
            } catch (Exception e) {
                view.mostrarErroLeituraOpcao();
            }
        }
    }

    /**
     * Lista os alunos do docente com a respetiva média académica.
     * Toda a matemática e filtragem é feita na DocenteBLL.
     */

    private void listarMeusAlunos() {
        view.mostrarCabecalhoAlunos();
        List<Object[]> alunos = docenteBll.obterAlunosDoDocenteComMedia(docente);

        if (alunos.isEmpty()) {
            view.mostrarErroCarregarAlunos();
            return;
        }
        for (Object[] par : alunos) {
            Estudante e  = (Estudante) par[0];
            double media = (double)    par[1];
            String ucs   = (String)    par[2];

            view.mostrarAlunoComMedia(e.getNumeroMecanografico(), e.getNome(), media, ucs);
        }
    }

    /**
     * Mostra a ficha pessoal do docente autenticado.
     */
    private void verDadosPessoais() {
        view.mostrarFichaDocente(docente);
    }

    /**
     * Lista as Unidades Curriculares atribuídas ao docente.
     */
    private void verMinhasUcs() {
        view.mostrarUcsDocente(docente);
    }

    /**
     * Fluxo de recolha de uma única nota e envio para a DocenteBLL processar o registo.
     * Inclui validação de UC, limites de 3 avaliações e pertença ao docente.
     */
    private void executarLancamentoNotas() {
        view.mostrarCabecalhoLancamentoNotas();
        try {
            if (utils.Consola.lerSimNao("Deseja listar as suas Unidades Curriculares disponíveis?")) {
                verMinhasUcs();
            }
            String siglaUc = view.pedirSiglaUc();

            if (!docenteBll.lecionaEstaUC(docente, siglaUc)) {
                System.out.println("  [ERRO] Não leciona a UC '" + siglaUc + "'.");
                return;
            }

            if (utils.Consola.lerSimNao("Deseja listar os alunos inscritos em " + siglaUc + "?")) {
                listarAlunosPorUC(siglaUc);
            }

            int numMec = -1;
            boolean alunoValido = false;
            while (!alunoValido) {
                numMec = view.pedirNumeroAluno();
                if (estudanteDAL.procurarPorNumMec(numMec) != null) {
                    alunoValido = true;
                    break;
                } else {
                    System.out.println("  [ERRO] Aluno com nº " + numMec + " não encontrado. Tente novamente.");
                }
            }

            int anoAtivo = repo.getAnoAtual();
            System.out.println("  Ano Letivo: " + anoAtivo + " (Assumido pelo sistema)");

            // Obter e mostrar o número de momentos da UC
            int numMomentos = docenteBll.obterNumMomentosDaUC(siglaUc);
            view.mostrarNumMomentosDaUC(siglaUc, numMomentos);

            if (numMomentos > 1) {
                // UC com múltiplos momentos: pedir N notas e calcular a média
                List<Double> notas = new ArrayList<>();
                for (int momento = 1; momento <= numMomentos; momento++) {
                    double notaMomento = -1;
                    boolean notaValida = false;
                    while (!notaValida) {
                        notaMomento = view.pedirNotaPorMomento(momento, numMomentos);
                        if ((notaMomento >= 0 && notaMomento <= 20) || notaMomento == -1) {
                            notaValida = true;
                        } else {
                            System.out.println("  [ERRO] Nota inválida. Insira um valor entre 0 e 20 (ou -1 para falta).");
                        }
                    }
                    String erro = docenteBll.lancarNota(numMec, siglaUc, anoAtivo, notaMomento, docente);
                    if (erro != null) {
                        System.out.println("  >> " + erro);
                        break;
                    }
                    notas.add(notaMomento);
                }
                if (notas.size() == numMomentos) {
                    double notaFinal = docenteBll.calcularNotaFinal(notas);
                    view.mostrarNotaFinalCalculada(notaFinal);
                    view.mostrarSucessoLancamento();
                }
            } else {
                // UC com 1 momento: comportamento original
                double notaMomento = -1;
                boolean notaValida = false;
                while (!notaValida) {
                    notaMomento = view.pedirNotaMomento();
                    if ((notaMomento >= 0 && notaMomento <= 20) || notaMomento == -1) {
                        notaValida = true;
                    } else {
                        System.out.println("  [ERRO] Nota inválida. Insira um valor entre 0 e 20 (ou -1 para falta).");
                    }
                }
                String erro = docenteBll.lancarNota(numMec, siglaUc, anoAtivo, notaMomento, docente);
                if (erro != null) {
                    System.out.println("  >> " + erro);
                } else {
                    view.mostrarSucessoLancamento();
                }
            }

        } catch (utils.CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        } catch (Exception e) {
            view.mostrarErroLeituraOpcao();
        }
    }

    /**
     * Método auxiliar para listar apenas os alunos inscritos na UC selecionada.
     */
    private void listarAlunosPorUC(String siglaUC) {
        List<Object[]> todosAlunos = docenteBll.obterAlunosDoDocenteComMedia(docente);
        boolean encontrou = false;

        System.out.println("\n  --- Alunos Inscritos em " + siglaUC.toUpperCase() + " ---");
        for (Object[] par : todosAlunos) {
            model.Estudante e = (model.Estudante) par[0];
            String ucsInscritas = (String) par[2];

            if (ucsInscritas.toUpperCase().contains(siglaUC.toUpperCase())) {
                view.mostrarAlunoSimples(e.getNumeroMecanografico(), e.getNome());
                encontrou = true;
            }
        }

        if (!encontrou) {
            System.out.println("  Nenhum aluno encontrado inscrito nesta Unidade Curricular.");
        }
        System.out.println();
    }

    private void alterarPassword() {
        try {
            String novaPass = view.pedirNovaPassword();
            docenteBll.alterarPassword(docente, novaPass);
            view.mostrarSucessoAlteracaoPassword();
        } catch (CancelamentoException e) {
            view.mostrarCancelamentoPassword();
        }
    }

    private void executarLancamentoNotasLote() {
        view.mostrarCabecalhoLancamentoNotasLote();
        try {
            String siglaUc = view.pedirSiglaUc();
            if (!docenteBll.lecionaEstaUC(docente, siglaUc)) {
                view.mostrarErro("Não lecciona a UC " + siglaUc);
                return;
            }
            List<String> alunos = docenteBll.obterAlunosInscritosNaUc(siglaUc);
            if (alunos.isEmpty()) {
                view.mostrarErro("Nenhum aluno inscrito nesta UC.");
                return;
            }
            view.mostrarListaAlunosParaLote(siglaUc, alunos);
            if (!Consola.lerSimNao("Iniciar lançamento sequencial de notas para esta UC?")) {
                view.mostrarOperacaoCancelada();
                return;
            }
            int anoLetivo = view.pedirAnoLetivo();

            // Função que pergunta a nota para cada aluno
            java.util.function.Function<Integer, Double> obterNota = (numMec) -> {
                Estudante e = estudanteDAL.procurarPorNumMec(numMec);
                String nome = (e != null) ? e.getNome() : "Desconhecido";
                view.mostrarPedidoNotaParaAluno(numMec, nome);
                try {
                    double nota = view.pedirNotaMomento();
                    return nota;
                } catch (CancelamentoException ex) {
                    return null; // indica salto
                }
            };

            String resultado = docenteBll.lancarNotasEmLote(siglaUc, anoLetivo, docente, obterNota);
            view.mostrarResultadoLote(resultado);
        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        } catch (Exception e) {
            e.printStackTrace();
            view.mostrarErroLeituraOpcao();
        }
    }
    private void consultarHistoricoAluno() {
        try {
            int numMec = view.pedirNumeroAluno();

            // Pede também o ano ao Docente
            int ano = utils.Consola.lerInt("Introduza o Ano Letivo que deseja consultar");

            java.util.List<String> historico = historicoDAL.consultarHistoricoPorAluno(numMec);

            view.mostrarLinha("--- Histórico do Aluno " + numMec + " no Ano " + ano + " ---");
            boolean encontrou = false;
            for (String registo : historico) {
                String[] p = registo.split(";");
                // Filtra pelo ano escolhido
                if (p.length >= 5 && Integer.parseInt(p[0].trim()) == ano) {
                    view.mostrarLinha(String.format("UC: %-6s | Notas: %-15s | %s", p[2], p[3], p[4]));
                    encontrou = true;
                }
            }

            if (!encontrou) {
                view.mostrarLinha("Não foram encontrados registos para este ano.");
            }
            utils.Consola.pausar();
        } catch (utils.CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    /**
     * Mostra alerta no login se o ano está em PLANEAMENTO e o docente
     * tem UCs sem momentos de avaliação definidos.
     */
    private void verificarMomentosPendentes() {
        bll.AnoLetivoBLL anoBll = new bll.AnoLetivoBLL();
        if (anoBll.getEstadoAnoAtual() == model.EstadoAnoLetivo.PLANEAMENTO) {
            java.util.List<String> pendentes = docenteBll.obterUcsSemMomentos(docente);
            if (!pendentes.isEmpty()) {
                view.mostrarAlertaMomentosPendentes(pendentes);
            }
        }
    }

    /**
     * Permite ao docente definir o número de momentos de avaliação
     * para uma das suas UCs, apenas se o ano letivo estiver em PLANEAMENTO.
     */
    private void definirMomentosAvaliacao() {
        view.mostrarCabecalhoDefinirMomentos();
        if (docente.getTotalUcsLecionadas() == 0) {
            view.mostrarErro("Não leciona nenhuma UC.");
            return;
        }
        view.mostrarUcsParaDefinicao(docente);
        try {
            int escolha = Consola.lerInt("Selecione o número da UC");
            if (escolha < 1 || escolha > docente.getTotalUcsLecionadas()) {
                view.mostrarErro("Opção inválida.");
                return;
            }
            UnidadeCurricular uc = docente.getUcsLecionadas()[escolha - 1];
            int momentosAtuais = docenteBll.obterMomentosUc(uc.getSigla());
            view.mostrarMomentosAtuais(uc.getSigla(), momentosAtuais);
            int momentos = view.pedirNumeroMomentos();
            String erro = docenteBll.definirMomentosAvaliacao(docente, uc.getSigla(), momentos);
            if (erro == null) {
                view.mostrarSucessoMomentos(uc.getSigla(), momentos);
            } else {
                view.mostrarErro(erro);
            }
        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }
}
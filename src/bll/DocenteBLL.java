package bll;

import common.ConfigApp;
import dal.UcDALFile;
import dal.UcDALSql;

import dal.AvaliacaoDAL;
import dal.AvaliacaoDALFile;
import dal.AvaliacaoDALSql;
import dal.DocenteDAL;
import dal.DocenteDALFile;
import dal.DocenteDALSql;
import dal.EstudanteDAL;
import dal.EstudanteDALFile;
import dal.EstudanteDALSql;
import dal.UcDAL;
import dal.InscricaoDAL;
import dal.InscricaoDALFile;
import dal.InscricaoDALSql;
import model.*;
import controller.LoginController;
import utils.Config;
import java.util.ArrayList;
import java.util.List;


/**
 * Lógica de negócio para o perfil Docente.
 * Gere o lançamento de avaliações com todas as validações necessárias,
 * a obtenção dos alunos associados e a alteração segura de credenciais.
 */
public class DocenteBLL {

    private static final String PASTA_BD = ConfigApp.PASTA_BD;
    private final UcDAL ucDAL = ConfigApp.isModoSql() ? new UcDALSql() : new UcDALFile();
    private final LoginController loginController = new LoginController();
    private final EstudanteDAL estudanteDAL = ConfigApp.isModoSql() ? new EstudanteDALSql() : new EstudanteDALFile();
    private final DocenteDAL docenteDAL =
            ConfigApp.isModoSql() ? new DocenteDALSql() : new DocenteDALFile();
    private final InscricaoDAL inscricaoDAL =
            ConfigApp.isModoSql() ? new InscricaoDALSql() : new InscricaoDALFile();
    private final AvaliacaoDAL avaliacaoDAL =
            ConfigApp.isModoSql() ? new AvaliacaoDALSql() : new AvaliacaoDALFile();

    public DocenteBLL() {
        inscricaoDAL.inicializar();
        avaliacaoDAL.inicializar();
    }

    /**
     * Verifica se uma UC pertence ao plano de lecionação do docente.
     */
    public boolean lecionaEstaUC(Docente docente, String siglaUc) {
        if (siglaUc == null) return false;
        for (int i = 0; i < docente.getTotalUcsLecionadas(); i++) {
            if (docente.getUcsLecionadas()[i].getSigla().equalsIgnoreCase(siglaUc)) return true;
        }
        return false;
    }

    /**
     * Lança uma nota de forma faseada.
     * Se já existir avaliação, anexa a nova nota (até ao limite de 3).
     * Se não existir, cria a primeira.
     *
     * FIX (Bug DocenteBLL):
     *   - Linha ~27: "carregarAvaliacoesPorAluno" → "obterAvaliacoesPorAluno"  (nome errado na DAL)
     *   - Linha ~275: "adicionarAvaliacao(avaliacao)" → "adicionarAvaliacao(avaliacao, numMec, PASTA_BD)" (args em falta)
     */
    public String lancarNota(int numMec, String siglaUc, int ano, double notaMomento, Docente d) {
        Estudante aluno = estudanteDAL.procurarPorNumMec(numMec);

        AnoLetivoBLL anoBll = new AnoLetivoBLL();
        if (anoBll.getEstadoAnoAtual() == EstadoAnoLetivo.PLANEAMENTO) {
            return "ERRO: Não é possível lançar notas enquanto o ano letivo está em PLANEAMENTO.";
        }

        if (aluno == null)
            return "ERRO: Aluno com nº " + numMec + " não encontrado.";

        if (!lecionaEstaUC(d, siglaUc))
            return "ERRO: A UC '" + siglaUc + "' não pertence às suas unidades curriculares.";

        UnidadeCurricular uc = new UcBLL().procurarUCCompleta(siglaUc);
        if (uc == null)
            return "ERRO: A UC '" + siglaUc + "' não foi encontrada no sistema.";

        int numMomentos = uc.getNumMomentos(); // 1 por omissão; >1 se configurado

        Avaliacao avaliacaoExistente = avaliacaoDAL.obterAvaliacao(numMec, siglaUc, ano);

        if (avaliacaoExistente != null) {
            if (avaliacaoExistente.getTotalAvaliacoesLancadas() >= numMomentos) {
                return "ERRO: O aluno já tem as " + numMomentos
                        + " nota(s) máxima(s) lançadas para esta UC.";
            }
            avaliacaoExistente.adicionarResultado(notaMomento);
            avaliacaoDAL.atualizarAvaliacao(avaliacaoExistente, numMec);
            return null;

        } else {
            Avaliacao novaAvaliacao = new Avaliacao(uc, ano);
            novaAvaliacao.adicionarResultado(notaMomento);
            avaliacaoDAL.adicionarAvaliacao(novaAvaliacao, numMec);
            return null;
        }
    }

    /**
     * Calcula a nota final de uma UC como média simples dos momentos lançados.
     * @param notas Lista com as notas de cada momento.
     * @return Média aritmética simples, ou 0.0 se a lista estiver vazia.
     */
    public double calcularNotaFinal(List<Double> notas) {
        if (notas == null || notas.isEmpty()) return 0.0;
        double soma = 0;
        for (double nota : notas) soma += nota;
        return soma / notas.size();
    }

    /**
     * Devolve o número de momentos de avaliação configurados para uma UC.
     * @param siglaUc Sigla da UC.
     * @return Número de momentos (mínimo 1).
     */
    public int obterNumMomentosDaUC(String siglaUc) {
        UnidadeCurricular uc = new UcBLL().procurarUCCompleta(siglaUc);
        return (uc != null) ? uc.getNumMomentos() : 1;
    }

    /**
     * Altera a password do docente com hashing e persistência.
     */
    public void alterarPassword(Docente docente, String novaPass) {
        loginController.atualizarPassword(docente.getEmail(), novaPass);
    }

    /**
     * Devolve os alunos associados ao docente (das suas UCs) com a média e a lista de UCs.
     * Cada elemento da lista é um Object[] de 3 posições: [Estudante, Double média, String ucs].
     *
     * FIX (compile): método estava em falta — causava "cannot find symbol method
     * obterAlunosDoDocenteComMedia(model.Docente)" no DocenteController (linhas 67 e 163).
     *
     * FIX (runtime): a versão original devolvia apenas 2 elementos [Estudante, média],
     * mas o DocenteController acede a par[2] (string de UCs) em listarMeusAlunos() e
     * listarAlunosPorUC() — o que provocaria ArrayIndexOutOfBoundsException.
     * Agora devolve sempre 3 posições.
     *
     * @param docente Docente autenticado.
     * @return Lista de [Estudante, média (Double), UCs inscritas (String)].
     */
    public List<Object[]> obterAlunosDoDocenteComMedia(Docente docente) {
        List<Object[]> resultado = new ArrayList<>();
        List<Integer> alunosAdicionados = new ArrayList<>(); // evita duplicados
        int anoAtual = Config.getAnoAtual();

        for (int i = 0; i < docente.getTotalUcsLecionadas(); i++) {
            UnidadeCurricular uc = docente.getUcsLecionadas()[i];
            if (uc == null) continue;

            List<Integer> numsMec = inscricaoDAL.obterAlunosPorUc(uc.getSigla(), anoAtual);
            for (int numMec : numsMec) {
                if (contemAluno(alunosAdicionados, numMec)) continue;

                Estudante aluno = estudanteDAL.procurarPorNumMec(numMec);                if (aluno == null) continue;

                carregarAvaliacoesSeNecessario(aluno);
                double media = calcularMediaAlunoNaUc(aluno, uc.getSigla());

                // par[2]: string das UCs em que o aluno está inscrito (usada em .contains())
                List<String> siglasInscritas =
                        inscricaoDAL.obterSiglasUcsPorAluno(numMec, anoAtual);
                String ucs = String.join(", ", siglasInscritas);

                resultado.add(new Object[]{aluno, media, ucs});
                alunosAdicionados.add(numMec);
            }
        }
        return resultado;
    }

    /** Verifica se um número mecanográfico já está na lista (controlo de duplicados). */
    private boolean contemAluno(List<Integer> lista, int numMec) {
        for (int m : lista) {
            if (m == numMec) return true;
        }
        return false;
    }

    /**
     * Devolve a lista de alunos inscritos numa UC no ano corrente.
     */
    public List<String> obterAlunosInscritosNaUc(String siglaUc) {
        List<Integer> nums = inscricaoDAL.obterAlunosPorUc(siglaUc, Config.getAnoAtual());
        List<String> alunosFormatados = new ArrayList<>();
        for (int num : nums) {
            Estudante e = estudanteDAL.procurarPorNumMec(num);            String nome = (e != null) ? e.getNome() : "Desconhecido";
            alunosFormatados.add(num + " - " + nome);
        }
        return alunosFormatados;
    }

    /**
     * Lista todos os docentes (dados básicos, sem UCs carregadas).
     */
    public List<Docente> listarTodos() {
        return docenteDAL.carregarTodos();
    }

    /**
     * Obtém um docente pela sua sigla (com dados básicos).
     */
    public Docente obterPorSigla(String sigla) {
        return docenteDAL.procurarPorSigla(sigla);
    }

    /**
     * Actualiza os dados de um docente (nome, morada, dataNascimento, NIF).
     */
    public boolean atualizarDocente(Docente docente) {
        if (docente == null) return false;
        return docenteDAL.atualizarDocente(docente);
    }

    /**
     * Verifica se um docente tem UCs atribuídas.
     */
    public boolean temUcAtribuida(String sigla) {
        return docenteDAL.temUcAtribuida(sigla);
    }

    /**
     * Remove um docente (apenas se não tiver UCs atribuídas).
     */
    public boolean removerDocente(String sigla) {
        if (temUcAtribuida(sigla)) return false;
        return docenteDAL.removerDocente(sigla);
    }

    /**
     * Lança notas para todos os alunos inscritos numa UC, pedindo uma nota para cada um.
     * @return String com o relatório detalhado das operações.
     */
    public String lancarNotasEmLote(String siglaUc, int anoLetivo, Docente docente,
                                    java.util.function.Function<Integer, Double> obterNota) {

        AnoLetivoBLL anoBll = new AnoLetivoBLL();
        if (anoBll.getEstadoAnoAtual() == EstadoAnoLetivo.PLANEAMENTO) {
            return "ERRO: Não é possível lançar notas enquanto o ano letivo está em PLANEAMENTO.";
        }

        if (!lecionaEstaUC(docente, siglaUc)) {
            return "ERRO: Não lecciona a UC " + siglaUc;
        }
        UnidadeCurricular uc = new UcBLL().procurarUCCompleta(siglaUc);
        if (uc == null) return "ERRO: UC não encontrada.";

        List<Integer> alunosInscritos = inscricaoDAL.obterAlunosPorUc(siglaUc, anoLetivo);
        StringBuilder relatorio = new StringBuilder();
        int sucessos = 0, erros = 0;

        for (int numMec : alunosInscritos) {
            Estudante aluno = estudanteDAL.procurarPorNumMec(numMec);
            String nome = (aluno != null) ? aluno.getNome() : "Desconhecido";

            Double nota = obterNota.apply(numMec);
            if (nota == null) {
                relatorio.append(String.format(" %d - %s → Saltado pelo docente\n", numMec, nome));
                continue;
            }

            String resultado = lancarNota(numMec, siglaUc, anoLetivo, nota, docente);
            if (resultado == null) {
                sucessos++;
                relatorio.append(String.format("  %d - %s → Nota %.1f registada\n", numMec, nome, nota));
            } else {
                erros++;
                relatorio.append(String.format("  %d - %s → %s\n", numMec, nome, resultado));
            }
        }
        relatorio.insert(0, String.format("Resumo: %d sucessos, %d falhas, %d saltos.\n",
                sucessos, erros, alunosInscritos.size() - sucessos - erros));
        return relatorio.toString();
    }

    /**
     * Retorna uma lista de strings "numMec - nome" para os alunos inscritos numa UC.
     */
    public List<String> obterAlunosFormatados(String siglaUc, int anoLetivo) {
        List<Integer> nums = inscricaoDAL.obterAlunosPorUc(siglaUc, anoLetivo);
        List<String> formatados = new ArrayList<>();
        for (int num : nums) {
            Estudante e = estudanteDAL.procurarPorNumMec(num);
            String nome = (e != null) ? e.getNome() : "Desconhecido";
            formatados.add(num + " - " + nome);
        }
        return formatados;
    }

    // ── Métodos privados ──────────────────────────────────────────────

    /**
     * Calcula a média de um aluno numa UC específica.
     * FIX: usava "carregarAvaliacoesPorAluno" → correto é "obterAvaliacoesPorAluno".
     */
    private double calcularMediaAlunoNaUc(Estudante aluno, String siglaUc) {
        for (int i = 0; i < aluno.getPercurso().getTotalAvaliacoes(); i++) {
            Avaliacao av = aluno.getPercurso().getHistoricoAvaliacoes()[i];
            if (av != null && av.getUc() != null && av.getUc().getSigla().equalsIgnoreCase(siglaUc)) {
                return av.calcularMedia();
            }
        }
        return 0.0;
    }

    /**
     * Carrega as avaliações de um estudante se ainda não estiverem carregadas.
     * FIX: era "AvaliacaoDAL.carregarAvaliacoesPorAluno" → correto: "obterAvaliacoesPorAluno".
     */
    private void carregarAvaliacoesSeNecessario(Estudante aluno) {
        if (aluno.getPercurso().getTotalAvaliacoes() == 0) {
            // FIX: nome correto do método na DAL é obterAvaliacoesPorAluno
            List<Avaliacao> avaliacoes = avaliacaoDAL.obterAvaliacoesPorAluno(
                    aluno.getNumeroMecanografico());
            for (Avaliacao av : avaliacoes) {
                aluno.getPercurso().registarAvaliacao(av);
            }
        }
    }

    /**
     * Define o número de momentos de avaliação para uma unidade curricular.
     * Apenas permitido quando o ano letivo ativo está em PLANEAMENTO.
     */
    public String definirMomentosAvaliacao(Docente docente, String siglaUc, int numMomentos) {
        AnoLetivoBLL anoBll = new AnoLetivoBLL();
        if (anoBll.getEstadoAnoAtual() != EstadoAnoLetivo.PLANEAMENTO) {
            return "Apenas é permitido definir momentos de avaliação quando o ano letivo está em PLANEAMENTO.";
        }
        if (!lecionaEstaUC(docente, siglaUc)) {
            return "Não leciona a UC " + siglaUc;
        }
        if (numMomentos < 1 || numMomentos > 3) {
            return "Número de momentos inválido. Deve ser 1, 2 ou 3.";
        }
        ucDAL.atualizarMomentos(siglaUc, numMomentos, PASTA_BD);
        return null;
    }

    /**
     * Devolve a lista de UCs do docente que ainda não têm momentos definidos
     * (momentos == 0). Usado para mostrar alerta no login quando o ano está
     * em PLANEAMENTO.
     */
    public List<String> obterUcsSemMomentos(Docente docente) {
        List<String> pendentes = new java.util.ArrayList<>();
        UnidadeCurricular[] ucs = docente.getUcsLecionadas();
        for (int i = 0; i < docente.getTotalUcsLecionadas(); i++) {
            UnidadeCurricular uc = ucs[i];
            if (uc != null && ucDAL.obterMomentos(uc.getSigla(), PASTA_BD) == 0) {
                pendentes.add(uc.getSigla() + " - " + uc.getNome());
            }
        }
        return pendentes;
    }

    /**
     * Devolve o número de momentos atualmente definidos para a UC.
     * 0 = ainda não definido.
     */
    public int obterMomentosUc(String siglaUc) {
        return ucDAL.obterMomentos(siglaUc, PASTA_BD);
    }
}
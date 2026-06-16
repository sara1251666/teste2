package bll;

import common.ConfigApp;

import dal.AvaliacaoDAL;
import dal.AvaliacaoDALFile;
import dal.AvaliacaoDALSql;
import dal.CredencialDAL;
import dal.EstudanteDAL;
import dal.EstudanteDALFile;
import dal.EstudanteDALSql;
import dal.InscricaoDAL;
import dal.InscricaoDALFile;
import dal.InscricaoDALSql;
import dal.PagamentoDAL;
import dal.PagamentoDALFile;
import dal.PagamentoDALSql;
import model.Avaliacao;
import model.Estudante;
import model.Pagamento;
import model.UnidadeCurricular;
import controller.LoginController;
import utils.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * Lógica de negócio para o perfil Estudante.
 */
public class EstudanteBLL {

    private static final String PASTA_BD = ConfigApp.PASTA_BD;
    private final LoginController loginController = new LoginController();

    // Instanciação da DAL com o caminho estipulado
    private final EstudanteDAL dal = ConfigApp.isModoSql() ? new EstudanteDALSql() : new EstudanteDALFile();

    private final InscricaoDAL inscricaoDAL =
            ConfigApp.isModoSql() ? new InscricaoDALSql() : new InscricaoDALFile();
    private final AvaliacaoDAL avaliacaoDAL =
            ConfigApp.isModoSql() ? new AvaliacaoDALSql() : new AvaliacaoDALFile();
    private final PagamentoDAL pagamentoDAL =
            ConfigApp.isModoSql() ? new PagamentoDALSql() : new PagamentoDALFile();

    public EstudanteBLL() {
        inscricaoDAL.inicializar();
        avaliacaoDAL.inicializar();
        pagamentoDAL.inicializar();
    }

    /** Carrega o perfil completo de um estudante após login. */
    public Estudante obterPerfilCompleto(String email, String hash) {
        Estudante e = dal.carregarPerfil(email, hash);
        if (e == null) return null;

        carregarInscricoes(e);
        carregarAvaliacoes(e);
        carregarHistoricoPagamentos(e);
        return e;
    }

    /** Devolve todos os estudantes com dados básicos. */
    public List<Estudante> obterTodos() {
        return dal.carregarTodos();
    }

    /** Mantido por retrocompatibilidade. Faz o mesmo que obterTodos(). */
    public List<Estudante> listarTodos() {
        return dal.carregarTodos();
    }

    /** Devolve todos os estudantes com percurso académico completo. */
    public List<Estudante> carregarTodosCompleto() {
        List<Estudante> base = dal.carregarTodos();
        List<Estudante> hidratados = new ArrayList<>();

        for (Estudante e : base) {
            if (e == null) continue;
            carregarInscricoes(e);
            carregarAvaliacoes(e);
            hidratados.add(e);
        }
        return hidratados;
    }

    /** Obtém um estudante pelo número mecanográfico. */
    public Estudante obterPorNumMec(int numMec) {
        return dal.procurarPorNumMec(numMec);
    }

    /** Atualiza a morada do estudante e persiste a alteração. */
    public void atualizarMorada(Estudante estudante, String novaMorada) {
        estudante.setMorada(novaMorada);
        dal.atualizarEstudante(estudante);
    }

    /** Actualiza os dados de um estudante e persiste. */
    public boolean atualizarEstudante(Estudante estudante) {
        if (estudante == null) return false;
        dal.atualizarEstudante(estudante);
        return true;
    }

    /** Altera a password do estudante com hashing e persistência. */
    public void alterarPassword(Estudante estudante, String novaPass) {
        loginController.atualizarPassword(estudante.getEmail(), novaPass);
    }

    /** Calcula o próximo número mecanográfico disponível. */
    public int obterProximoNumeroMecanografico(int anoAtual) {
        return dal.obterProximoNumeroMecanografico(anoAtual);
    }

    /**
     * Remove um estudante e todos os seus dados associados.
     * ORQUESTRAÇÃO NA BLL (Boa Prática de Arquitetura).
     */
    public boolean removerEstudante(int numMec) {
        Estudante e = dal.procurarPorNumMec(numMec);
        if (e == null) return false;

        // Remove apenas inscrições activas e credencial primeiro
        inscricaoDAL.removerInscricoesPorAluno(numMec);
        CredencialDAL.removerCredencial(e.getEmail(), PASTA_BD);

        // Remove o estudante no ficheiro estudantes.csv
        return dal.removerEstudante(numMec);
    }

    // ==================================================================
    // Lógica Privada de Hidratação de Dados
    // ==================================================================

    private void carregarHistoricoPagamentos(Estudante e) {
        List<Pagamento> pagamentos =
                pagamentoDAL.carregarPagamentosPorAluno(e.getNumeroMecanografico());
        for (Pagamento p : pagamentos) {
            e.adicionarPagamento(p);
        }
    }

    private void carregarInscricoes(Estudante e) {
        int anoAtual = Config.getAnoAtual();
        List<String> siglas = inscricaoDAL.obterSiglasUcsPorAluno(
                e.getNumeroMecanografico(), anoAtual);
        for (String sigla : siglas) {
            UnidadeCurricular uc = new UcBLL().procurarUCCompleta(sigla);
            if (uc != null) e.getPercurso().inscreverEmUc(uc);
        }
    }

    private void carregarAvaliacoes(Estudante e) {
        List<Avaliacao> avaliacoes =
                avaliacaoDAL.obterAvaliacoesPorAluno(e.getNumeroMecanografico());
        for (Avaliacao av : avaliacoes) {
            e.getPercurso().registarAvaliacao(av);
        }
    }

    // ==================================================================
    // Lógica de Apresentação (View Helpers)
    // ==================================================================

    public String obterInfoInscricoes(Estudante e) {
        StringBuilder sb = new StringBuilder();
        if (e.getAnoCurricular() >= 4) {
            sb.append("Curso Concluído\n");
        } else {
            sb.append("Ano Curricular: ").append(e.getAnoCurricular()).append("º\n");
        }
        sb.append("UCs inscrito:\n");
        UnidadeCurricular[] ucs = e.getPercurso().getUcsInscrito();
        int total = e.getPercurso().getTotalUcsInscrito();
        if (total == 0) {
            sb.append("  Nenhuma UC encontrada.\n");
        } else {
            for (int i = 0; i < total; i++) {
                sb.append("  - ").append(ucs[i].getSigla()).append(": ").append(ucs[i].getNome()).append("\n");
            }
        }
        return sb.toString();
    }

    public String obterNotasDoEstudante(Estudante e) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Notas do aluno: %s (%d)\n", e.getNome(), e.getNumeroMecanografico()));
        sb.append("------------------------------------------------------------\n");

        int totalAvaliacoes = e.getPercurso().getTotalAvaliacoes();
        if (totalAvaliacoes == 0) {
            sb.append("Nenhuma avaliação registada.\n");
            return sb.toString();
        }

        Avaliacao[] historico = e.getPercurso().getHistoricoAvaliacoes();
        for (int i = 0; i < totalAvaliacoes; i++) {
            Avaliacao av = historico[i];
            if (av == null || av.getUc() == null) continue;

            String sigla = av.getUc().getSigla();
            String nomeUC = av.getUc().getNome();
            int ano = av.getAnoLetivo();
            double[] notas = av.getResultados();
            int totalNotas = av.getTotalAvaliacoesLancadas();

            sb.append(String.format("UC: %s - %s (Ano %d)\n", sigla, nomeUC, ano));
            if (totalNotas == 0) {
                sb.append("  Sem notas lançadas.\n");
            } else {
                sb.append("  Notas: ");
                for (int j = 0; j < totalNotas; j++) {
                    sb.append(String.format("%.1f", notas[j]));
                    if (j < totalNotas - 1) sb.append(", ");
                }
                double media = av.calcularMedia();
                sb.append(String.format(" | Média: %.1f | %s\n", media, av.isAprovado() ? "APROVADO" : "REPROVADO"));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
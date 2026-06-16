package bll;

import common.ConfigApp;
import dal.*;
import model.*;
import utils.Config;
import java.util.*;

public class InscricaoBLL {

    private static final String PASTA_BD = Config.PASTA_BD;
    private final UcDAL ucDAL = ConfigApp.isModoSql() ? new UcDALSql() : new UcDALFile();
    private static final double NOTA_MINIMA_APROVACAO = 9.5;

    private final EstudanteDAL estudanteDAL = ConfigApp.isModoSql() ? new EstudanteDALSql() : new EstudanteDALFile();
    private final InscricaoDAL inscricaoDAL =
            ConfigApp.isModoSql() ? new InscricaoDALSql() : new InscricaoDALFile();
    private final AvaliacaoDAL avaliacaoDAL =
            ConfigApp.isModoSql() ? new AvaliacaoDALSql() : new AvaliacaoDALFile();

    public InscricaoBLL() {
        inscricaoDAL.inicializar();
        avaliacaoDAL.inicializar();
    }

    public OperationResult transitarAlunos(AnoLetivo anoAtual, AnoLetivo anoSeguinte) {
        List<Estudante> todos = new EstudanteBLL().carregarTodosCompleto();
        if (todos == null || todos.isEmpty()) {
            return OperationResult.sucesso("Nenhum estudante encontrado.");
        }

        Map<Estudante, Integer> novoAnoMap = new HashMap<>();
        Map<Estudante, List<String>> novasInscricoesMap = new HashMap<>();
        List<String> alunosBloqueados = new ArrayList<>();

        for (Estudante e : todos) {
            if (e.getAnoCurricular() > 3) continue;

            int anoAtualCurricular = e.getAnoCurricular();

            List<String> ucsInscritas = inscricaoDAL.obterSiglasUcsPorAluno(
                    e.getNumeroMecanografico(), anoAtual.getAno());

            List<String> aprovadas = new ArrayList<>();
            List<String> reprovadas = new ArrayList<>();
            for (String sigla : ucsInscritas) {
                Avaliacao av = avaliacaoDAL.obterAvaliacao(e.getNumeroMecanografico(), sigla, anoAtual.getAno());
                if (av != null && av.isAprovado()) {
                    aprovadas.add(sigla);
                } else {
                    reprovadas.add(sigla);
                }
            }

            double percentagem = ucsInscritas.isEmpty() ? 0.0 : (double) aprovadas.size() / ucsInscritas.size();

            List<String> ucsParaInscrever = new ArrayList<>();
            int novoAno = anoAtualCurricular;

            if (percentagem >= 0.6) {
                if (anoAtualCurricular < 3) {
                    novoAno = anoAtualCurricular + 1;
                    ucsParaInscrever.addAll(ucDAL.obterSiglasUcsPorCursoEAno(e.getSiglaCurso(), novoAno, PASTA_BD));
                    ucsParaInscrever.addAll(reprovadas);
                } else {
                    List<String> ucsTerceiroAno = ucDAL.obterSiglasUcsPorCursoEAno(e.getSiglaCurso(), 3, PASTA_BD);
                    boolean aprovouTodas = !ucsTerceiroAno.isEmpty() && ucsTerceiroAno.stream().allMatch(aprovadas::contains);
                    if (aprovouTodas) {
                        novoAno = 4;
                    } else {
                        novoAno = 3;
                        ucsParaInscrever.addAll(reprovadas);
                    }
                }
            } else {

                novoAno = anoAtualCurricular;
                ucsParaInscrever.addAll(reprovadas);
            }

            List<String> semDuplicados = new ArrayList<>();
            for (String uc : ucsParaInscrever) {
                if (!semDuplicados.contains(uc)) semDuplicados.add(uc);
            }

            novoAnoMap.put(e, novoAno);
            novasInscricoesMap.put(e, semDuplicados);
        }

        if (!alunosBloqueados.isEmpty()) {
            StringBuilder msg = new StringBuilder("Existem alunos sem aproveitamento mínimo (60%):\n");
            for (String bloco : alunosBloqueados) {
                msg.append("  - ").append(bloco).append("\n");
            }
            msg.append("Transição cancelada. Corrija as notas dos alunos ou remova os bloqueios.");
            return OperationResult.falha(msg.toString());
        }

        try {
            for (Map.Entry<Estudante, List<String>> entry : novasInscricoesMap.entrySet()) {
                Estudante e = entry.getKey();
                int novoAno = novoAnoMap.get(e);
                List<String> ucs = entry.getValue();

                e.setAnoCurricular(novoAno);

                estudanteDAL.atualizarEstudante(e);

                inscricaoDAL.removerInscricoesPorAluno(e.getNumeroMecanografico());
                for (String sigla : ucs) {
                    inscricaoDAL.adicionarInscricao(e.getNumeroMecanografico(), sigla, anoSeguinte.getAno());
                }
            }
            return OperationResult.sucesso("Transição concluída para " + novasInscricoesMap.size() + " alunos.");
        } catch (Exception ex) {
            return OperationResult.falha("Erro ao persistir: " + ex.getMessage());
        }
    }
}
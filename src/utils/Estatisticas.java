package utils;

import bll.EstudanteBLL;
import model.Avaliacao;
import model.Estudante;

import java.util.List;

/**
 * Motor de cálculo de estatísticas institucionais.
 * Produz dados numéricos brutos para a GestorBLL apresentar via GestorView.
 */
public class Estatisticas {

    private Estatisticas() {}

    /**
     * Calcula a soma total de notas e o número de momentos lançados.
     * @return Array [soma, total] para calcular a média global;
     *         null se não existirem estudantes; {0, 0} se não houver notas.
     */
    public static double[] calcularDadosMediaGlobal() {
        List<Estudante> estudantes = new EstudanteBLL().carregarTodosCompleto();
        if (estudantes == null || estudantes.isEmpty()) return null;

        double soma    = 0;
        int totalNotas = 0;

        for (Estudante e : estudantes) {
            if (e == null) continue;
            for (int i = 0; i < e.getPercurso().getTotalAvaliacoes(); i++) {
                Avaliacao av = e.getPercurso().getHistoricoAvaliacoes()[i];
                if (av == null) continue;
                for (int j = 0; j < av.getTotalAvaliacoesLancadas(); j++) {
                    soma += av.getResultados()[j];
                    totalNotas++;
                }
            }
        }

        return new double[]{soma, totalNotas};
    }

    /**
     * Determina o estudante com a maior média académica global.
     * @return Array [Estudante, Double] com o melhor aluno e a sua média;
     *         null se não houver alunos com avaliações lançadas.
     */
    public static Object[] calcularMelhorAluno() {
        List<Estudante> estudantes = new EstudanteBLL().carregarTodosCompleto();
        if (estudantes == null || estudantes.isEmpty()) return null;

        Estudante melhor     = null;
        double    maiorMedia = -1;

        for (Estudante e : estudantes) {
            if (e == null || e.getPercurso().getTotalAvaliacoes() == 0) continue;

            double somaMedias = 0;
            int    total      = e.getPercurso().getTotalAvaliacoes();

            for (int i = 0; i < total; i++) {
                Avaliacao av = e.getPercurso().getHistoricoAvaliacoes()[i];
                if (av != null) somaMedias += av.calcularMedia();
            }

            double mediaAluno = somaMedias / total;
            if (mediaAluno > maiorMedia) {
                maiorMedia = mediaAluno;
                melhor     = e;
            }
        }

        return melhor != null ? new Object[]{melhor, maiorMedia} : null;
    }
}
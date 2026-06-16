package model;

/**
 * Regista as notas obtidas por um estudante numa unidade curricular.
 * Suporta até três momentos de avaliação: época normal, recurso e especial.
 * O estudante é considerado aprovado se obtiver pelo menos 9,5 em qualquer época.
 */
public class Avaliacao {

    private UnidadeCurricular uc;
    private int anoLetivo;
    private double[] resultados;
    private int totalAvaliacoesLancadas;

    /**
     * Cria um registo de avaliação para uma UC num dado ano letivo.
     * @param uc        Unidade curricular avaliada.
     * @param anoLetivo Ano em que a avaliação foi realizada.
     */
    public Avaliacao(UnidadeCurricular uc, int anoLetivo) {
        this.uc = uc;
        this.anoLetivo = anoLetivo;
        this.resultados = new double[3]; // Representa Época Normal, Recurso e Especial
        this.totalAvaliacoesLancadas = 0;
    }

    // ---------- MÉTODOS DE INTEGRIDADE E LÓGICA ----------

    /**
     * Regista uma nova nota nesta avaliação.
     * @param nota Valor da nota (0 a 20).
     * @return true se a nota foi guardada; false se o limite de 3 momentos foi atingido.
     */
    public void adicionarResultado(double nota) {
        if (totalAvaliacoesLancadas < 3) {
            this.resultados[totalAvaliacoesLancadas] = nota;
            this.totalAvaliacoesLancadas++;
        }
    }

    /**
     * Calcula a média aritmética de todas as tentativas realizadas pelo aluno nesta UC.
     * @return A média das notas lançadas.
     */
    public double calcularMedia() {
        if (totalAvaliacoesLancadas == 0) return 0.0;

        double soma = 0;
        for (int i = 0; i < totalAvaliacoesLancadas; i++) {
            soma += resultados[i];
        }
        return soma / totalAvaliacoesLancadas;
    }

    /**
     * Verifica se o estudante obteve aprovação nesta UC.
     * @return true se pelo menos uma nota for igual ou superior a 9,5.
     */
    public boolean isAprovado() {
        for (int i = 0; i < totalAvaliacoesLancadas; i++) {
            if (resultados[i] >= 9.5) { // Lógica académica de arredondamento
                return true;
            }
        }
        return false;
    }

    // ---------- GETTERS ----------

    /** @return UC a que esta avaliação se refere. */
    public UnidadeCurricular getUc() { return uc; }

    /** @return Ano letivo em que a avaliação foi realizada. */
    public int getAnoLetivo() { return anoLetivo; }

    /** @return Array com todas as notas lançadas. */
    public double[] getResultados() { return resultados; }

    /** @return Número de momentos de avaliação lançados. */
    public int getTotalAvaliacoesLancadas() { return totalAvaliacoesLancadas; }

    // ---------- SETTERS ----------

    /** @param uc Nova UC associada. */
    public void setUc(UnidadeCurricular uc) { this.uc = uc; }

    /** @param anoLetivo Novo ano letivo. */
    public void setAnoLetivo(int anoLetivo) { this.anoLetivo = anoLetivo; }
}
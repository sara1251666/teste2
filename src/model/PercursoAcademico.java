package model;

/**
 * Historial académico de um estudante ao longo da sua vida na instituição.
 * Mantém as inscrições ativas em unidades curriculares e o arquivo
 * permanente de avaliações.
 * Contém a lógica de cálculo do aproveitamento necessário para progressão de ano.
 */
public class PercursoAcademico {

    // ---------- ATRIBUTOS ----------
    private UnidadeCurricular[] ucsInscrito;
    private int totalUcsInscrito;

    private final Avaliacao[] historicoAvaliacoes;
    private int totalAvaliacoes;


    // ---------- CONSTRUTOR ----------


    /** Cria um percurso académico vazio. */
    public PercursoAcademico() {
        this.ucsInscrito = new UnidadeCurricular[15];
        this.totalUcsInscrito = 0;
        this.historicoAvaliacoes = new Avaliacao[100];
        this.totalAvaliacoes  = 0;
    }

    // ---------- MÉTODOS DE LÓGICA E INTEGRIDADE ----------

     /**
     * Inscreve o estudante numa unidade curricular, ignorando duplicados.
     * @param uc UC em que o estudante passa a estar inscrito.
     */
    public void inscreverEmUc(UnidadeCurricular uc) {
        for (int i = 0; i < totalUcsInscrito; i++) {
            if (ucsInscrito[i].getSigla().equals(uc.getSigla())) return;
        }
        if (totalUcsInscrito < ucsInscrito.length) {
            ucsInscrito[totalUcsInscrito] = uc;
            totalUcsInscrito++;
        }
    }


    /**
     * Adiciona uma avaliação ao historial permanente do estudante.
     * @param avaliacao Avaliação a arquivar.
     */
    public void registarAvaliacao(Avaliacao avaliacao) {
        if (totalAvaliacoes < historicoAvaliacoes.length) {
            historicoAvaliacoes[totalAvaliacoes] = avaliacao;
            totalAvaliacoes++;
        }
    }

    /**
     * Verifica se o estudante tem aproveitamento suficiente para transitar de ano.
     * A transição requer que pelo menos 60% das UCs inscritas tenham nota positiva.
     * @return true se a taxa de aprovação for igual ou superior a 60%.
     */
    public boolean temAproveitamentoSuficiente() {
        if (totalUcsInscrito == 0) return false;

        int aprovadas = 0;
        for (int i = 0; i < totalUcsInscrito; i++) {
            if (ucsInscrito[i] == null) continue;
            String siglaUc = ucsInscrito[i].getSigla();
            for (int j = 0; j < totalAvaliacoes; j++) {
                Avaliacao av = historicoAvaliacoes[j];
                if (av != null && av.getUc() != null
                        && av.getUc().getSigla().equalsIgnoreCase(siglaUc)
                        && av.isAprovado()) {
                    aprovadas++;
                    break;
                }
            }
        }
        return (double) aprovadas / totalUcsInscrito >= 0.60;
    }

    /**
     * Calcula a percentagem de aproveitamento atual entre 0.0 e 1.0.
     * @return Rácio de UCs aprovadas face ao total inscrito.
     */
    public double calcularPercentagemAproveitamento() {
        if (totalUcsInscrito == 0) return 0.0;
        int aprovadas = 0;
        for (int i = 0; i < totalUcsInscrito; i++) {
            if (ucsInscrito[i] == null) continue;
            String siglaUc = ucsInscrito[i].getSigla();
            for (int j = 0; j < totalAvaliacoes; j++) {
                Avaliacao av = historicoAvaliacoes[j];
                if (av != null && av.getUc() != null
                        && av.getUc().getSigla().equalsIgnoreCase(siglaUc)
                        && av.isAprovado()) {
                    aprovadas++;
                    break;
                }
            }
        }
        return (double) aprovadas / totalUcsInscrito;
    }

    /**
     * Remove todas as inscrições ativas, preservando o historial de avaliações.
     * Chamado na transição de ano antes de carregar as novas inscrições.
     */
    public void limparInscricoesAtivas() {
        this.ucsInscrito      = new UnidadeCurricular[15];
        this.totalUcsInscrito = 0;
    }

    public UnidadeCurricular[] getUcsInscrito() { return ucsInscrito; }
    public int getTotalUcsInscrito() { return totalUcsInscrito; }

    /** @return Array com o historial completo de avaliações do estudante. */
    public Avaliacao[] getHistoricoAvaliacoes() { return historicoAvaliacoes; }


    /** @return Número total de avaliações arquivadas. */
    public int         getTotalAvaliacoes()      { return totalAvaliacoes; }}


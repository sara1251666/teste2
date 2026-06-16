package model;

/**
 * Representa um ano letivo no sistema (ex: 2026).
 * O ano é a chave única; o estado controla o ciclo de vida.
 * Apenas a BLL deve alterar o estado.
 */
public class AnoLetivo {

    private final int ano;
    private EstadoAnoLetivo estado;

    /**
     * Cria um novo AnoLetivo em estado PLANEAMENTO por omissão.
     * @param ano O ano letivo
     */
    public AnoLetivo(int ano) {
        this.ano = ano;
        this.estado = EstadoAnoLetivo.PLANEAMENTO;
    }

    /**
     * Cria um AnoLetivo com estado específico.
     * Usado pela DAL ao reconstruir o objeto a partir do CSV.
     * @param ano    O ano letivo
     * @param estado O estado atual
     */
    public AnoLetivo(int ano, EstadoAnoLetivo estado) {
        this.ano = ano;
        this.estado = estado;
    }

    // ---------- GETTERS ----------

    /** @return O ano letivo. */
    public int getAno() { return ano; }

    /** @return O estado atual no ciclo de vida. */
    public EstadoAnoLetivo getEstado() { return estado; }

    // ---------- SETTERS ----------

    /** @param estado Novo estado. Validação de transição é responsabilidade da BLL. */
    public void setEstado(EstadoAnoLetivo estado) { this.estado = estado; }

    // ---------- SERIALIZAÇÃO ----------

    /**
     * Converte para uma linha CSV no formato: ano;ESTADO
     * @return Linha pronta a escrever no ficheiro.
     */
    public String toCSV() {
        return ano + ";" + estado.name();
    }

    @Override
    public String toString() {
        return "AnoLetivo " + ano + " [" + estado + "]";
    }
}
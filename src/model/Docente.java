package model;

/**
 * Representa um docente do sistema.
 * Para além dos dados pessoais herdados de Utilizador, mantém
 * a sigla de identificação e a lista de unidades curriculares lecionadas.
 */
public class Docente extends Utilizador {

    private String sigla;
    private final UnidadeCurricular[] ucsLecionadas;
    private int totalUcsLecionadas;


    /**
     * Cria um docente com todos os dados.
     * @param sigla          Sigla de 3 caracteres gerada automaticamente.
     * @param email          Email institucional.
     * @param password       Hash PBKDF2 da palavra-chave.
     * @param nome           Nome completo.
     * @param nif            NIF com 9 dígitos.
     * @param morada         Morada de residência.
     * @param dataNascimento Data de nascimento (DD-MM-AAAA).
     */
    public Docente(String sigla, String email, String password,
                   String nome, String nif, String morada, String dataNascimento) {
        super(email, password, nome, nif, morada, dataNascimento);
        this.sigla             = sigla;
        this.ucsLecionadas     = new UnidadeCurricular[20];
        this.totalUcsLecionadas = 0;
    }

    // --- Getters ---

    /** @return Sigla identificadora do docente (ex.: "JDO"). */
    public String getSigla(){ return sigla; }

    /** @return Array das unidades curriculares atribuídas a este docente. */
    public UnidadeCurricular[] getUcsLecionadas(){ return ucsLecionadas; }

    /** @return Número de UCs atualmente atribuídas. */
    public int getTotalUcsLecionadas(){ return totalUcsLecionadas; }

    // --- Setters ---

    /** @param sigla Nova sigla do docente. */
    public void setSigla(String sigla){ this.sigla = sigla; }

    /**
     * Associa uma unidade curricular a este docente.
     * Ignorada se o limite máximo de UCs for atingido.
     * @param uc Unidade curricular a adicionar.
     */
    public void adicionarUcLecionada(UnidadeCurricular uc) {
        if (totalUcsLecionadas < ucsLecionadas.length) {
            ucsLecionadas[totalUcsLecionadas] = uc;
            totalUcsLecionadas++;
        }
    }
}
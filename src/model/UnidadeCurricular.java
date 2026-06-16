package model;


/**
 * Representa uma Unidade Curricular do sistema.
 * Uma UC pode pertencer a vários cursos em simultâneo.
 * Todas as UCs partilham o mesmo valor de ECTS definido por ECTS_PADRAO.
 */
public class UnidadeCurricular {

    /** Valor de créditos ECTS atribuído a todas as unidades curriculares. */
    public static final int ECTS_PADRAO = 6;

    // ---------- ATRIBUTOS ----------
    private String sigla;
    private String nome;
    private int anoCurricular;
    private int ects;
    private Docente docenteResponsavel;
    /** Número de momentos de avaliação (1 = comportamento original; 2 = média de 2 notas). */
    private int numMomentos;

    private final Curso[] cursos;
    private int totalCursos;


    /**
     * Cria uma UC com ECTS explícito.
     * @param sigla              Sigla identificadora (ex.: "POO").
     * @param nome               Nome completo da UC.
     * @param anoCurricular      Ano do plano de estudos em que é lecionada (1, 2 ou 3).
     * @param docenteResponsavel Docente responsável pela UC.
     * @param ects               Número de créditos ECTS atribuídos.
     */
    public UnidadeCurricular(String sigla, String nome, int anoCurricular,
                             Docente docenteResponsavel, int ects) {
        this.sigla              = sigla;
        this.nome               = nome;
        this.anoCurricular      = anoCurricular;
        this.docenteResponsavel = docenteResponsavel;
        this.ects               = ects;
        this.numMomentos        = 1;
        this.cursos             = new Curso[10];
        this.totalCursos        = 0;
    }


    /**
     * Cria uma UC com o valor padrão de ECTS.
     * @param sigla              Sigla identificadora.
     * @param nome               Nome completo.
     * @param anoCurricular      Ano curricular (1, 2 ou 3).
     * @param docenteResponsavel Docente responsável.
     */
    public UnidadeCurricular(String sigla, String nome, int anoCurricular,
                             Docente docenteResponsavel) {
        this(sigla, nome, anoCurricular, docenteResponsavel, ECTS_PADRAO);
    }

    // ---------- GETTERS ----------

    /** @return Sigla da UC. */
    public String getSigla(){ return sigla; }

    /** @return Nome completo da UC. */
    public String getNome(){ return nome; }

    /** @return Ano curricular em que a UC é lecionada. */
    public int getAnoCurricular(){ return anoCurricular; }

    /** @return Créditos ECTS atribuídos. */
    public int getEcts(){ return ects; }

    /** @return Docente responsável pela UC. */
    public Docente getDocenteResponsavel(){ return docenteResponsavel; }

    /** @return Número de momentos de avaliação configurados para esta UC (1 por omissão). */
    public int getNumMomentos(){ return numMomentos; }

    /** @return Array dos cursos aos quais a UC está associada. */
    public Curso[] getCursos(){ return cursos; }

    // ---------- SETTERS ----------

    /** @param sigla Nova sigla. */
    public void setSigla(String sigla)              { this.sigla = sigla; }

    /** @param nome Novo nome. */
    public void setNome(String nome)                { this.nome = nome; }

    /** @param anoCurricular Novo ano curricular. */
    public void setAnoCurricular(int anoCurricular) { this.anoCurricular = anoCurricular; }

    /** @param ects Novo valor de ECTS. */
    public void setEcts(int ects)                   { this.ects = ects; }

    /** @param numMomentos Número de momentos de avaliação (mínimo 1). */
    public void setNumMomentos(int numMomentos)      { this.numMomentos = Math.max(1, numMomentos); }

    // ---------- MÉTODOS DE LÓGICA E AÇÃO ----------

    /**
     * Associa esta UC a um curso adicional.
     * @param curso Curso a associar.
     * @return true se a associação foi registada; false se o limite foi atingido.
     */
    public boolean adicionarCurso(Curso curso) {
        if (totalCursos < cursos.length) {
            cursos[totalCursos] = curso;
            totalCursos++;
            return true;
        }
        return false;
    }
}
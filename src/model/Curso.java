package model;

/**
 * Representa um curso académico com duração de 3 anos.
 * Cada curso pertence a um único departamento e tem um valor
 * de propina anual configurável.
 * Um curso não pode ser alterado enquanto tiver estudantes ou docentes alocados.
 */
public class Curso {

    /** Duração padrão de qualquer curso, em anos. */
    public static final int DURACAO_ANOS = 3;

    private String sigla;
    private String nome;
    private Departamento departamento;
    private String estado;
    private final double valorPropinaAnual;

    // ---------- CONSTRUTOR ----------

    /**
     * Cria um curso no estado "Inativo" por omissão.
     * @param sigla             Sigla identificadora do curso.
     * @param nome              Nome completo do curso.
     * @param departamento      Departamento ao qual o curso pertence.
     * @param valorPropinaAnual Propina anual cobrada a cada estudante.
     */
    public Curso(String sigla, String nome, Departamento departamento, double valorPropinaAnual) {
        this.sigla = sigla;
        this.nome              = nome;
        this.departamento      = departamento;
        this.valorPropinaAnual = valorPropinaAnual;
        this.estado            = "Inativo";
    }

    // ---------- GETTERS ----------

    /** @return Sigla do curso. */
    public String getSigla(){ return sigla; }

    /** @return Nome completo do curso. */
    public String getNome(){ return nome; }

    /** @return Departamento ao qual o curso pertence. */
    public Departamento getDepartamento(){ return departamento; }

    /** @return Valor da propina anual em euros. */
    public double getValorPropinaAnual(){ return valorPropinaAnual; }

    /** @return Estado atual do curso ("Ativo" ou "Inativo"). */
    public String getEstado(){ return estado; }


    // ---------- SETTERS ----------

    /** @param sigla Nova sigla. */
    public void setSigla(String sigla){ this.sigla = sigla; }

    /** @param nome Novo nome. */
    public void setNome(String nome){ this.nome = nome; }

    /** @param dep Novo departamento. */
    public void setDepartamento(Departamento dep){ this.departamento = dep; }

    /** @param estado Novo estado ("Ativo" ou "Inativo"). */
    public void setEstado(String estado){ this.estado = estado; }
}

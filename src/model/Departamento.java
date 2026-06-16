package model;

/**
 * Representa um departamento académico do sistema.
 * Cada curso está associado a exatamente um departamento.
 */
public class Departamento {

    private String sigla;
    private String nome;

    /**
     * Cria um departamento com sigla e nome.
     * @param sigla Sigla identificadora (ex.: "DEIS").
     * @param nome  Nome completo do departamento.
     */
    public Departamento(String sigla, String nome) {
        this.sigla = sigla;
        this.nome  = nome;
    }

    /** @return Sigla do departamento. */
    public String getSigla(){ return sigla; }

    /** @return Nome completo do departamento. */
    public String getNome(){ return nome; }

    /** @param sigla Nova sigla. */
    public void setSigla(String sigla){ this.sigla = sigla; }

    /** @param nome Novo nome. */
    public void setNome(String nome){ this.nome = nome; }
}
package model;

/**
 * Classe base para todos os utilizadores do sistema.
 * Contém os dados pessoais e credenciais de acesso comuns
 * a estudantes, docentes e gestores.
 */
public abstract class Utilizador {

    // ---------- ATRIBUTOS ----------
    protected String email;
    protected String password;
    protected String nome;
    protected String nif;
    protected String morada;
    protected String dataNascimento;


    public Utilizador() {}

    /**
     * Cria um utilizador com todos os dados pessoais e credenciais.
     * @param email          Email institucional.
     * @param password       Hash PBKDF2 da palavra-chave.
     * @param nome           Nome completo.
     * @param nif            NIF (9 dígitos).
     * @param morada         Morada de residência.
     * @param dataNascimento Data de nascimento (DD-MM-AAAA).
     */
    public Utilizador(String email, String password, String nome, String nif, String morada, String dataNascimento) {
        this.email = email;
        this.password = password;
        this.nome = nome;
        this.nif = nif;
        this.morada = morada;
        this.dataNascimento = dataNascimento;
    }

    /** @return Email institucional do utilizador. */
    public String getEmail() { return email; }

    /** @return Hash PBKDF2 da palavra-chave. Nunca texto limpo. */
    public String getPassword() { return password; }

    /** @return Nome completo. */
    public String getNome() { return nome; }

    /** @return NIF (9 dígitos). */
    public String getNif() { return nif; }

    /** @return Morada de residência. */
    public String getMorada() { return morada; }

    /** @return Data de nascimento (DD-MM-AAAA). */
    public String getDataNascimento() { return dataNascimento; }

    /**
     * Atualiza o hash da palavra-chave após alteração ou recuperação.
     * @param password Novo hash PBKDF2.
     */
    public void setPassword(String password) { this.password = password; }

    /** @param nome Novo nome completo. */
    public void setNome(String nome) { this.nome = nome; }

    /** @param nif Novo NIF. */
    public void setNif(String nif) { this.nif = nif; }

    /** @param morada Nova morada. */
    public void setMorada(String morada) { this.morada = morada; }

    /** @param dataNascimento Nova data de nascimento (DD-MM-AAAA). */
    public void setDataNascimento(String dataNascimento) { this.dataNascimento = dataNascimento; }
}
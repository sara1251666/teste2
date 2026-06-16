package model;

/**
 * Representa um registo da tabela [login] (ou de credenciais.csv no modo ficheiros).
 * Espelha exatamente as colunas do schema: dados de autenticação de um utilizador,
 * independentes do seu perfil de domínio (Estudante/Docente/Gestor).
 */
public class LoginModel {

    private int id;
    private String email;
    private String passwordHash;
    private String passwordSalt;
    private String tipoUtilizador;   // GESTOR | DOCENTE | ESTUDANTE
    private boolean ativo;
    private String createdAt;
    private String updatedAt;

    public LoginModel() {}

    /** Construtor para um registo novo, ainda sem id atribuído. */
    public LoginModel(String email, String passwordHash, String passwordSalt,
                      String tipoUtilizador, boolean ativo) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.tipoUtilizador = tipoUtilizador;
        this.ativo = ativo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getPasswordSalt() { return passwordSalt; }
    public void setPasswordSalt(String passwordSalt) { this.passwordSalt = passwordSalt; }

    public String getTipoUtilizador() { return tipoUtilizador; }
    public void setTipoUtilizador(String tipoUtilizador) { this.tipoUtilizador = tipoUtilizador; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "#" + id + " " + email + " (" + tipoUtilizador + ", " + (ativo ? "ativo" : "inativo") + ")";
    }
}

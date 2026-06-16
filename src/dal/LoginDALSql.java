package dal;

import dal.db.ConnectionManager;
import dal.db.RowMapper;
import model.LoginModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Implementação de {@link LoginDAL} sobre o SQL Server, no padrão do professor:
 * usa {@link ConnectionManager#select} / {@link ConnectionManager#create} /
 * {@link ConnectionManager#update} com {@link RowMapper} e queries parametrizadas (?).
 *
 * Tabela: [login] (singular, conforme convenção do projeto).
 */
public class LoginDALSql implements LoginDAL {

    private static final String TABELA = "login";
    private static final String[] CAMINHOS_SCHEMA = {
            "sql/schema_login.sql",
            "LP2-Grupo1/sql/schema_login.sql",
            "../sql/schema_login.sql"
    };

    private static final RowMapper<LoginModel> MAPPER = rs -> {
        LoginModel m = new LoginModel();
        m.setId(rs.getInt("id"));
        m.setEmail(rs.getString("email"));
        m.setPasswordHash(rs.getString("passwordHash"));
        m.setPasswordSalt(rs.getString("passwordSalt"));
        m.setTipoUtilizador(rs.getString("tipoUtilizador"));
        m.setAtivo(rs.getBoolean("ativo"));
        m.setCreatedAt(rs.getString("createdAt"));
        m.setUpdatedAt(rs.getString("updatedAt"));
        return m;
    };

    private final ConnectionManager cm;

    public LoginDALSql() {
        this(new ConnectionManager());
    }

    public LoginDALSql(ConnectionManager cm) {
        this.cm = cm;
    }

    @Override
    public void inicializar() {
        if (!cm.existeTabela(TABELA)) {
            cm.executarScript(lerSchema());
        }
        if (contar() == 0) {
            importarDeCsv();
        }
        // Garante sempre que o admin existe com hash PBKDF2 correto.
        // O upsert permite corrigir registos criados com algoritmo diferente (ex: SHA-256).
        LoginModel admin = LoginDAL.adminPorOmissao();
        if (!existe(admin.getEmail())) {
            criar(admin);
        } else {
            atualizar(admin);
        }
    }

    @Override
    public LoginModel procurarPorEmail(String email) {
        List<LoginModel> r = cm.select(
                "SELECT * FROM [login] WHERE email = ?", MAPPER, email);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public List<LoginModel> listarTodos() {
        return cm.select("SELECT * FROM [login] ORDER BY id", MAPPER);
    }

    @Override
    public LoginModel criar(LoginModel novo) {
        int id = cm.create(
                "INSERT INTO [login] (email, passwordHash, passwordSalt, tipoUtilizador, ativo) "
                        + "VALUES (?, ?, ?, ?, ?)",
                novo.getEmail(), novo.getPasswordHash(), novo.getPasswordSalt(),
                novo.getTipoUtilizador(), novo.isAtivo());
        novo.setId(id);
        return novo;
    }

    @Override
    public boolean atualizar(LoginModel login) {
        int linhas = cm.update(
                "UPDATE [login] SET passwordHash = ?, passwordSalt = ?, tipoUtilizador = ?, "
                        + "ativo = ?, updatedAt = GETDATE() WHERE email = ?",
                login.getPasswordHash(), login.getPasswordSalt(), login.getTipoUtilizador(),
                login.isAtivo(), login.getEmail());
        return linhas > 0;
    }

    @Override
    public boolean eliminar(String email) {
        return cm.update("DELETE FROM [login] WHERE email = ?", email) > 0;
    }

    @Override
    public boolean existe(String email) {
        return procurarPorEmail(email) != null;
    }

    @Override
    public int contar() {
        List<Integer> r = cm.select("SELECT COUNT(*) AS total FROM [login]",
                rs -> rs.getInt("total"));
        return r.isEmpty() ? 0 : r.get(0);
    }

    // ------------------------------------------------------------------

    /** Importa registos de credenciais.csv para a BD. Devolve true se importou algum. */
    private boolean importarDeCsv() {
        List<LoginModel> doFicheiro = new LoginDALFile().listarTodos();
        if (doFicheiro.isEmpty()) return false;
        for (LoginModel m : doFicheiro) {
            if (!existe(m.getEmail())) criar(m);
        }
        System.out.println(">> Migração: " + doFicheiro.size() + " login(s) importado(s) de credenciais.csv para SQL.");
        return true;
    }

    private static String lerSchema() {
        for (String c : CAMINHOS_SCHEMA) {
            Path p = Path.of(c);
            if (Files.exists(p)) {
                try { return Files.readString(p); }
                catch (IOException e) { throw new dal.db.DataAccessException("Falha ao ler " + p, e); }
            }
        }
        // Fallback embutido
        return """
               CREATE TABLE [login] (
                   id            INT IDENTITY(1,1) PRIMARY KEY,
                   email         NVARCHAR(255) NOT NULL UNIQUE,
                   passwordHash  NVARCHAR(500) NOT NULL,
                   passwordSalt  NVARCHAR(255) NOT NULL,
                   tipoUtilizador NVARCHAR(20) NOT NULL,
                   ativo         BIT NOT NULL DEFAULT 1,
                   createdAt     DATETIME2 NOT NULL DEFAULT GETDATE(),
                   updatedAt     DATETIME2 NOT NULL DEFAULT GETDATE()
               );
               """;
    }
}

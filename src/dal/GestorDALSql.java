package dal;

import dal.db.ConnectionManager;
import dal.db.RowMapper;
import model.Gestor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Implementação de {@link GestorDAL} sobre o SQL Server.
 * Usa {@link ConnectionManager} + {@link RowMapper} e queries parametrizadas (?).
 *
 * Tabela: [gestor] (email PK, nome, nif, morada, dataNascimento).
 * Inicialização automática: cria tabela se não existe e importa gestores.csv se vazio.
 */
public class GestorDALSql implements GestorDAL {

    private static final String TABELA = "gestor";
    private static final String[] CAMINHOS_SCHEMA = {
            "sql/schema_gestor.sql",
            "LP2-Grupo1/sql/schema_gestor.sql",
            "../sql/schema_gestor.sql"
    };

    /**
     * RowMapper que constrói um Gestor a partir de um ResultSet.
     * O hash da password não está nesta tabela — é passado pelo chamador.
     */
    private static Gestor mapRow(java.sql.ResultSet rs, String hash) throws java.sql.SQLException {
        return new Gestor(
                rs.getString("email"),
                hash != null ? hash : "",
                rs.getString("nome"),
                rs.getString("nif"),
                rs.getString("morada") != null ? rs.getString("morada") : "",
                rs.getString("dataNascimento") != null ? rs.getString("dataNascimento") : ""
        );
    }

    private final ConnectionManager cm;

    public GestorDALSql() {
        this(new ConnectionManager());
    }

    public GestorDALSql(ConnectionManager cm) {
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
    }

    @Override
    public Gestor procurarPorEmail(String email, String hash) {
        List<Gestor> r = cm.select(
                "SELECT * FROM [gestor] WHERE email = ?",
                rs -> mapRow(rs, hash),
                email);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public int contar() {
        List<Integer> r = cm.select("SELECT COUNT(*) AS total FROM [gestor]",
                rs -> rs.getInt("total"));
        return r.isEmpty() ? 0 : r.get(0);
    }

    // ------------------------------------------------------------------

    private void importarDeCsv() {
        // Importa perfis de gestores.csv; o hash não está no CSV — usa string vazia
        GestorDALFile file = new GestorDALFile();
        // Lê linhas diretamente para não depender do hash
        List<String> linhas = dal.DALUtil.lerFicheiro(
                common.ConfigApp.PASTA_BD + java.io.File.separator + "gestores.csv");
        int importados = 0;
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase("email;nome;nif;morada;dataNascimento")) continue;
            String[] d = linha.split(";", -1);
            if (d.length < 5) continue;
            String email = d[0].trim();
            if (email.isEmpty()) continue;
            // Verifica se já existe para evitar duplicados
            List<Gestor> check = cm.select(
                    "SELECT * FROM [gestor] WHERE email = ?", rs -> mapRow(rs, ""), email);
            if (!check.isEmpty()) continue;
            cm.update(
                    "INSERT INTO [gestor] (email, nome, nif, morada, dataNascimento) VALUES (?, ?, ?, ?, ?)",
                    email, d[1].trim(), d[2].trim(), d[3].trim(), d[4].trim());
            importados++;
        }
        if (importados > 0) {
            System.out.println(">> Migração: " + importados
                    + " gestor(es) importado(s) de gestores.csv para SQL.");
        }
    }

    private static String lerSchema() {
        for (String c : CAMINHOS_SCHEMA) {
            Path p = Path.of(c);
            if (Files.exists(p)) {
                try { return Files.readString(p); }
                catch (IOException e) {
                    throw new dal.db.DataAccessException("Falha ao ler " + p, e);
                }
            }
        }
        // Fallback embutido
        return "CREATE TABLE [gestor] (\n"
                + "    email          NVARCHAR(255) NOT NULL PRIMARY KEY,\n"
                + "    nome           NVARCHAR(100) NOT NULL,\n"
                + "    nif            NVARCHAR(9)   NOT NULL UNIQUE,\n"
                + "    morada         NVARCHAR(255),\n"
                + "    dataNascimento NVARCHAR(20)\n"
                + ");\n";
    }
}

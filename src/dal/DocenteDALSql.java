package dal;

import common.ConfigApp;
import dal.db.ConnectionManager;
import dal.db.RowMapper;
import model.Docente;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação SQL Server de {@link DocenteDAL}.
 * Usa {@link ConnectionManager} + {@link RowMapper} e queries parametrizadas (?).
 *
 * Tabela: [docente] (sigla PK, email UNIQUE, nome, nif UNIQUE, morada, dataNascimento).
 * Inicialização automática: cria tabela se não existe e importa docentes.csv se vazio.
 */
public class DocenteDALSql implements DocenteDAL {

    private UcDAL ucDALInstance;
    private UcDAL ucDAL() {
        if (ucDALInstance == null)
            ucDALInstance = ConfigApp.isModoSql() ? new UcDALSql() : new UcDALFile();
        return ucDALInstance;
    }

    private static final String TABELA = "docente";
    private static final String[] CAMINHOS_SCHEMA = {
            "sql/schema_docente.sql",
            "LP2-Grupo1/sql/schema_docente.sql",
            "../sql/schema_docente.sql"
    };

    private static final RowMapper<Docente> MAPPER =
            rs -> mapRow(rs, "");

    private static Docente mapRow(java.sql.ResultSet rs, String hash) throws java.sql.SQLException {
        return new Docente(
                rs.getString("sigla"),
                rs.getString("email"),
                hash,
                rs.getString("nome"),
                rs.getString("nif"),
                rs.getString("morada"),
                rs.getString("dataNascimento")
        );
    }

    private final ConnectionManager cm;

    public DocenteDALSql() {
        this(new ConnectionManager());
    }

    public DocenteDALSql(ConnectionManager cm) {
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
    public Docente procurarPorEmail(String email, String hash) {
        List<Docente> r = cm.select(
                "SELECT * FROM [docente] WHERE email = ?",
                rs -> mapRow(rs, hash), email);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public Docente procurarPorSigla(String sigla) {
        List<Docente> r = cm.select(
                "SELECT * FROM [docente] WHERE sigla = ?", MAPPER, sigla);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public List<Docente> carregarTodos() {
        return cm.select("SELECT * FROM [docente] ORDER BY sigla", MAPPER);
    }

    @Override
    public String[] obterListaDocentes() {
        List<Docente> todos = carregarTodos();
        List<String> lista = new ArrayList<>();
        for (Docente d : todos) lista.add(d.getSigla() + " - " + d.getNome());
        return lista.toArray(new String[0]);
    }

    @Override
    public boolean adicionarDocente(Docente d) {
        if (d == null) return false;
        cm.update("INSERT INTO [docente] (sigla, email, nome, nif, morada, dataNascimento) "
                + "VALUES (?, ?, ?, ?, ?, ?)",
                d.getSigla(), d.getEmail(), d.getNome(),
                d.getNif(), d.getMorada(), d.getDataNascimento());
        return true;
    }

    @Override
    public boolean atualizarDocente(Docente d) {
        if (d == null) return false;
        int linhas = cm.update(
                "UPDATE [docente] SET nome = ?, nif = ?, morada = ?, dataNascimento = ? "
                + "WHERE sigla = ?",
                d.getNome(), d.getNif(), d.getMorada(), d.getDataNascimento(), d.getSigla());
        return linhas > 0;
    }

    @Override
    public boolean removerDocente(String sigla) {
        Docente d = procurarPorSigla(sigla);
        if (d == null) return false;
        cm.update("DELETE FROM [login] WHERE email = ?", d.getEmail());
        int linhas = cm.update("DELETE FROM [docente] WHERE sigla = ?", sigla);
        return linhas > 0;
    }

    @Override
    public boolean existeSigla(String sigla) {
        return procurarPorSigla(sigla) != null;
    }

    @Override
    public boolean existeNif(String nif) {
        if (nif == null || nif.trim().isEmpty()) return false;
        List<Integer> r = cm.select(
                "SELECT COUNT(*) AS total FROM [docente] WHERE nif = ?",
                rs -> rs.getInt("total"), nif);
        return !r.isEmpty() && r.get(0) > 0;
    }

    @Override
    public boolean temUcAtribuida(String sigla) {
        return !ucDAL().obterSiglasUcsPorDocente(sigla, ConfigApp.PASTA_BD).isEmpty();
    }

    @Override
    public int contar() {
        List<Integer> r = cm.select("SELECT COUNT(*) AS total FROM [docente]",
                rs -> rs.getInt("total"));
        return r.isEmpty() ? 0 : r.get(0);
    }

    // ------------------------------------------------------------------

    private void importarDeCsv() {
        List<Docente> doFicheiro = new DocenteDALFile().carregarTodos();
        if (doFicheiro.isEmpty()) return;
        for (Docente d : doFicheiro) {
            if (!existeSigla(d.getSigla())) adicionarDocente(d);
        }
        System.out.println(">> Migração: " + doFicheiro.size()
                + " docente(s) importado(s) de docentes.csv para SQL.");
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
        return "CREATE TABLE [docente] (\n"
                + "    sigla          NVARCHAR(10)  NOT NULL PRIMARY KEY,\n"
                + "    email          NVARCHAR(255) NOT NULL UNIQUE,\n"
                + "    nome           NVARCHAR(100) NOT NULL,\n"
                + "    nif            NVARCHAR(9)   NOT NULL UNIQUE,\n"
                + "    morada         NVARCHAR(255),\n"
                + "    dataNascimento NVARCHAR(20)\n"
                + ");\n";
    }
}

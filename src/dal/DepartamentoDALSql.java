package dal;

import dal.db.ConnectionManager;
import dal.db.RowMapper;
import model.Departamento;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação de {@link DepartamentoDAL} sobre o SQL Server.
 * Usa {@link ConnectionManager} + {@link RowMapper} e queries parametrizadas (?).
 *
 * Tabela: [departamento] (sigla PK, nome).
 * Inicialização automática: cria tabela se não existe e importa departamentos.csv se vazio.
 */
public class DepartamentoDALSql implements DepartamentoDAL {

    private static final String TABELA = "departamento";
    private static final String[] CAMINHOS_SCHEMA = {
            "sql/schema_departamento.sql",
            "LP2-Grupo1/sql/schema_departamento.sql",
            "../sql/schema_departamento.sql"
    };

    private static final RowMapper<Departamento> MAPPER =
            rs -> new Departamento(rs.getString("sigla"), rs.getString("nome"));

    private final ConnectionManager cm;

    public DepartamentoDALSql() {
        this(new ConnectionManager());
    }

    public DepartamentoDALSql(ConnectionManager cm) {
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
    public Departamento procurarPorSigla(String sigla) {
        List<Departamento> r = cm.select(
                "SELECT * FROM [departamento] WHERE sigla = ?", MAPPER, sigla);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public List<Departamento> listarTodos() {
        return cm.select("SELECT * FROM [departamento] ORDER BY sigla", MAPPER);
    }

    @Override
    public String[] obterListaFormatada() {
        List<Departamento> todos = listarTodos();
        List<String> lista = new ArrayList<>();
        for (Departamento d : todos) lista.add(d.getSigla() + " - " + d.getNome());
        return lista.toArray(new String[0]);
    }

    @Override
    public boolean criar(Departamento d) {
        if (d == null) return false;
        cm.update("INSERT INTO [departamento] (sigla, nome) VALUES (?, ?)",
                d.getSigla(), d.getNome());
        return true;
    }

    @Override
    public boolean atualizar(Departamento d) {
        if (d == null) return false;
        int linhas = cm.update("UPDATE [departamento] SET nome = ? WHERE sigla = ?",
                d.getNome(), d.getSigla());
        return linhas > 0;
    }

    @Override
    public boolean eliminar(String sigla) {
        return cm.update("DELETE FROM [departamento] WHERE sigla = ?", sigla) > 0;
    }

    @Override
    public boolean existe(String sigla) {
        return procurarPorSigla(sigla) != null;
    }

    @Override
    public int contar() {
        List<Integer> r = cm.select("SELECT COUNT(*) AS total FROM [departamento]",
                rs -> rs.getInt("total"));
        return r.isEmpty() ? 0 : r.get(0);
    }

    // ------------------------------------------------------------------

    private void importarDeCsv() {
        List<Departamento> doFicheiro = new DepartamentoDALFile().listarTodos();
        if (doFicheiro.isEmpty()) return;
        for (Departamento d : doFicheiro) {
            if (!existe(d.getSigla())) criar(d);
        }
        System.out.println(">> Migração: " + doFicheiro.size()
                + " departamento(s) importado(s) de departamentos.csv para SQL.");
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
        return "CREATE TABLE [departamento] (\n"
                + "    sigla NVARCHAR(10)  NOT NULL PRIMARY KEY,\n"
                + "    nome  NVARCHAR(100) NOT NULL\n"
                + ");\n";
    }
}

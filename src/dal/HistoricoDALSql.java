package dal;

import common.ConfigApp;
import dal.db.ConnectionManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementação SQL Server de {@link HistoricoDAL}.
 * Usa {@link ConnectionManager} e queries parametrizadas (?).
 *
 * Tabela: [historicoAcademico] (anoLetivo, numMec, siglaUC, notas, estado; PK composta).
 * Inicialização automática: cria tabela se não existe e importa
 * historico_academico.csv se a tabela estiver vazia.
 */
public class HistoricoDALSql implements HistoricoDAL {

    private static final String TABELA = "historicoAcademico";
    private static final String[] CAMINHOS_SCHEMA = {
            "sql/schema_historico_academico.sql",
            "LP2-Grupo1/sql/schema_historico_academico.sql",
            "../sql/schema_historico_academico.sql"
    };

    private final ConnectionManager cm;

    public HistoricoDALSql() {
        this(new ConnectionManager());
    }

    public HistoricoDALSql(ConnectionManager cm) {
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
    public void guardarRegistoHistorico(int anoLetivo, int numMec, String siglaUC, String notas, String estado) {
        String notasVal = notas == null ? "" : notas;
        String estadoVal = estado == null ? "" : estado;

        if (existeRegisto(anoLetivo, numMec, siglaUC)) {
            cm.update("UPDATE [historicoAcademico] SET notas = ?, estado = ? "
                            + "WHERE anoLetivo = ? AND numMec = ? AND siglaUC = ?",
                    notasVal, estadoVal, anoLetivo, numMec, siglaUC);
            return;
        }
        cm.update("INSERT INTO [historicoAcademico] (anoLetivo, numMec, siglaUC, notas, estado) VALUES (?, ?, ?, ?, ?)",
                anoLetivo, numMec, siglaUC, notasVal, estadoVal);
    }

    @Override
    public List<String> consultarHistoricoPorAno(int anoLetivo) {
        return cm.select(
                "SELECT anoLetivo, numMec, siglaUC, notas, estado FROM [historicoAcademico] WHERE anoLetivo = ?",
                HistoricoDALSql::mapearLinha, anoLetivo);
    }

    @Override
    public List<String> consultarHistoricoPorAluno(int numMec) {
        return cm.select(
                "SELECT anoLetivo, numMec, siglaUC, notas, estado FROM [historicoAcademico] WHERE numMec = ?",
                HistoricoDALSql::mapearLinha, numMec);
    }

    // ------------------------------------------------------------------

    private static String mapearLinha(ResultSet rs) throws SQLException {
        String notas = rs.getString("notas");
        String estado = rs.getString("estado");
        return rs.getInt("anoLetivo") + ";" + rs.getInt("numMec") + ";" + rs.getString("siglaUC")
                + ";" + (notas == null ? "" : notas) + ";" + (estado == null ? "" : estado);
    }

    private boolean existeRegisto(int anoLetivo, int numMec, String siglaUC) {
        List<Integer> r = cm.select(
                "SELECT COUNT(*) AS total FROM [historicoAcademico] WHERE anoLetivo = ? AND numMec = ? AND siglaUC = ?",
                rs -> rs.getInt("total"), anoLetivo, numMec, siglaUC);
        return !r.isEmpty() && r.get(0) > 0;
    }

    private int contar() {
        List<Integer> r = cm.select("SELECT COUNT(*) AS total FROM [historicoAcademico]",
                rs -> rs.getInt("total"));
        return r.isEmpty() ? 0 : r.get(0);
    }

    private void importarDeCsv() {
        String caminho = ConfigApp.PASTA_BD + File.separator + "historico_academico.csv";
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        int total = 0;
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase("anoLetivo;numMec;siglaUC;notas;estado")) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 5) {
                try {
                    int anoLetivo = Integer.parseInt(dados[0].trim());
                    int numMec = Integer.parseInt(dados[1].trim());
                    String siglaUC = dados[2].trim();
                    String notas = dados[3].trim();
                    String estado = dados[4].trim();
                    cm.update("INSERT INTO [historicoAcademico] (anoLetivo, numMec, siglaUC, notas, estado) VALUES (?, ?, ?, ?, ?)",
                            anoLetivo, numMec, siglaUC, notas, estado);
                    total++;
                } catch (NumberFormatException ignored) {}
            }
        }
        if (total > 0) {
            System.out.println(">> Migração: " + total
                    + " registo(s) de histórico importado(s) de historico_academico.csv para SQL.");
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
        return "CREATE TABLE [historicoAcademico] (\n"
                + "    anoLetivo INT          NOT NULL,\n"
                + "    numMec    INT          NOT NULL REFERENCES [estudante](numMec),\n"
                + "    siglaUC   NVARCHAR(10) NOT NULL,\n"
                + "    notas     NVARCHAR(100),\n"
                + "    estado    NVARCHAR(20),\n"
                + "    PRIMARY KEY (anoLetivo, numMec, siglaUC)\n"
                + ");\n";
    }
}

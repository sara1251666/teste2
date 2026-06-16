package dal;

import common.ConfigApp;
import dal.db.ConnectionManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Implementação SQL Server de {@link InscricaoDAL}.
 * Usa {@link ConnectionManager} e queries parametrizadas (?).
 *
 * Tabela: [inscricao] (numMec, siglaUC, anoLetivo; PK composta).
 * Inicialização automática: cria tabela se não existe e importa
 * inscricoes.csv se a tabela estiver vazia.
 */
public class InscricaoDALSql implements InscricaoDAL {

    private static final String TABELA = "inscricao";
    private static final String[] CAMINHOS_SCHEMA = {
            "sql/schema_inscricao.sql",
            "LP2-Grupo1/sql/schema_inscricao.sql",
            "../sql/schema_inscricao.sql"
    };

    private final ConnectionManager cm;

    public InscricaoDALSql() {
        this(new ConnectionManager());
    }

    public InscricaoDALSql(ConnectionManager cm) {
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
    public void adicionarInscricao(int numMec, String siglaUC, int anoLetivo) {
        if (siglaUC == null || siglaUC.trim().isEmpty()) return;
        if (existeInscricao(numMec, siglaUC.trim(), anoLetivo)) return;
        cm.update("INSERT INTO [inscricao] (numMec, siglaUC, anoLetivo) VALUES (?, ?, ?)",
                numMec, siglaUC.trim(), anoLetivo);
    }

    @Override
    public void removerInscricao(int numMec, String siglaUC, int anoLetivo) {
        if (siglaUC == null || siglaUC.trim().isEmpty()) return;
        cm.update("DELETE FROM [inscricao] WHERE numMec = ? AND siglaUC = ? AND anoLetivo = ?",
                numMec, siglaUC.trim(), anoLetivo);
    }

    @Override
    public List<String> obterSiglasUcsPorAluno(int numMec, int anoLetivo) {
        return cm.select(
                "SELECT siglaUC FROM [inscricao] WHERE numMec = ? AND anoLetivo = ?",
                rs -> rs.getString("siglaUC"), numMec, anoLetivo);
    }

    @Override
    public List<String> obterSiglasUcsPorAlunoTodosAnos(int numMec) {
        return cm.select(
                "SELECT siglaUC FROM [inscricao] WHERE numMec = ?",
                rs -> rs.getString("siglaUC"), numMec);
    }

    @Override
    public List<Integer> obterAlunosPorUc(String siglaUC, int anoLetivo) {
        return cm.select(
                "SELECT numMec FROM [inscricao] WHERE siglaUC = ? AND anoLetivo = ?",
                rs -> rs.getInt("numMec"), siglaUC, anoLetivo);
    }

    @Override
    public List<Integer> obterAlunosPorUcTodosAnos(String siglaUC) {
        return cm.select(
                "SELECT numMec FROM [inscricao] WHERE siglaUC = ?",
                rs -> rs.getInt("numMec"), siglaUC);
    }

    @Override
    public void removerInscricoesPorAluno(int numMec) {
        cm.update("DELETE FROM [inscricao] WHERE numMec = ?", numMec);
    }

    // ------------------------------------------------------------------

    private boolean existeInscricao(int numMec, String siglaUC, int anoLetivo) {
        List<Integer> r = cm.select(
                "SELECT COUNT(*) AS total FROM [inscricao] WHERE numMec = ? AND siglaUC = ? AND anoLetivo = ?",
                rs -> rs.getInt("total"), numMec, siglaUC, anoLetivo);
        return !r.isEmpty() && r.get(0) > 0;
    }

    private int contar() {
        List<Integer> r = cm.select("SELECT COUNT(*) AS total FROM [inscricao]",
                rs -> rs.getInt("total"));
        return r.isEmpty() ? 0 : r.get(0);
    }

    private void importarDeCsv() {
        String caminho = ConfigApp.PASTA_BD + File.separator + "inscricoes.csv";
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        int total = 0;
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase("numMec;siglaUC;anoLetivo")
                    || linha.equalsIgnoreCase("numMec;siglaUC")) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 3) {
                try {
                    int numMec = Integer.parseInt(dados[0].trim());
                    String siglaUC = dados[1].trim();
                    int anoLetivo = Integer.parseInt(dados[2].trim());
                    if (!siglaUC.isEmpty()) {
                        adicionarInscricao(numMec, siglaUC, anoLetivo);
                        total++;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        if (total > 0) {
            System.out.println(">> Migração: " + total
                    + " inscrição(ões) importada(s) de inscricoes.csv para SQL.");
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
        return "CREATE TABLE [inscricao] (\n"
                + "    numMec    INT          NOT NULL REFERENCES [estudante](numMec),\n"
                + "    siglaUC   NVARCHAR(10) NOT NULL,\n"
                + "    anoLetivo INT          NOT NULL REFERENCES [anoLetivo](ano),\n"
                + "    PRIMARY KEY (numMec, siglaUC, anoLetivo)\n"
                + ");\n";
    }
}

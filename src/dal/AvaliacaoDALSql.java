package dal;

import common.ConfigApp;
import dal.db.ConnectionManager;
import dal.db.RowMapper;
import model.Avaliacao;
import model.UnidadeCurricular;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação SQL Server de {@link AvaliacaoDAL}.
 * Usa {@link ConnectionManager} e queries parametrizadas (?).
 *
 * Tabela: [avaliacao] (numMec, siglaUC, anoLetivo, nota1, nota2, nota3; PK composta).
 * Inicialização automática: cria tabela se não existe e importa
 * avaliacoes.csv se a tabela estiver vazia.
 */
public class AvaliacaoDALSql implements AvaliacaoDAL {

    private UcDAL ucDALInstance;
    private UcDAL ucDAL() {
        if (ucDALInstance == null)
            ucDALInstance = ConfigApp.isModoSql() ? new UcDALSql() : new UcDALFile();
        return ucDALInstance;
    }

    private static final String TABELA = "avaliacao";
    private static final String[] CAMINHOS_SCHEMA = {
            "sql/schema_avaliacao.sql",
            "LP2-Grupo1/sql/schema_avaliacao.sql",
            "../sql/schema_avaliacao.sql"
    };

    private static final String[] NOTA_COLS = {"nota1", "nota2", "nota3"};

    private static final RowMapper<Object[]> ROW_NOTAS = rs -> new Object[]{
            getNullableDouble(rs, "nota1"),
            getNullableDouble(rs, "nota2"),
            getNullableDouble(rs, "nota3")
    };

    private static final RowMapper<Object[]> ROW_COMPLETA = rs -> new Object[]{
            rs.getString("siglaUC"),
            rs.getInt("anoLetivo"),
            getNullableDouble(rs, "nota1"),
            getNullableDouble(rs, "nota2"),
            getNullableDouble(rs, "nota3")
    };

    private final ConnectionManager cm;

    public AvaliacaoDALSql() {
        this(new ConnectionManager());
    }

    public AvaliacaoDALSql(ConnectionManager cm) {
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
    public boolean existeAvaliacao(int numMec, String siglaUc, int anoLetivo) {
        List<Integer> r = cm.select(
                "SELECT COUNT(*) AS total FROM [avaliacao] WHERE numMec = ? AND siglaUC = ? AND anoLetivo = ?",
                rs -> rs.getInt("total"), numMec, siglaUc, anoLetivo);
        return !r.isEmpty() && r.get(0) > 0;
    }

    @Override
    public void adicionarAvaliacao(Avaliacao avaliacao, int numMec) {
        if (avaliacao == null || avaliacao.getUc() == null) return;

        String siglaUC = avaliacao.getUc().getSigla();
        int anoLetivo = avaliacao.getAnoLetivo();

        if (existeAvaliacao(numMec, siglaUC, anoLetivo)) {
            atualizarAvaliacao(avaliacao, numMec);
            return;
        }

        int total = avaliacao.getTotalAvaliacoesLancadas();
        double[] resultados = avaliacao.getResultados();

        StringBuilder cols = new StringBuilder("numMec, siglaUC, anoLetivo");
        StringBuilder placeholders = new StringBuilder("?, ?, ?");
        List<Object> params = new ArrayList<>();
        params.add(numMec);
        params.add(siglaUC);
        params.add(anoLetivo);
        for (int i = 0; i < total; i++) {
            cols.append(", ").append(NOTA_COLS[i]);
            placeholders.append(", ?");
            params.add(resultados[i]);
        }

        cm.update("INSERT INTO [avaliacao] (" + cols + ") VALUES (" + placeholders + ")",
                params.toArray());
    }

    @Override
    public List<Avaliacao> obterAvaliacoesPorAluno(int numMec) {
        List<Object[]> linhas = cm.select(
                "SELECT siglaUC, anoLetivo, nota1, nota2, nota3 FROM [avaliacao] WHERE numMec = ?",
                ROW_COMPLETA, numMec);

        List<Avaliacao> resultado = new ArrayList<>();
        for (Object[] dados : linhas) {
            String siglaUC = (String) dados[0];
            int ano = (Integer) dados[1];
            UnidadeCurricular uc = ucDAL().procurarUC(siglaUC, ConfigApp.PASTA_BD);
            if (uc == null) continue;

            Avaliacao av = new Avaliacao(uc, ano);
            for (int i = 2; i <= 4; i++) {
                Double nota = (Double) dados[i];
                if (nota != null) av.adicionarResultado(nota);
            }
            resultado.add(av);
        }
        return resultado;
    }

    @Override
    public Avaliacao obterAvaliacao(int numMec, String siglaUc, int ano) {
        List<Object[]> linhas = cm.select(
                "SELECT nota1, nota2, nota3 FROM [avaliacao] WHERE numMec = ? AND siglaUC = ? AND anoLetivo = ?",
                ROW_NOTAS, numMec, siglaUc, ano);
        if (linhas.isEmpty()) return null;

        UnidadeCurricular uc = ucDAL().procurarUC(siglaUc, ConfigApp.PASTA_BD);
        if (uc == null) uc = new UnidadeCurricular(siglaUc, "", ano, null);

        Avaliacao av = new Avaliacao(uc, ano);
        for (Object nota : linhas.get(0)) {
            if (nota != null) av.adicionarResultado((Double) nota);
        }
        return av;
    }

    @Override
    public void atualizarAvaliacao(Avaliacao aval, int numMec) {
        if (aval == null || aval.getUc() == null) return;

        int total = aval.getTotalAvaliacoesLancadas();
        if (total == 0) return;
        double[] resultados = aval.getResultados();

        StringBuilder set = new StringBuilder();
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            if (set.length() > 0) set.append(", ");
            set.append(NOTA_COLS[i]).append(" = ?");
            params.add(resultados[i]);
        }
        params.add(numMec);
        params.add(aval.getUc().getSigla());
        params.add(aval.getAnoLetivo());

        cm.update("UPDATE [avaliacao] SET " + set + " WHERE numMec = ? AND siglaUC = ? AND anoLetivo = ?",
                params.toArray());
    }

    // ------------------------------------------------------------------

    private int contar() {
        List<Integer> r = cm.select("SELECT COUNT(*) AS total FROM [avaliacao]",
                rs -> rs.getInt("total"));
        return r.isEmpty() ? 0 : r.get(0);
    }

    private void importarDeCsv() {
        String caminho = ConfigApp.PASTA_BD + File.separator + "avaliacoes.csv";
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        int total = 0;
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase("numMec;siglaUC;anoLetivo;nota1;nota2;nota3")) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 3) {
                try {
                    int numMec = Integer.parseInt(dados[0].trim());
                    String siglaUC = dados[1].trim();
                    int anoLetivo = Integer.parseInt(dados[2].trim());

                    StringBuilder cols = new StringBuilder("numMec, siglaUC, anoLetivo");
                    StringBuilder placeholders = new StringBuilder("?, ?, ?");
                    List<Object> params = new ArrayList<>();
                    params.add(numMec);
                    params.add(siglaUC);
                    params.add(anoLetivo);

                    for (int i = 0; i < NOTA_COLS.length; i++) {
                        int idx = 3 + i;
                        if (dados.length > idx && !dados[idx].trim().isEmpty()) {
                            cols.append(", ").append(NOTA_COLS[i]);
                            placeholders.append(", ?");
                            params.add(Double.parseDouble(dados[idx].trim()));
                        }
                    }

                    cm.update("INSERT INTO [avaliacao] (" + cols + ") VALUES (" + placeholders + ")",
                            params.toArray());
                    total++;
                } catch (NumberFormatException ignored) {}
            }
        }
        if (total > 0) {
            System.out.println(">> Migração: " + total
                    + " avaliação(ões) importada(s) de avaliacoes.csv para SQL.");
        }
    }

    private static Double getNullableDouble(ResultSet rs, String coluna) throws SQLException {
        double v = rs.getDouble(coluna);
        return rs.wasNull() ? null : v;
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
        return "CREATE TABLE [avaliacao] (\n"
                + "    numMec    INT          NOT NULL REFERENCES [estudante](numMec),\n"
                + "    siglaUC   NVARCHAR(10) NOT NULL,\n"
                + "    anoLetivo INT          NOT NULL,\n"
                + "    nota1     DECIMAL(4,2),\n"
                + "    nota2     DECIMAL(4,2),\n"
                + "    nota3     DECIMAL(4,2),\n"
                + "    PRIMARY KEY (numMec, siglaUC, anoLetivo)\n"
                + ");\n";
    }
}

package dal;

import common.ConfigApp;
import dal.db.ConnectionManager;
import model.Pagamento;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Implementação SQL Server de {@link PagamentoDAL}.
 * Usa {@link ConnectionManager} e queries parametrizadas (?).
 *
 * Tabela: [pagamento] (id identity, numMec, valorPago, dataPagamento).
 * Inicialização automática: cria tabela se não existe e importa
 * pagamentos.csv se a tabela estiver vazia.
 */
public class PagamentoDALSql implements PagamentoDAL {

    private static final String TABELA = "pagamento";
    private static final String[] CAMINHOS_SCHEMA = {
            "sql/schema_pagamento.sql",
            "LP2-Grupo1/sql/schema_pagamento.sql",
            "../sql/schema_pagamento.sql"
    };

    private final ConnectionManager cm;

    public PagamentoDALSql() {
        this(new ConnectionManager());
    }

    public PagamentoDALSql(ConnectionManager cm) {
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
    public void adicionarPagamento(int numMec, double valorPago, String dataPagamento) {
        cm.update("INSERT INTO [pagamento] (numMec, valorPago, dataPagamento) VALUES (?, ?, ?)",
                numMec, valorPago, dataPagamento);
    }

    @Override
    public List<Pagamento> carregarPagamentosPorAluno(int numMec) {
        return cm.select(
                "SELECT numMec, valorPago, dataPagamento FROM [pagamento] WHERE numMec = ? ORDER BY id",
                rs -> new Pagamento(rs.getInt("numMec"), rs.getDouble("valorPago"), rs.getString("dataPagamento")),
                numMec);
    }

    // ------------------------------------------------------------------

    private int contar() {
        List<Integer> r = cm.select("SELECT COUNT(*) AS total FROM [pagamento]",
                rs -> rs.getInt("total"));
        return r.isEmpty() ? 0 : r.get(0);
    }

    private void importarDeCsv() {
        String caminho = ConfigApp.PASTA_BD + File.separator + "pagamentos.csv";
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        int total = 0;
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase("numMec;valorPago;dataPagamento")) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 3) {
                try {
                    int numMec = Integer.parseInt(dados[0].trim());
                    double valor = Double.parseDouble(dados[1].trim());
                    String data = dados[2].trim();
                    adicionarPagamento(numMec, valor, data);
                    total++;
                } catch (NumberFormatException ignored) {}
            }
        }
        if (total > 0) {
            System.out.println(">> Migração: " + total
                    + " pagamento(s) importado(s) de pagamentos.csv para SQL.");
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
        return "CREATE TABLE [pagamento] (\n"
                + "    id            INT IDENTITY(1,1) PRIMARY KEY,\n"
                + "    numMec        INT           NOT NULL REFERENCES [estudante](numMec),\n"
                + "    valorPago     DECIMAL(10,2) NOT NULL,\n"
                + "    dataPagamento NVARCHAR(20)  NOT NULL\n"
                + ");\n";
    }
}

package dal;

import dal.db.ConnectionManager;
import dal.db.RowMapper;
import model.AnoLetivo;
import model.EstadoAnoLetivo;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementação de {@link HistoricoAnoLetivoDAL} sobre o SQL Server.
 * Tabela: [anoLetivoHistorico] (ano PK, estado, dataArquivo).
 * Inicialização automática: cria tabela se não existe e importa CSV se vazio.
 */
public class HistoricoAnoLetivoDALSql implements HistoricoAnoLetivoDAL {

    private static final String TABELA = "anoLetivoHistorico";
    private static final String[] CAMINHOS_SCHEMA = DALUtil.SCHEMA_ANO_LETIVO_CAMINHOS;

    private final ConnectionManager cm;

    public HistoricoAnoLetivoDALSql() {
        this(new ConnectionManager());
    }

    public HistoricoAnoLetivoDALSql(ConnectionManager cm) {
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
    public void arquivar(AnoLetivo anoLetivo) {
        if (anoLetivo == null) return;
        if (jaExiste(anoLetivo.getAno())) return;

        String dataHoje = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        cm.update("INSERT INTO [anoLetivoHistorico] (ano, estado, dataArquivo) VALUES (?, ?, ?)",
                anoLetivo.getAno(), anoLetivo.getEstado().name(), dataHoje);
    }

    @Override
    public List<String> listar() {
        List<String> resultado = new ArrayList<>();
        List<String[]> linhas = cm.select(
                "SELECT ano, estado, dataArquivo FROM [anoLetivoHistorico] ORDER BY ano DESC",
                rs -> new String[]{
                        rs.getString("ano"),
                        rs.getString("estado"),
                        rs.getString("dataArquivo")
                });
        for (String[] d : linhas) {
            String data = (d[2] != null && !d[2].isBlank()) ? d[2] : "N/A";
            resultado.add(String.format("Ano %-6s | Estado: %-10s | Arquivado em: %s",
                    d[0], d[1], data));
        }
        return resultado;
    }

    // ------------------------------------------------------------------

    private boolean jaExiste(int ano) {
        List<Integer> r = cm.select(
                "SELECT COUNT(*) AS total FROM [anoLetivoHistorico] WHERE ano = ?",
                rs -> rs.getInt("total"), ano);
        return !r.isEmpty() && r.get(0) > 0;
    }

    private int contar() {
        List<Integer> r = cm.select(
                "SELECT COUNT(*) AS total FROM [anoLetivoHistorico]",
                rs -> rs.getInt("total"));
        return r.isEmpty() ? 0 : r.get(0);
    }

    private void importarDeCsv() {
        // Reutilizar o DALFile para obter os dados raw através de arquivar()
        // Lemos o CSV diretamente para não duplicar lógica
        String caminho = common.ConfigApp.PASTA_BD
                + java.io.File.separator + "anos_letivos_historico.csv";
        String cabecalho = "ano;estado;dataArquivo";
        List<String> csv = DALUtil.lerFicheiro(caminho);
        int importados = 0;
        for (String linha : csv) {
            if (linha.equalsIgnoreCase(cabecalho)) continue;
            String[] d = linha.split(";", -1);
            if (d.length < 2) continue;
            try {
                int ano    = Integer.parseInt(d[0].trim());
                String est = d[1].trim().toUpperCase();
                String dat = d.length >= 3 ? d[2].trim() : "";
                if (!jaExiste(ano)) {
                    cm.update("INSERT INTO [anoLetivoHistorico] (ano, estado, dataArquivo) VALUES (?, ?, ?)",
                            ano, est, dat.isEmpty() ? null : dat);
                    importados++;
                }
            } catch (Exception ignored) {}
        }
        if (importados > 0) {
            System.out.println(">> Migração: " + importados
                    + " registo(s) de histórico importado(s) de anos_letivos_historico.csv para SQL.");
        }
    }

    private static String lerSchema() {
        return DALUtil.lerSchema(CAMINHOS_SCHEMA, DALUtil.SCHEMA_ANO_LETIVO_FALLBACK);
    }
}

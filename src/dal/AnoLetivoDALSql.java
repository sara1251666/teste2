package dal;

import dal.db.ConnectionManager;
import dal.db.RowMapper;
import model.AnoLetivo;
import model.EstadoAnoLetivo;

import java.util.List;

/**
 * Implementação de {@link AnoLetivoDAL} sobre o SQL Server.
 * Tabela: [anoLetivo] (ano PK, estado).
 * Inicialização automática: cria tabela se não existe e importa anos_letivos.csv se vazio.
 */
public class AnoLetivoDALSql implements AnoLetivoDAL {

    private static final String TABELA = "anoLetivo";
    private static final String[] CAMINHOS_SCHEMA = DALUtil.SCHEMA_ANO_LETIVO_CAMINHOS;

    private static final RowMapper<AnoLetivo> MAPPER =
            rs -> new AnoLetivo(
                    rs.getInt("ano"),
                    EstadoAnoLetivo.valueOf(rs.getString("estado").trim().toUpperCase())
            );

    private final ConnectionManager cm;

    public AnoLetivoDALSql() {
        this(new ConnectionManager());
    }

    public AnoLetivoDALSql(ConnectionManager cm) {
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
    public void adicionar(AnoLetivo ano) {
        if (ano == null) return;
        cm.update("INSERT INTO [anoLetivo] (ano, estado) VALUES (?, ?)",
                ano.getAno(), ano.getEstado().name());
    }

    @Override
    public void atualizar(AnoLetivo ano) {
        if (ano == null) return;
        cm.update("UPDATE [anoLetivo] SET estado = ? WHERE ano = ?",
                ano.getEstado().name(), ano.getAno());
    }

    @Override
    public boolean remover(int ano) {
        return cm.update("DELETE FROM [anoLetivo] WHERE ano = ?", ano) > 0;
    }

    @Override
    public AnoLetivo procurarPorAno(int ano) {
        List<AnoLetivo> r = cm.select(
                "SELECT * FROM [anoLetivo] WHERE ano = ?", MAPPER, ano);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public List<AnoLetivo> listarTodos() {
        return cm.select("SELECT * FROM [anoLetivo] ORDER BY ano", MAPPER);
    }

    @Override
    public AnoLetivo obterAnoAtivo() {
        List<AnoLetivo> r = cm.select(
                "SELECT TOP 1 * FROM [anoLetivo] ORDER BY ano DESC", MAPPER);
        return r.isEmpty() ? null : r.get(0);
    }

    // ------------------------------------------------------------------

    private int contar() {
        List<Integer> r = cm.select("SELECT COUNT(*) AS total FROM [anoLetivo]",
                rs -> rs.getInt("total"));
        return r.isEmpty() ? 0 : r.get(0);
    }

    private void importarDeCsv() {
        List<AnoLetivo> doFicheiro = new AnoLetivoDALFile().listarTodos();
        if (doFicheiro.isEmpty()) return;
        for (AnoLetivo al : doFicheiro) {
            if (procurarPorAno(al.getAno()) == null) adicionar(al);
        }
        System.out.println(">> Migração: " + doFicheiro.size()
                + " ano(s) letivo(s) importado(s) de anos_letivos.csv para SQL.");
    }

    private static String lerSchema() {
        return DALUtil.lerSchema(CAMINHOS_SCHEMA, DALUtil.SCHEMA_ANO_LETIVO_FALLBACK);
    }
}

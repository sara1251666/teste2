package dal;

import common.ConfigApp;
import model.AnoLetivo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação de {@link HistoricoAnoLetivoDAL} sobre o ficheiro anos_letivos_historico.csv.
 * Formato CSV (delimitador ';'): ano;estado;dataArquivo
 */
public class HistoricoAnoLetivoDALFile implements HistoricoAnoLetivoDAL {

    private static final String NOME_FICHEIRO = "anos_letivos_historico.csv";
    private static final String CABECALHO     = "ano;estado;dataArquivo";

    private String caminho() {
        return ConfigApp.PASTA_BD + File.separator + NOME_FICHEIRO;
    }

    @Override
    public void inicializar() {
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
    }

    @Override
    public void arquivar(AnoLetivo anoLetivo) {
        if (anoLetivo == null) return;
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
        if (jaExiste(anoLetivo.getAno())) return;

        String dataHoje = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        DALUtil.adicionarLinhaCSV(caminho(),
                anoLetivo.getAno() + ";" + anoLetivo.getEstado().name() + ";" + dataHoje);
    }

    @Override
    public List<String> listar() {
        List<String> linhas    = DALUtil.lerFicheiro(caminho());
        List<String> resultado = new ArrayList<>();
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] d = linha.split(";", -1);
            if (d.length >= 3) {
                resultado.add(String.format("Ano %-6s | Estado: %-10s | Arquivado em: %s",
                        d[0].trim(), d[1].trim(), d[2].trim()));
            } else if (d.length == 2) {
                resultado.add(String.format("Ano %-6s | Estado: %-10s",
                        d[0].trim(), d[1].trim()));
            }
        }
        return resultado;
    }

    // ---------- privado ----------

    private boolean jaExiste(int ano) {
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] d = linha.split(";", -1);
            if (d.length >= 1) {
                try {
                    if (Integer.parseInt(d[0].trim()) == ano) return true;
                } catch (NumberFormatException ignored) {}
            }
        }
        return false;
    }
}

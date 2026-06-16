package dal;

import common.ConfigApp;
import model.AnoLetivo;
import model.EstadoAnoLetivo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação de {@link AnoLetivoDAL} sobre o ficheiro anos_letivos.csv.
 * Formato CSV (delimitador ';'): ano;estado
 */
public class AnoLetivoDALFile implements AnoLetivoDAL {

    private static final String NOME_FICHEIRO = "anos_letivos.csv";
    private static final String CABECALHO     = "ano;estado";

    private String caminho() {
        return ConfigApp.PASTA_BD + File.separator + NOME_FICHEIRO;
    }

    @Override
    public void inicializar() {
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
    }

    @Override
    public void adicionar(AnoLetivo ano) {
        if (ano == null) return;
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
        DALUtil.adicionarLinhaCSV(caminho(), ano.getAno() + ";" + ano.getEstado().name());
    }

    @Override
    public void atualizar(AnoLetivo ano) {
        if (ano == null) return;
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        List<String> novas  = new ArrayList<>();
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) { novas.add(linha); continue; }
            String[] d = linha.split(";", -1);
            if (d.length >= 1 && parseAno(d[0]) == ano.getAno()) {
                novas.add(ano.getAno() + ";" + ano.getEstado().name());
            } else {
                novas.add(linha);
            }
        }
        DALUtil.reescreverFicheiro(caminho(), novas);
    }

    @Override
    public boolean remover(int ano) {
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        List<String> novas  = new ArrayList<>();
        boolean removeu     = false;
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) { novas.add(linha); continue; }
            String[] d = linha.split(";", -1);
            if (d.length >= 1 && parseAno(d[0]) == ano) {
                removeu = true;
            } else {
                novas.add(linha);
            }
        }
        if (removeu) DALUtil.reescreverFicheiro(caminho(), novas);
        return removeu;
    }

    @Override
    public AnoLetivo procurarPorAno(int ano) {
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            AnoLetivo al = parseLinha(linha);
            if (al != null && al.getAno() == ano) return al;
        }
        return null;
    }

    @Override
    public List<AnoLetivo> listarTodos() {
        List<AnoLetivo> lista = new ArrayList<>();
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            AnoLetivo al = parseLinha(linha);
            if (al != null) lista.add(al);
        }
        return lista;
    }

    @Override
    public AnoLetivo obterAnoAtivo() {
        List<AnoLetivo> todos = listarTodos();
        AnoLetivo ativo = null;
        for (AnoLetivo al : todos) {
            if (ativo == null || al.getAno() > ativo.getAno()) {
                ativo = al;
            }
        }
        return ativo;
    }

    // ---------- helpers ----------

    private static AnoLetivo parseLinha(String linha) {
        String[] d = linha.split(";", -1);
        if (d.length < 2) return null;
        try {
            int ano = Integer.parseInt(d[0].trim());
            EstadoAnoLetivo estado = EstadoAnoLetivo.valueOf(d[1].trim().toUpperCase());
            return new AnoLetivo(ano, estado);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static int parseAno(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return -1; }
    }
}

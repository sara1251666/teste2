package dal;

import common.ConfigApp;
import model.Departamento;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação de {@link DepartamentoDAL} sobre o ficheiro departamentos.csv.
 * Formato CSV (delimitador ';'): sigla;nome
 */
public class DepartamentoDALFile implements DepartamentoDAL {

    private static final String NOME_FICHEIRO = "departamentos.csv";
    private static final String CABECALHO     = "sigla;nome";

    private String caminho() {
        return ConfigApp.PASTA_BD + File.separator + NOME_FICHEIRO;
    }

    @Override
    public void inicializar() {
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
        // CSV já existe com dados — nada mais a fazer em modo ficheiro
    }

    @Override
    public Departamento procurarPorSigla(String sigla) {
        if (sigla == null) return null;
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] d = linha.split(";", -1);
            if (d.length >= 2 && d[0].trim().equalsIgnoreCase(sigla)) {
                return new Departamento(d[0].trim(), d[1].trim());
            }
        }
        return null;
    }

    @Override
    public List<Departamento> listarTodos() {
        List<Departamento> lista = new ArrayList<>();
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] d = linha.split(";", -1);
            if (d.length >= 2) lista.add(new Departamento(d[0].trim(), d[1].trim()));
        }
        return lista;
    }

    @Override
    public String[] obterListaFormatada() {
        List<String> lista = new ArrayList<>();
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] d = linha.split(";", -1);
            if (d.length >= 2) lista.add(d[0].trim() + " - " + d[1].trim());
        }
        return lista.toArray(new String[0]);
    }

    @Override
    public boolean criar(Departamento d) {
        if (d == null) return false;
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
        DALUtil.adicionarLinhaCSV(caminho(), d.getSigla() + ";" + d.getNome());
        return true;
    }

    @Override
    public boolean atualizar(Departamento d) {
        if (d == null) return false;
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        List<String> novas  = new ArrayList<>();
        boolean atualizou   = false;

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) { novas.add(linha); continue; }
            String[] dados = linha.split(";", -1);
            if (dados.length >= 2 && dados[0].trim().equalsIgnoreCase(d.getSigla())) {
                novas.add(d.getSigla() + ";" + d.getNome());
                atualizou = true;
            } else {
                novas.add(linha);
            }
        }
        if (atualizou) DALUtil.reescreverFicheiro(caminho(), novas);
        return atualizou;
    }

    @Override
    public boolean eliminar(String sigla) {
        if (sigla == null) return false;
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        List<String> novas  = new ArrayList<>();
        boolean removeu     = false;

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) { novas.add(linha); continue; }
            String[] dados = linha.split(";", -1);
            if (dados.length >= 1 && dados[0].trim().equalsIgnoreCase(sigla)) {
                removeu = true;
            } else {
                novas.add(linha);
            }
        }
        if (removeu) DALUtil.reescreverFicheiro(caminho(), novas);
        return removeu;
    }

    @Override
    public boolean existe(String sigla) {
        return procurarPorSigla(sigla) != null;
    }

    @Override
    public int contar() {
        return listarTodos().size();
    }
}

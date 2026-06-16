package dal;

import common.ConfigApp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação ficheiro (CSV) de {@link HistoricoDAL}.
 * Persiste em historico_academico.csv na pasta de dados configurada.
 * Formato das colunas: anoLetivo;numMec;siglaUC;notas;estado.
 */
public class HistoricoDALFile implements HistoricoDAL {

    private static final String NOME_FICHEIRO = "historico_academico.csv";
    private static final String CABECALHO = "anoLetivo;numMec;siglaUC;notas;estado";

    private final String pastaBase;

    public HistoricoDALFile() {
        this.pastaBase = ConfigApp.PASTA_BD;
    }

    private String caminho() {
        return pastaBase + File.separator + NOME_FICHEIRO;
    }

    @Override
    public void inicializar() {
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
    }

    @Override
    public void guardarRegistoHistorico(int anoLetivo, int numMec, String siglaUC, String notas, String estado) {
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
        String linha = anoLetivo + ";" + numMec + ";" + siglaUC + ";" + notas + ";" + estado;
        DALUtil.adicionarLinhaCSV(caminho(), linha);
    }

    @Override
    public List<String> consultarHistoricoPorAno(int anoLetivo) {
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        List<String> resultados = new ArrayList<>();

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 1) {
                try {
                    if (Integer.parseInt(dados[0].trim()) == anoLetivo) {
                        resultados.add(linha);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return resultados;
    }

    @Override
    public List<String> consultarHistoricoPorAluno(int numMec) {
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        List<String> resultados = new ArrayList<>();

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 2) {
                try {
                    if (Integer.parseInt(dados[1].trim()) == numMec) {
                        resultados.add(linha);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return resultados;
    }
}

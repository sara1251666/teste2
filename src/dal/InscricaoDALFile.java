package dal;

import common.ConfigApp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação ficheiro (CSV) de {@link InscricaoDAL}.
 * Persiste em inscricoes.csv na pasta de dados configurada.
 * Formato das colunas: numMec;siglaUC;anoLetivo.
 */
public class InscricaoDALFile implements InscricaoDAL {

    private static final String NOME_FICHEIRO = "inscricoes.csv";
    private static final String CABECALHO = "numMec;siglaUC;anoLetivo";

    private final String pastaBase;

    public InscricaoDALFile() {
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
    public void adicionarInscricao(int numMec, String siglaUC, int anoLetivo) {
        if (siglaUC == null || siglaUC.trim().isEmpty()) return;
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
        DALUtil.adicionarLinhaCSV(caminho(), numMec + ";" + siglaUC.trim() + ";" + anoLetivo);
    }

    @Override
    public void removerInscricao(int numMec, String siglaUC, int anoLetivo) {
        if (siglaUC == null || siglaUC.trim().isEmpty()) return;
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        List<String> novas = new ArrayList<>();

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) { novas.add(linha); continue; }
            String[] dados = linha.split(";", -1);
            if (dados.length >= 3) {
                try {
                    if (Integer.parseInt(dados[0].trim()) == numMec
                            && dados[1].trim().equalsIgnoreCase(siglaUC.trim())
                            && Integer.parseInt(dados[2].trim()) == anoLetivo) {
                        continue;
                    }
                } catch (NumberFormatException ignored) {}
            }
            novas.add(linha);
        }
        DALUtil.reescreverFicheiro(caminho(), novas);
    }

    @Override
    public List<String> obterSiglasUcsPorAluno(int numMec, int anoLetivo) {
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        List<String> siglas = new ArrayList<>();

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 3) {
                try {
                    if (Integer.parseInt(dados[0].trim()) == numMec
                            && Integer.parseInt(dados[2].trim()) == anoLetivo) {
                        siglas.add(dados[1].trim());
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return siglas;
    }

    @Override
    public List<String> obterSiglasUcsPorAlunoTodosAnos(int numMec) {
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        List<String> siglas = new ArrayList<>();

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 2) {
                try {
                    if (Integer.parseInt(dados[0].trim()) == numMec) {
                        siglas.add(dados[1].trim());
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return siglas;
    }

    @Override
    public List<Integer> obterAlunosPorUc(String siglaUC, int anoLetivo) {
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        List<Integer> alunos = new ArrayList<>();

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 3 && dados[1].trim().equalsIgnoreCase(siglaUC)) {
                try {
                    if (Integer.parseInt(dados[2].trim()) == anoLetivo) {
                        alunos.add(Integer.parseInt(dados[0].trim()));
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return alunos;
    }

    @Override
    public List<Integer> obterAlunosPorUcTodosAnos(String siglaUC) {
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        List<Integer> alunos = new ArrayList<>();

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 2 && dados[1].trim().equalsIgnoreCase(siglaUC)) {
                try {
                    alunos.add(Integer.parseInt(dados[0].trim()));
                } catch (NumberFormatException ignored) {}
            }
        }
        return alunos;
    }

    @Override
    public void removerInscricoesPorAluno(int numMec) {
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        List<String> novas = new ArrayList<>();

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) {
                novas.add(linha);
                continue;
            }
            String[] dados = linha.split(";", -1);
            if (dados.length >= 1) {
                try {
                    if (Integer.parseInt(dados[0].trim()) == numMec) {
                        continue; // ignora, ou seja, remove
                    }
                } catch (NumberFormatException ignored) {}
            }
            novas.add(linha);
        }
        DALUtil.reescreverFicheiro(caminho(), novas);
    }
}

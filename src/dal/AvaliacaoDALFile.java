package dal;

import common.ConfigApp;
import model.Avaliacao;
import model.UnidadeCurricular;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação ficheiro (CSV) de {@link AvaliacaoDAL}.
 * Persiste em avaliacoes.csv na pasta de dados configurada.
 * Formato das colunas: numMec;siglaUC;anoLetivo;nota1;nota2;nota3.
 */
public class AvaliacaoDALFile implements AvaliacaoDAL {

    private UcDAL ucDALInstance;
    private UcDAL ucDAL() {
        if (ucDALInstance == null)
            ucDALInstance = ConfigApp.isModoSql() ? new UcDALSql() : new UcDALFile();
        return ucDALInstance;
    }

    private static final String NOME_FICHEIRO = "avaliacoes.csv";
    private static final String CABECALHO = "numMec;siglaUC;anoLetivo;nota1;nota2;nota3";

    private final String pastaBase;

    public AvaliacaoDALFile() {
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
    public boolean existeAvaliacao(int numMec, String siglaUc, int anoLetivo) {
        List<String> linhas = DALUtil.lerFicheiro(caminho());

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 3) {
                try {
                    if (Integer.parseInt(dados[0].trim()) == numMec
                            && dados[1].trim().equalsIgnoreCase(siglaUc)
                            && Integer.parseInt(dados[2].trim()) == anoLetivo) {
                        return true;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return false;
    }

    @Override
    public void adicionarAvaliacao(Avaliacao avaliacao, int numMec) {
        if (avaliacao == null || avaliacao.getUc() == null) return;
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);

        int total = avaliacao.getTotalAvaliacoesLancadas();
        StringBuilder linha = new StringBuilder();
        linha.append(numMec).append(";")
                .append(avaliacao.getUc().getSigla()).append(";")
                .append(avaliacao.getAnoLetivo());

        for (int i = 0; i < total; i++) {
            linha.append(";").append(avaliacao.getResultados()[i]);
        }
        DALUtil.adicionarLinhaCSV(caminho(), linha.toString());
    }

    @Override
    public List<Avaliacao> obterAvaliacoesPorAluno(int numMec) {
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        List<Avaliacao> avaliacoesDoAluno = new ArrayList<>();

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;

            String[] dados = linha.split(";", -1);
            if (dados.length >= 4) {
                try {
                    if (Integer.parseInt(dados[0].trim()) == numMec) {
                        UnidadeCurricular uc = ucDAL().procurarUC(dados[1].trim(), pastaBase);
                        if (uc != null) {
                            Avaliacao av = new Avaliacao(uc, Integer.parseInt(dados[2].trim()));

                            if (!dados[3].trim().isEmpty()) av.adicionarResultado(Double.parseDouble(dados[3].trim()));
                            if (dados.length > 4 && !dados[4].trim().isEmpty()) av.adicionarResultado(Double.parseDouble(dados[4].trim()));
                            if (dados.length > 5 && !dados[5].trim().isEmpty()) av.adicionarResultado(Double.parseDouble(dados[5].trim()));

                            avaliacoesDoAluno.add(av);
                        }
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return avaliacoesDoAluno;
    }

    @Override
    public Avaliacao obterAvaliacao(int numMec, String siglaUc, int ano) {
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 3) {
                try {
                    int num = Integer.parseInt(dados[0].trim());
                    String sigla = dados[1].trim();
                    int anoLido = Integer.parseInt(dados[2].trim());
                    if (num == numMec && sigla.equalsIgnoreCase(siglaUc) && anoLido == ano) {
                        UnidadeCurricular uc = ucDAL().procurarUC(siglaUc, pastaBase);
                        if (uc == null) uc = new UnidadeCurricular(siglaUc, "", ano, null);
                        Avaliacao av = new Avaliacao(uc, ano);
                        for (int i = 3; i < dados.length; i++) {
                            if (!dados[i].trim().isEmpty()) {
                                av.adicionarResultado(Double.parseDouble(dados[i].trim().replace(",", ".")));
                            }
                        }
                        return av;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }

    @Override
    public void atualizarAvaliacao(Avaliacao aval, int numMec) {
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        List<String> novasLinhas = new ArrayList<>();

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) {
                novasLinhas.add(linha);
                continue;
            }
            String[] dados = linha.split(";", -1);
            if (dados.length >= 3) {
                try {
                    int num = Integer.parseInt(dados[0].trim());
                    String sigla = dados[1].trim();
                    int ano = Integer.parseInt(dados[2].trim());
                    if (num == numMec && sigla.equalsIgnoreCase(aval.getUc().getSigla()) && ano == aval.getAnoLetivo()) {
                        StringBuilder novaLinha = new StringBuilder();
                        novaLinha.append(numMec).append(";")
                                .append(aval.getUc().getSigla()).append(";")
                                .append(aval.getAnoLetivo());
                        for (int i = 0; i < aval.getTotalAvaliacoesLancadas(); i++) {
                            novaLinha.append(";").append(aval.getResultados()[i]);
                        }
                        novasLinhas.add(novaLinha.toString());
                        continue;
                    }
                } catch (NumberFormatException e) {
                    novasLinhas.add(linha);
                    continue;
                }
            }
            novasLinhas.add(linha);
        }
        DALUtil.reescreverFicheiro(caminho(), novasLinhas);
    }
}

package dal;

import common.ConfigApp;
import model.Pagamento;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação ficheiro (CSV) de {@link PagamentoDAL}.
 * Persiste em pagamentos.csv na pasta de dados configurada.
 * Formato das colunas: numMec;valorPago;dataPagamento.
 */
public class PagamentoDALFile implements PagamentoDAL {

    private static final String NOME_FICHEIRO = "pagamentos.csv";
    private static final String CABECALHO = "numMec;valorPago;dataPagamento";

    private final String pastaBase;

    public PagamentoDALFile() {
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
    public void adicionarPagamento(int numMec, double valorPago, String dataPagamento) {
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
        String linha = numMec + ";" + valorPago + ";" + dataPagamento;
        DALUtil.adicionarLinhaCSV(caminho(), linha);
    }

    @Override
    public List<Pagamento> carregarPagamentosPorAluno(int numMec) {
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        List<Pagamento> pagamentos = new ArrayList<>();

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 3) {
                try {
                    int idAluno = Integer.parseInt(dados[0].trim());
                    if (idAluno == numMec) {
                        double valor = Double.parseDouble(dados[1].trim());
                        String data = dados[2].trim();
                        pagamentos.add(new Pagamento(idAluno, valor, data));
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return pagamentos;
    }
}

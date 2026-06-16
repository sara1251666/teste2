package bll;

import common.ConfigApp;

import dal.EstudanteDAL;
import dal.EstudanteDALFile;
import dal.EstudanteDALSql;
import dal.PagamentoDAL;
import dal.PagamentoDALFile;
import dal.PagamentoDALSql;
import model.Estudante;
import model.Pagamento;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Lógica de negócio financeira do sistema.
 * Processa pagamentos de propinas totais ou parciais e garante
 * a consistência entre o saldo em memória e a persistência em ficheiro.
 */
public class PagamentoBLL {

    private static final String PASTA_BD = ConfigApp.PASTA_BD;
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // Instância da DAL criada aqui
    private final EstudanteDAL estudanteDAL = ConfigApp.isModoSql() ? new EstudanteDALSql() : new EstudanteDALFile();
    private final PagamentoDAL pagamentoDAL =
            ConfigApp.isModoSql() ? new PagamentoDALSql() : new PagamentoDALFile();

    public PagamentoBLL() {
        pagamentoDAL.inicializar();
    }

    /**
     * Processa um pagamento de propina total ou parcial.
     * Deduz o montante do saldo do estudante, regista o pagamento
     * em memória e persiste em estudantes.csv e pagamentos.csv.
     * @param estudante Estudante que efetua o pagamento.
     * @param valor     Montante a pagar; deve ser positivo e não exceder o saldo.
     * @return true se o pagamento foi processado com sucesso.
     */
    public boolean processarPagamento(Estudante estudante, double valor) {
        if (valor <= 0 || valor > estudante.getSaldoDevedor()) {
            return false;
        }

        estudante.efetuarPagamento(valor);

        String dataHoje = LocalDate.now().format(FORMATO_DATA);
        Pagamento registo = new Pagamento(estudante.getNumeroMecanografico(), valor, dataHoje);
        estudante.adicionarPagamento(registo);

        estudanteDAL.atualizarEstudante(estudante);
        pagamentoDAL.adicionarPagamento(estudante.getNumeroMecanografico(), valor, dataHoje);

        return true;
    }
}
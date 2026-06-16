package dal;

import model.Pagamento;

import java.util.List;

/**
 * Contrato de acesso ao histórico de pagamentos de propinas.
 * Duas implementações intermutáveis, escolhidas em runtime via
 * {@link common.ConfigApp#isModoSql()}:
 * <ul>
 *     <li>{@link PagamentoDALFile} — persiste em pagamentos.csv</li>
 *     <li>{@link PagamentoDALSql} — persiste na tabela [pagamento]</li>
 * </ul>
 */
public interface PagamentoDAL {

    /** Garante que a tabela/ficheiro existe (e importa dados do CSV se necessário em modo SQL). */
    void inicializar();

    /**
     * Regista um novo pagamento de propina.
     * @param numMec        Número mecanográfico do estudante.
     * @param valorPago     Montante pago em euros.
     * @param dataPagamento Data do pagamento (DD-MM-AAAA).
     */
    void adicionarPagamento(int numMec, double valorPago, String dataPagamento);

    /**
     * Carrega todos os pagamentos de um estudante por ordem de registo.
     */
    List<Pagamento> carregarPagamentosPorAluno(int numMec);
}

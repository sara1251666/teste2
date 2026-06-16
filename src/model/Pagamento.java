package model;

/**
 * Registo imutável de um pagamento de propina efetuado por um estudante.
 * Cada pagamento — total ou parcial — origina um novo objeto
 * que é adicionado ao histórico do estudante.
 */
public class Pagamento {

    private int idAluno;
    private double valorPago;
    private String dataPagamento; // Formato DD-MM-AAAA


    /**
     * Cria um registo de pagamento.
     * @param idAluno       Número mecanográfico do estudante.
     * @param valorPago     Montante pago em euros.
     * @param dataPagamento Data do pagamento (DD-MM-AAAA).
     */
    public Pagamento(int idAluno, double valorPago, String dataPagamento) {
        this.idAluno = idAluno;
        this.valorPago = valorPago;
        this.dataPagamento = dataPagamento;
    }

    // ---------- GETTERS / SETTERS ----------

    /** @return Montante pago em euros. */
    public double getValorPago()       { return valorPago; }

    /** @return Data do pagamento (DD-MM-AAAA). */
    public String getDataPagamento()   { return dataPagamento; }

    /**
     * @return Pagamento formatado como "valor€ em data".
     */
    @Override
    public String toString() {
        return String.format("%.2f€ em %s", valorPago, dataPagamento);
    }
}
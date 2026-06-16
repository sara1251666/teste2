package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa um estudante matriculado no sistema.
 * Para além dos dados pessoais herdados de Utilizador, mantém
 * o número mecanográfico, o ano curricular, o percurso académico
 * e o histórico de pagamentos de propinas.
 * Um valor de anoCurricular superior a 3 indica que o estudante concluiu o curso.
 */
public class Estudante extends Utilizador {

    private final int numeroMecanografico;
    private final int anoPrimeiraInscricao;
    private int anoCurricular;
    private final PercursoAcademico percurso;
    private double saldoDevedor;
    private String siglaCurso;
    private final List<Pagamento> historicoPagamentos;
    private int totalPagamentos;


    /**
     * Cria um estudante com todos os dados de identificação e matrícula.
     * @param numeroMecanografico  Identificador único gerado automaticamente.
     * @param email                Email institucional gerado pelo sistema.
     * @param password             Hash PBKDF2 da palavra-chave inicial.
     * @param nome                 Nome completo.
     * @param nif                  NIF com 9 dígitos.
     * @param morada               Morada de residência.
     * @param dataNascimento       Data de nascimento (DD-MM-AAAA).
     * @param anoPrimeiraInscricao Ano letivo de entrada no sistema.
     */
    public Estudante(int numeroMecanografico, String email, String password, String nome,
                     String nif, String morada, String dataNascimento, int anoPrimeiraInscricao) {
        super(email, password, nome, nif, morada, dataNascimento);
        this.numeroMecanografico = numeroMecanografico;
        this.anoPrimeiraInscricao = anoPrimeiraInscricao;
        this.anoCurricular = 1;
        this.percurso = new PercursoAcademico();
        this.historicoPagamentos = new ArrayList<>();
        this.totalPagamentos = 0;
    }

    // ---------- GETTERS ----------

    /** @return Número mecanográfico único do estudante. */
    public int getNumeroMecanografico(){ return numeroMecanografico; }

    /** @return Ano letivo em que o estudante se inscreveu pela primeira vez. */
    public int getAnoPrimeiraInscricao(){ return anoPrimeiraInscricao; }

    /** @return Ano curricular atual. Valor superior a 3 indica curso concluído. */
    public int getAnoCurricular(){ return anoCurricular; }

    /** @return Percurso académico com inscrições e histórico de avaliações. */
    public PercursoAcademico getPercurso(){ return percurso; }

    /** @return Valor em dívida de propinas do ano letivo corrente. */
    public double getSaldoDevedor(){ return saldoDevedor; }

    /** @return Sigla do curso em que o estudante está matriculado. */
    public String getSiglaCurso(){ return siglaCurso; }

    /** @return Array com o histórico de pagamentos de propinas. */
    public List<Pagamento> getHistoricoPagamentos(){ return historicoPagamentos; }

    /** @return Número de pagamentos registados no histórico. */
    public int getTotalPagamentos(){ return historicoPagamentos.size(); }


    // ---------- SETTERS ----------

    /** @param anoCurricular Novo ano curricular. */
    public void setAnoCurricular(int anoCurricular)  { this.anoCurricular = anoCurricular; }

    /** @param saldoDevedor Novo saldo devedor em euros. */
    public void setSaldoDevedor(double saldoDevedor) { this.saldoDevedor = saldoDevedor; }

    /** @param siglaCurso Sigla do curso atribuído. */
    public void setSiglaCurso(String siglaCurso)     { this.siglaCurso = siglaCurso; }



    /**
     * Adiciona um pagamento ao histórico em memória.
     * @param pagamento Pagamento a registar.
     */
    public void adicionarPagamento(Pagamento pagamento) {
        historicoPagamentos.add(pagamento);
    }

    /**
     * Deduz o valor pago ao saldo devedor corrente.
     * Ignorado se o valor não for positivo ou exceder a dívida.
     * @param valor Montante a subtrair.
     */
    public void efetuarPagamento(double valor) {
        if (valor > 0 && valor <= this.saldoDevedor) {
            this.saldoDevedor -= valor;
        }
    }

    /**
     * @return Número mecanográfico e nome no formato "numMec - nome".
     */
    @Override
    public String toString() {
        return numeroMecanografico + " - " + getNome();
    }
}
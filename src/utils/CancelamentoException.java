package utils;

/**
 * Exceção lançada quando o utilizador digita "sair" para abandonar
 * um formulário a meio do preenchimento.
 * Os controllers apanham-na nos blocos try/catch dos seus fluxos de interação.
 */
public class CancelamentoException extends RuntimeException {

    /** Cria a exceção com a mensagem padrão de cancelamento. */
    public CancelamentoException() {
        super("Operação cancelada pelo utilizador. A regressar ao menu...");
    }
}
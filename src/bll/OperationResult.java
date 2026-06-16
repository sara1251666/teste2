package bll;

/**
 * Resultado de uma operação que pode ser transacional.
 * Indica sucesso ou falha, com uma mensagem descritiva.
 */
public class OperationResult {
    private final boolean sucesso;
    private final String mensagem;

    private OperationResult(boolean sucesso, String mensagem) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
    }

    public static OperationResult sucesso(String mensagem) {
        return new OperationResult(true, mensagem);
    }

    public static OperationResult falha(String mensagem) {
        return new OperationResult(false, mensagem);
    }

    public boolean isSucesso() { return sucesso; }
    public String getMensagem() { return mensagem; }

    @Override
    public String toString() {
        return (sucesso ? "[OK] " : "[ERRO] ") + mensagem;
    }
}
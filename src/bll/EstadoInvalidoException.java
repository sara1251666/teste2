package bll;

/**
 * Lançada quando uma operação no ciclo de vida do Ano Letivo é tentada
 * sobre um estado que não a permite.
 * Exemplos:
 *   - Tentar fechar um ano que ainda está em PLANEAMENTO
 *   - Tentar iniciar um ano que tem cursos sem quórum
 *   - Tentar editar um ano que já foi iniciado
 */
public class EstadoInvalidoException extends RuntimeException {

    public EstadoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
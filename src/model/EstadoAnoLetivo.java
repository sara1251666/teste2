package model;

/**
 * Estados possíveis no ciclo de vida de um Ano Letivo.
 * Fluxo:
 *   PLANEAMENTO → INICIADO → FECHADO
 */
public enum EstadoAnoLetivo {

    /**
     * Ano criado mas ainda não arrancou. Permite edição.
     */
    PLANEAMENTO,

    INICIADO,

    FECHADO
}

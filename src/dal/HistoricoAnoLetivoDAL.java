package dal;

import model.AnoLetivo;
import java.util.List;

/**
 * Contrato de persistência do histórico de anos letivos fechados.
 * Duas implementações: {@link HistoricoAnoLetivoDALFile} e {@link HistoricoAnoLetivoDALSql}.
 */
public interface HistoricoAnoLetivoDAL {

    /** Cria tabela/ficheiro se não existir; importa CSV se vazio. */
    void inicializar();

    /**
     * Arquiva um ano letivo fechado. Ignora duplicados (mesmo ano já arquivado).
     * @param anoLetivo Ano letivo com estado FECHADO.
     */
    void arquivar(AnoLetivo anoLetivo);

    /**
     * Lista todos os anos arquivados formatados para apresentação.
     * Formato: "Ano XXXX | Estado: ESTADO | Arquivado em: DD-MM-YYYY"
     */
    List<String> listar();
}

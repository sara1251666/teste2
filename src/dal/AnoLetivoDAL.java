package dal;

import model.AnoLetivo;
import java.util.List;

/**
 * Contrato de persistência do módulo Ano Letivo.
 * Duas implementações intermutáveis: {@link AnoLetivoDALFile} e {@link AnoLetivoDALSql},
 * escolhidas em runtime via {@link common.ConfigApp#isModoSql()}.
 */
public interface AnoLetivoDAL {
    /** Cria tabela/ficheiro se não existir; importa CSV se a tabela estiver vazia. */
    void inicializar();

    void adicionar(AnoLetivo ano);
    void atualizar(AnoLetivo ano);
    boolean remover(int ano);

    AnoLetivo procurarPorAno(int ano);
    List<AnoLetivo> listarTodos();

    /** Devolve o ano letivo mais recente (maior número), independentemente do estado. */
    AnoLetivo obterAnoAtivo();
}

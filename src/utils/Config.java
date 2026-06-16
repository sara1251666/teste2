package utils;

import common.ConfigApp;
import dal.AnoLetivoDAL;
import dal.AnoLetivoDALFile;
import dal.AnoLetivoDALSql;
import model.AnoLetivo;

import java.util.List;

/**
 * Configuração centralizada do projeto.
 */
public final class Config {

    private Config() {}

    /**
     * Pasta onde estão guardados todos os ficheiros CSV da aplicação.
     * Valor centralizado em {@link ConfigApp#PASTA_BD} (lê config.properties).
     */
    public static final String PASTA_BD = ConfigApp.PASTA_BD;

    /**
     * Devolve o ano letivo ativo segundo AnoLetivoDAL.
     * Útil para BLLs que precisam do ano atual sem terem acesso a RepositorioDados.
     *
     * @return Ano ativo (PLANEAMENTO ou INICIADO); se todos estiverem FECHADO,
     *         devolve o mais recente registado; em último caso 2026.
     */
    public static int getAnoAtual() {
        AnoLetivoDAL dal = ConfigApp.isModoSql() ? new AnoLetivoDALSql() : new AnoLetivoDALFile();

        AnoLetivo ativo = dal.obterAnoAtivo();
        if (ativo != null) return ativo.getAno();

        List<AnoLetivo> todos = dal.listarTodos();
        int max = 0;
        for (AnoLetivo a : todos) {
            if (a.getAno() > max) max = a.getAno();
        }
        return max > 0 ? max : 2026;
    }
}

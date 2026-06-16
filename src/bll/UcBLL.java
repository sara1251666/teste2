package bll;

import common.ConfigApp;
import dal.CursoDALFile;
import dal.CursoDALSql;
import dal.UcDALFile;
import dal.UcDALSql;

import model.UnidadeCurricular;
import model.Docente;
import model.Curso;
import dal.UcDAL;
import dal.DocenteDALFile;
import dal.CursoDAL;

/**
 * Lógica de negócio para a entidade UnidadeCurricular.
 * Constrói objetos UC com docente e cursos associados
 * e fornece listagens para os menus de seleção dos controllers.
 */
public class UcBLL {

    private static final String PASTA_BD = ConfigApp.PASTA_BD;
    private final CursoDAL cursoDAL = ConfigApp.isModoSql() ? new CursoDALSql() : new CursoDALFile();
    private final UcDAL ucDAL = ConfigApp.isModoSql() ? new UcDALSql() : new UcDALFile();

    /**
     * Constrói e devolve uma UC com docente e cursos associados.
     * @param sigla Sigla da UC a pesquisar.
     * @return A UC construída, ou null se não existir.
     */
    public UnidadeCurricular procurarUCCompleta(String sigla) {
        String[] dados = ucDAL.obterDadosBrutosUC(sigla, PASTA_BD);
        if (dados == null) return null;

        try {
            String siglaUc   = dados[0].trim();
            String nomeUc    = dados[1].trim();
            int ano          = Integer.parseInt(dados[2].trim());
            String siglaDoc  = dados[3].trim();

            Docente docResponsavel = new DocenteDALFile().procurarPorSigla(siglaDoc);
            UnidadeCurricular uc = new UnidadeCurricular(siglaUc, nomeUc, ano, docResponsavel);

            if (dados.length >= 5
                    && !dados[4].trim().equalsIgnoreCase("N/A")
                    && !dados[4].trim().isEmpty()) {
                Curso curso = cursoDAL.procurarCurso(dados[4].trim(), PASTA_BD);
                if (curso != null) uc.adicionarCurso(curso);
            }

            // Ler número de momentos (coluna 6, retrocompatível)
            if (dados.length >= 7 && !dados[6].trim().isEmpty()) {
                try {
                    int momentos = Integer.parseInt(dados[6].trim());
                    if (momentos > 1) uc.setNumMomentos(momentos);
                } catch (NumberFormatException ignored) {}
            }

            return uc;

        } catch (NumberFormatException e) {
            System.err.println(">> Erro na BLL ao construir a UC " + sigla + ": ano inválido.");
            return null;
        }
    }

    /**
     * Devolve um array "SIGLA - Nome" de todas as UCs para menus de seleção.
     * @return Array de strings.
     */
    public String[] obterListaUcs() {
        return ucDAL.obterListaUcs(PASTA_BD);
    }
}
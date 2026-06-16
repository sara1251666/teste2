package dal;

import model.UnidadeCurricular;
import model.Docente;
import java.util.List;

/**
 * Contrato de acesso aos dados de unidades curriculares.
 * Implementado por {@link UcDALFile} (CSV) e {@link UcDALSql} (SQL Server).
 *
 * A escolha da implementação é feita pelos chamadores através de
 * {@code ConfigApp.isModoSql() ? new UcDALSql() : new UcDALFile()}.
 *
 * Os parâmetros {@code pastaBase} são mantidos por compatibilidade; a
 * implementação SQL ignora-os nas queries.
 */
public interface UcDAL {

    /** Garante o suporte de dados (cria tabela e migra ucs.csv se necessário). */
    void inicializar();

    String[] obterDadosBrutosUC(String sigla, String pastaBase);

    UnidadeCurricular procurarUC(String sigla, String pastaBase);

    String[] obterListaUcs(String pastaBase);

    String[] obterListaUcsPorCurso(String siglaCurso, String pastaBase);

    int obterMomentos(String siglaUc, String pastaBase);

    String listarTodasUc(String pastaBase);

    String listarUcsPorCurso(String siglaCurso, String pastaBase);

    List<String> obterSiglasUcsPorDocente(String siglaDocente, String pastaBase);

    List<UnidadeCurricular> obterUcsPorDocente(Docente docente, String pastaBase);

    List<String> obterSiglasUcsPorCursoEAno(String siglaCurso, int ano, String pastaBase);

    int contarUcsPorCursoEAno(String siglaCurso, int ano, String pastaBase);

    List<String> obterCursosPorUc(String siglaUc, String pastaBase);

    String listarUcsDetalhadas(String pastaBase, int anoLetivoAtual);

    void adicionarUC(UnidadeCurricular uc, String siglaCurso, String pastaBase);

    void atualizarMomentos(String siglaUc, int numMomentos, String pastaBase);

    boolean removerUC(String siglaUc, String pastaBase);

    boolean removerAssociacaoUcCurso(String siglaUc, String siglaCurso, String pastaBase);

    boolean temCursoAssociado(String siglaUc, String pastaBase);
}

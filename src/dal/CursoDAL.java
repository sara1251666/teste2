package dal;

import model.Curso;
import java.util.List;

/**
 * Contrato de acesso aos dados de cursos.
 * Implementado por {@link CursoDALFile} (CSV) e {@link CursoDALSql} (SQL Server).
 *
 * A escolha da implementação é feita pelos chamadores através de
 * {@code ConfigApp.isModoSql() ? new CursoDALSql() : new CursoDALFile()}.
 *
 * Os parâmetros {@code pastaBase} são mantidos por compatibilidade com os
 * call sites existentes; a implementação SQL ignora-os nas queries.
 */
public interface CursoDAL {

    /** Garante o suporte de dados (cria tabela e migra cursos.csv se necessário). */
    void inicializar();

    void adicionarCurso(Curso curso, String pastaBase);

    void atualizarCurso(Curso cursoAtualizado, String pastaBase);

    boolean removerCurso(String sigla, String pastaBase);

    String[] obterDadosBrutosCurso(String sigla, String pastaBase);

    Curso procurarCurso(String sigla, String pastaBase);

    String[] obterListaCursos(String pastaBase);

    String listarCursosDetalhados(String pastaBase, int anoLetivoAtual);

    List<Curso> carregarTodos(String pastaBase);
}

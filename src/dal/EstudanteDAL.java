package dal;

import model.Estudante;
import java.util.List;

/**
 * Contrato de acesso aos dados de estudantes.
 * Implementado por {@link EstudanteDALFile} (CSV) e {@link EstudanteDALSql} (SQL Server).
 *
 * A escolha da implementação é feita pelos chamadores através de
 * {@code ConfigApp.isModoSql() ? new EstudanteDALSql() : new EstudanteDALFile()}.
 */
public interface EstudanteDAL {

    /** Garante que o suporte de dados existe (cria ficheiro/tabela e migra se necessário). */
    void inicializar();

    /** Persiste um novo estudante. */
    void adicionarEstudante(Estudante estudante, String siglaCurso);

    /** Atualiza o registo de um estudante existente. */
    void atualizarEstudante(Estudante estudante);

    /** Carrega o perfil de um estudante pelo email (usado no login). */
    Estudante carregarPerfil(String email, String hash);

    /** Procura um estudante pelo número mecanográfico. */
    Estudante procurarPorNumMec(int numMec);

    /** Carrega todos os estudantes com dados básicos. */
    List<Estudante> carregarTodos();

    /** Mantido por retrocompatibilidade. Faz o mesmo que carregarTodos(). */
    List<Estudante> carregarTodosBasico();

    /** Conta os estudantes de um curso num dado ano curricular. */
    int contarEstudantesPorCursoEAno(String siglaCurso, int anoCurricular);

    /** Calcula o próximo número mecanográfico disponível. */
    int obterProximoNumeroMecanografico(int anoAtual);

    /** Verifica se já existe um estudante com o NIF indicado. */
    boolean existeNif(String nif);

    /** Remove um estudante pelo número mecanográfico. */
    boolean removerEstudante(int numMec);
}

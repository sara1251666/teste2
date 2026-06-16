package dal;

import model.Docente;
import java.util.List;

/**
 * Contrato de acesso aos dados de docentes.
 * Implementado por {@link DocenteDALFile} (CSV) e {@link DocenteDALSql} (SQL Server).
 */
public interface DocenteDAL {
    void inicializar();

    Docente procurarPorEmail(String email, String hash);
    Docente procurarPorSigla(String sigla);

    List<Docente> carregarTodos();
    String[] obterListaDocentes();

    boolean adicionarDocente(Docente docente);
    boolean atualizarDocente(Docente docente);
    boolean removerDocente(String sigla);

    boolean existeSigla(String sigla);
    boolean existeNif(String nif);
    boolean temUcAtribuida(String sigla);

    int contar();
}

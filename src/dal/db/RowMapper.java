package dal.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Interface funcional que converte a linha atual de um {@link ResultSet}
 * num objeto do tipo T. Igual ao exemplo do professor.
 *
 * Usada por {@link ConnectionManager#select(String, RowMapper, Object...)}.
 *
 * @param <T> Tipo do objeto de domínio a construir a partir da linha.
 */
@FunctionalInterface
public interface RowMapper<T> {

    /**
     * Constrói um objeto a partir da linha atual do ResultSet.
     * Não deve chamar rs.next(); o ConnectionManager controla a iteração.
     *
     * @param rs ResultSet posicionado numa linha válida.
     * @return Objeto mapeado.
     * @throws SQLException se a leitura de alguma coluna falhar.
     */
    T map(ResultSet rs) throws SQLException;
}

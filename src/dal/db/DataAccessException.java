package dal.db;

/**
 * Exceção não verificada que encapsula falhas de acesso à base de dados
 * (SQLException), para que as camadas DAL/Controller não tenham de declarar
 * throws SQLException e mantenham paridade com a implementação em ficheiros.
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}

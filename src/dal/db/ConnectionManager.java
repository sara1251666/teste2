package dal.db;

import common.ConfigApp;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestor genérico de acesso ao SQL Server, adaptado do exemplo do professor.
 *
 * Mantém a API genérica do exemplo:
 *   - {@link #select(String, RowMapper, Object...)}  — consultas parametrizadas
 *   - {@link #create(String, Object...)}             — INSERT, devolve a chave gerada
 *   - {@link #update(String, Object...)}             — UPDATE/DELETE, devolve linhas afetadas
 * com suporte a transações ({@link #beginTransaction()}, {@link #commit()},
 * {@link #rollback()}) e {@link PreparedStatement} com parâmetros ?.
 *
 * A configuração (servidor, base de dados, utilizador, password) vem de
 * {@link ConfigApp} (que lê config.properties), substituindo o Dotenv do exemplo.
 *
 * Fora de uma transação cada operação abre e fecha a sua própria ligação.
 * Dentro de uma transação reutiliza-se a mesma ligação até commit/rollback.
 */
public class ConnectionManager {

    static {
        java.security.Security.setProperty("jdk.tls.disabledAlgorithms", "");
        System.setProperty("jdk.tls.client.protocols", "TLSv1,TLSv1.1,TLSv1.2");

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.err.println(">> AVISO: driver mssql-jdbc não encontrado no classpath. "
                    + "Adicione o JAR a lib/ para usar o modo SQL.");
        }
    }

    private final String url;
    private final String user;
    private final String password;

    /** Ligação ativa apenas durante uma transação; null caso contrário. */
    private Connection transacao;

    public ConnectionManager() {
        this(ConfigApp.jdbcUrl(), ConfigApp.DB_USER, ConfigApp.DB_PASSWORD);
    }

    public ConnectionManager(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    // ------------------------------------------------------------------
    //  API genérica
    // ------------------------------------------------------------------

    /**
     * Executa uma consulta e mapeia cada linha com o RowMapper fornecido.
     * @param sql    SQL com placeholders ? para os parâmetros.
     * @param mapper Converte cada linha do ResultSet num objeto T.
     * @param params Valores a ligar aos placeholders, pela ordem.
     * @return Lista de objetos mapeados (vazia se não houver resultados).
     */
    public <T> List<T> select(String sql, RowMapper<T> mapper, Object... params) {
        Connection c = obterLigacao();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ligarParametros(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> resultados = new ArrayList<>();
                while (rs.next()) {
                    resultados.add(mapper.map(rs));
                }
                return resultados;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Erro no SELECT: " + sql, e);
        } finally {
            fecharSeNaoTransacional(c);
        }
    }

    /**
     * Executa um INSERT e devolve a chave primária gerada (IDENTITY).
     * @return Id gerado, ou -1 se não existir chave gerada.
     */
    public int create(String sql, Object... params) {
        Connection c = obterLigacao();
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ligarParametros(ps, params);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Erro no INSERT: " + sql, e);
        } finally {
            fecharSeNaoTransacional(c);
        }
    }

    /**
     * Executa um UPDATE ou DELETE parametrizado.
     * @return Número de linhas afetadas.
     */
    public int update(String sql, Object... params) {
        Connection c = obterLigacao();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ligarParametros(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Erro no UPDATE/DELETE: " + sql, e);
        } finally {
            fecharSeNaoTransacional(c);
        }
    }

    // ------------------------------------------------------------------
    //  Apoio à inicialização (DDL / scripts)
    // ------------------------------------------------------------------

    /** Indica se uma tabela existe na base de dados (via metadata). */
    public boolean existeTabela(String nome) {
        Connection c = obterLigacao();
        try {
            DatabaseMetaData md = c.getMetaData();
            try (ResultSet rs = md.getTables(null, null, nome, new String[]{"TABLE"})) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Erro ao verificar tabela: " + nome, e);
        } finally {
            fecharSeNaoTransacional(c);
        }
    }

    /**
     * Executa um script DDL com várias instruções (separadas por ';' ou por linhas 'GO').
     * Linhas de comentário (--) são ignoradas.
     */
    public void executarScript(String script) {
        Connection c = obterLigacao();
        try (Statement st = c.createStatement()) {
            for (String instrucao : dividirInstrucoes(script)) {
                if (!instrucao.isBlank()) {
                    st.execute(instrucao);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Erro ao executar script DDL", e);
        } finally {
            fecharSeNaoTransacional(c);
        }
    }

    // ------------------------------------------------------------------
    //  Transações
    // ------------------------------------------------------------------

    public void beginTransaction() {
        if (transacao != null) {
            throw new DataAccessException("Já existe uma transação ativa", null);
        }
        try {
            transacao = DriverManager.getConnection(url, user, password);
            transacao.setAutoCommit(false);
        } catch (SQLException e) {
            transacao = null;
            throw new DataAccessException("Falha ao iniciar transação", e);
        }
    }

    public void commit() {
        if (transacao == null) return;
        try {
            transacao.commit();
        } catch (SQLException e) {
            throw new DataAccessException("Falha no commit", e);
        } finally {
            terminarTransacao();
        }
    }

    public void rollback() {
        if (transacao == null) return;
        try {
            transacao.rollback();
        } catch (SQLException e) {
            throw new DataAccessException("Falha no rollback", e);
        } finally {
            terminarTransacao();
        }
    }

    // ------------------------------------------------------------------
    //  Interno
    // ------------------------------------------------------------------

    private Connection obterLigacao() {
        if (transacao != null) return transacao;
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new DataAccessException("Falha ao ligar ao SQL Server (" + url + ")", e);
        }
    }

    private void fecharSeNaoTransacional(Connection c) {
        if (transacao == null && c != null) {
            try {
                c.close();
            } catch (SQLException ignored) {
                // fecho best-effort
            }
        }
    }

    private void terminarTransacao() {
        try {
            transacao.setAutoCommit(true);
            transacao.close();
        } catch (SQLException ignored) {
            // best-effort
        } finally {
            transacao = null;
        }
    }

    private static void ligarParametros(PreparedStatement ps, Object... params) throws SQLException {
        if (params == null) return;
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    private static List<String> dividirInstrucoes(String script) {
        List<String> instrucoes = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        for (String linha : script.split("\\r?\\n")) {
            String semComentario = linha;
            int idx = semComentario.indexOf("--");
            if (idx >= 0) semComentario = semComentario.substring(0, idx);

            if (semComentario.trim().equalsIgnoreCase("GO")) {
                instrucoes.add(atual.toString());
                atual.setLength(0);
                continue;
            }
            atual.append(semComentario).append('\n');
            if (semComentario.trim().endsWith(";")) {
                instrucoes.add(atual.toString());
                atual.setLength(0);
            }
        }
        if (!atual.toString().isBlank()) {
            instrucoes.add(atual.toString());
        }
        return instrucoes;
    }
}

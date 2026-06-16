package dal;

import common.ConfigApp;
import model.LoginModel;
import utils.SegurancaPasswords;

import java.util.List;

/**
 * Contrato de persistência do módulo Login. Tem duas implementações
 * intermutáveis — {@link LoginDALSql} e {@link LoginDALFile} — escolhidas em
 * tempo de execução pelo {@link controller.LoginController} consoante config.properties.
 *
 * Ambas garantem paridade total: autenticar (via procurarPorEmail), criar,
 * atualizar, listar e eliminar comportam-se de forma idêntica.
 */
public interface LoginDAL {

    /**
     * Prepara o armazenamento: cria a tabela/ficheiro se necessário e,
     * se estiver vazio, popula com o administrador de arranque.
     */
    void inicializar();

    /** Devolve o registo do email indicado, ou null se não existir. */
    LoginModel procurarPorEmail(String email);

    /** Devolve todos os registos de login. */
    List<LoginModel> listarTodos();

    /** Persiste um novo registo e devolve-o com id atribuído. */
    LoginModel criar(LoginModel novo);

    /** Atualiza o registo com o email de {@code login}. Devolve true se afetou alguma linha. */
    boolean atualizar(LoginModel login);

    /** Remove o registo do email indicado. Devolve true se removeu. */
    boolean eliminar(String email);

    /** Indica se já existe um registo com o email indicado. */
    boolean existe(String email);

    /** Número total de registos. */
    int contar();

    /**
     * Constrói o registo do administrador de arranque usando PBKDF2 (algoritmo do projeto).
     * A password vem de admin.password no config.properties (por omissão "admin123").
     */
    static LoginModel adminPorOmissao() {
        String passwordLimpa = ConfigApp.get("admin.password", "admin123");
        String credencial = SegurancaPasswords.gerarCredencialMista(passwordLimpa);
        int colon = credencial.indexOf(':');
        String salt = credencial.substring(0, colon);
        String hash = credencial.substring(colon + 1);
        return new LoginModel(ConfigApp.ADMIN_EMAIL, hash, salt, "GESTOR", true);
    }
}

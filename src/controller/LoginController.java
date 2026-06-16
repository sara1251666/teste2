package controller;

import common.ConfigApp;
import dal.LoginDAL;
import dal.LoginDALFile;
import dal.LoginDALSql;
import model.LoginModel;
import utils.SegurancaPasswords;

import java.util.List;

/**
 * Controlador do módulo Login. Chamado pela View; não tem BLL — a "lógica" é só
 * validação e hashing, centralizados em {@link SegurancaPasswords}.
 *
 * Decide em tempo de execução qual implementação de {@link LoginDAL} usar,
 * consoante login.persistence.mode no config.properties.
 *
 * As BLLs/Controllers das outras entidades devem delegar aqui a autenticação e
 * gestão de credenciais, em vez de acederem a credenciais.csv ou à tabela [login]
 * diretamente.
 */
public class LoginController {

    private final LoginDAL dal;

    public LoginController() {
        this.dal = ConfigApp.isModoSql() ? new LoginDALSql() : new LoginDALFile();
    }

    /** Construtor para injeção de dependência (testes). */
    public LoginController(LoginDAL dal) {
        this.dal = dal;
    }

    /** Garante o armazenamento pronto (tabela/ficheiro + seed do admin). */
    public void inicializar() {
        dal.inicializar();
    }

    /**
     * Autentica um utilizador via PBKDF2 (algoritmo do projeto).
     * @return O LoginModel se as credenciais forem válidas e a conta estiver ativa; null caso contrário.
     */
    public LoginModel autenticar(String email, String passwordLimpa) {
        if (email == null || passwordLimpa == null) return null;
        LoginModel m = dal.procurarPorEmail(email);
        if (m == null || !m.isAtivo()) return null;
        String combinado = m.getPasswordSalt() + ":" + m.getPasswordHash();
        return SegurancaPasswords.verificarPassword(passwordLimpa, combinado) ? m : null;
    }

    /**
     * Cria uma nova credencial com PBKDF2.
     * @return false se o email já existir ou os dados forem inválidos.
     */
    public boolean criarCredencial(String email, String passwordLimpa, String tipoUtilizador) {
        if (email == null || email.isBlank() || passwordLimpa == null || passwordLimpa.isEmpty()) return false;
        if (dal.existe(email)) return false;
        String credencial = SegurancaPasswords.gerarCredencialMista(passwordLimpa);
        int colon = credencial.indexOf(':');
        String salt = credencial.substring(0, colon);
        String hash = credencial.substring(colon + 1);
        dal.criar(new LoginModel(email, hash, salt, tipoUtilizador, true));
        return true;
    }

    /**
     * Atualiza a password de um utilizador existente (gera novo salt + hash PBKDF2).
     * @return false se o email não existir.
     */
    public boolean atualizarPassword(String email, String novaPasswordLimpa) {
        LoginModel m = dal.procurarPorEmail(email);
        if (m == null || novaPasswordLimpa == null || novaPasswordLimpa.isEmpty()) return false;
        String credencial = SegurancaPasswords.gerarCredencialMista(novaPasswordLimpa);
        int colon = credencial.indexOf(':');
        m.setPasswordSalt(credencial.substring(0, colon));
        m.setPasswordHash(credencial.substring(colon + 1));
        return dal.atualizar(m);
    }

    /** Ativa ou desativa uma conta sem alterar a password. */
    public boolean definirAtivo(String email, boolean ativo) {
        LoginModel m = dal.procurarPorEmail(email);
        if (m == null) return false;
        m.setAtivo(ativo);
        return dal.atualizar(m);
    }

    /** Lista todas as credenciais. */
    public List<LoginModel> listar() {
        return dal.listarTodos();
    }

    /** Remove a credencial de um email. */
    public boolean eliminar(String email) {
        return dal.eliminar(email);
    }

    /** Indica se já existe credencial para o email. */
    public boolean existe(String email) {
        return dal.existe(email);
    }

    /** Acesso ao DAL para uso interno das BLLs que precisam de verificações simples. */
    public LoginDAL dal() {
        return dal;
    }
}

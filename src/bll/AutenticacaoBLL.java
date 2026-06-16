package bll;

import common.ConfigApp;

import dal.*;
import dal.DocenteDAL;
import dal.DocenteDALFile;
import dal.DocenteDALSql;
import dal.GestorDAL;
import dal.GestorDALFile;
import dal.GestorDALSql;
import model.*;
import controller.LoginController;
import java.util.List;


/**
 * Ponto único de autenticação no sistema.
 * Valida as credenciais via {@link LoginController} (SQL ou ficheiro conforme
 * config.properties) e constrói o perfil correto consoante o tipo de utilizador.
 */
public class AutenticacaoBLL {

    private static final String PASTA_BD = ConfigApp.PASTA_BD;
    private final CursoDAL cursoDAL = ConfigApp.isModoSql() ? new CursoDALSql() : new CursoDALFile();
    private final UcDAL ucDAL = ConfigApp.isModoSql() ? new UcDALSql() : new UcDALFile();
    private final EstudanteDAL estudanteDAL = ConfigApp.isModoSql() ? new EstudanteDALSql() : new EstudanteDALFile();
    private final GestorDAL gestorDAL =
            ConfigApp.isModoSql() ? new GestorDALSql() : new GestorDALFile();
    private final DocenteDAL docenteDAL =
            ConfigApp.isModoSql() ? new DocenteDALSql() : new DocenteDALFile();

    private final LoginController loginController = new LoginController();

    /**
     * Autentica um utilizador e devolve o seu perfil completo.
     * Delega a verificação de credenciais no LoginController.
     */
    public Utilizador autenticar(String email, String pass) {
        LoginModel login = loginController.autenticar(email, pass);
        if (login == null) return null;

        String hash = login.getPasswordHash();
        switch (login.getTipoUtilizador()) {
            case "ESTUDANTE":
                return new EstudanteBLL().obterPerfilCompleto(email, hash);

            case "DOCENTE":
                Docente d = docenteDAL.procurarPorEmail(email, hash);
                if (d != null) {
                    List<UnidadeCurricular> ucs = ucDAL.obterUcsPorDocente(d, PASTA_BD);
                    ucs.forEach(d::adicionarUcLecionada);
                }
                return d;

            case "GESTOR":
                return gestorDAL.procurarPorEmail(email, hash);

            default:
                return null;
        }
    }

    /**
     * Recupera a password de um utilizador delegando na PasswordBLL.
     */
    public boolean recuperarPassword(String email) {
        if (!loginController.existe(email)) return false;
        new PasswordBLL().recuperarPassword(email);
        return true;
    }


    /**
     * Executa o processo de auto-matrícula delegando na MatriculaBLL.
     * @param nome       Nome do novo estudante.
     * @param nif        NIF do novo estudante.
     * @param morada     Morada de residência.
     * @param dataNasc   Data de nascimento (DD-MM-AAAA).
     * @param siglaCurso Sigla do curso escolhido.
     * @param anoAtual   Ano letivo atual.
     * @return Array [email, passwordLimpa] com as credenciais geradas.
     */
    public String[] realizarAutoMatricula(String nome, String nif, String morada,
                                          String dataNasc, String siglaCurso, int anoAtual) {
        return new MatriculaBLL().realizarAutoMatricula(
                nome, nif, morada, dataNasc, siglaCurso, anoAtual);
    }

    /**
     * Verifica se um NIF já está registado no sistema.
     * @param nif NIF a verificar.
     * @return true se o NIF já existir.
     */
    public boolean isNifDuplicado(String nif) {
        return estudanteDAL.existeNif(nif)
                || docenteDAL.existeNif(nif);
    }

    /**
     * Devolve a lista de cursos disponíveis para a auto-matrícula.
     * @return Array "SIGLA - Nome" de todos os cursos.
     */
    public String[] obterListaCursos() {
        return cursoDAL.obterListaCursos(PASTA_BD);
    }
}
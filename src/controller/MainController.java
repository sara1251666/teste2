package controller;

import bll.AutenticacaoBLL;
import bll.DepartamentoBLL;
import common.ConfigApp;
import dal.UcDAL;
import dal.UcDALFile;
import dal.UcDALSql;
import dal.CursoDAL;
import dal.CursoDALFile;
import dal.CursoDALSql;
import dal.DocenteDAL;
import dal.DocenteDALFile;
import dal.DocenteDALSql;
import dal.EstudanteDAL;
import dal.EstudanteDALFile;
import dal.EstudanteDALSql;
import dal.GestorDAL;
import dal.GestorDALFile;
import dal.GestorDALSql;
import model.*;
import view.MainView;
import utils.CancelamentoException;
import utils.Validador;

/**
 * Controlador principal que orquestra o arranque do sistema, login e auto-matrícula.
 * Recebe a MainView por parâmetro — é instanciado por MainView.iniciar().
 */
public class MainController {

    private static final String PASTA_BD = ConfigApp.PASTA_BD;
    private final UcDAL ucDAL = ConfigApp.isModoSql() ? new UcDALSql() : new UcDALFile();

    private final MainView view;
    private final RepositorioDados repositorio;
    private AutenticacaoBLL bll;

    public MainController(MainView view) {
        this.view        = view;
        this.repositorio = new RepositorioDados();
    }

    /**
     * Ponto de entrada: arranca o sistema e entra no loop de menu principal.
     */
    public void iniciar() {
        int modo = view.pedirModoPersistencia();
        ConfigApp.definirModo(modo == 2 ? "sql" : "file");
        // AutenticacaoBLL só pode ser criada DEPOIS de definirModo(), pois os
        // seus DALs (LoginController, GestorDAL, DocenteDAL) são escolhidos
        // no momento da construção, consoante ConfigApp.isModoSql().
        this.bll = new AutenticacaoBLL();
        iniciarSistema();
        view.mostrarBemVindo();

        boolean aExecutar = true;
        while (aExecutar) {
            int opcao = view.mostrarMenu();
            switch (opcao) {
                case 1:
                    try {
                        utils.Consola.imprimirTitulo("Login");
                        utils.Consola.imprimirDicaFormulario();
                        String email;
                        do {
                            email = view.pedirInputString("Email institucional");
                        } while (!validarFormatoEmailLogin(email));
                        String pass = view.pedirPassword("Password");
                        processarLogin(email, pass);
                    } catch (CancelamentoException e) {
                        view.mostrarOperacaoCancelada();
                    }
                    break;
                case 2:
                    try {
                        utils.Consola.imprimirTitulo("Recuperar Password");
                        utils.Consola.imprimirDicaFormulario();
                        String emailRecup;
                        do {
                            emailRecup = view.pedirInputString("Email institucional");
                            if (!Validador.isEmailInstitucionalValido(emailRecup))
                                view.mostrarErroLoginSufixo();
                        } while (!Validador.isEmailInstitucionalValido(emailRecup));
                        recuperarPassword(emailRecup);
                    } catch (CancelamentoException e) {
                        view.mostrarOperacaoCancelada();
                    }
                    break;
                case 3:
                    executarAutoMatricula();
                    break;
                case 0:
                    view.mostrarDespedida();
                    aExecutar = false;
                    break;
                default:
                    view.mostrarOpcaoInvalida();
            }
        }
    }

    /**
     * Garante que a estrutura de pastas da base de dados existe.
     */
    private void iniciarSistema() {
        java.io.File pasta = new java.io.File(PASTA_BD);
        if (!pasta.exists() && pasta.mkdirs()) {
            view.mostrarPastaCriada();
        }
        new LoginController().inicializar();
        new DepartamentoBLL().inicializar();
        GestorDAL gestorDAL = ConfigApp.isModoSql() ? new GestorDALSql() : new GestorDALFile();
        gestorDAL.inicializar();
        DocenteDAL docenteDAL = ConfigApp.isModoSql() ? new DocenteDALSql() : new DocenteDALFile();
        docenteDAL.inicializar();
        CursoDAL cursoDAL = ConfigApp.isModoSql() ? new CursoDALSql() : new CursoDALFile();
        cursoDAL.inicializar();
        ucDAL.inicializar();
        EstudanteDAL estudanteDAL = ConfigApp.isModoSql() ? new EstudanteDALSql() : new EstudanteDALFile();
        estudanteDAL.inicializar();
        new bll.AnoLetivoBLL();
    }

    /**
     * Valida se o e-mail tem o formato institucional correto antes de proceder ao login.
     */
    public boolean validarFormatoEmailLogin(String email) {
        if (email.equals("backoffice@issmf.ipp.pt")) {
            return true;
        }
        if (!Validador.validarSufixoLogin(email)) {
            view.mostrarErroLoginSufixo();
            return false;
        }
        return true;
    }

    /**
     * Processa a tentativa de login e redireciona para o controlador específico.
     */
    public void processarLogin(String email, String pass) {
        Utilizador user = bll.autenticar(email, pass);

        if (user == null) {
            view.mostrarCredenciaisInvalidas();
            return;
        }

        repositorio.setUtilizadorLogado(user);

        if (user instanceof Gestor) {
            view.mostrarLoginGestor();
            new GestorController(repositorio, (Gestor) user).iniciar();
        } else if (user instanceof Estudante) {
            view.mostrarLoginEstudante();
            new EstudanteController(repositorio, (Estudante) user).iniciar();
        } else if (user instanceof Docente) {
            view.mostrarLoginDocente();
            new DocenteController(repositorio, (Docente) user).iniciar();
        } else {
            view.mostrarCredenciaisInvalidas();
        }

        repositorio.limparSessao();
    }

    /**
     * Recupera a password de um utilizador e envia por email.
     */
    public void recuperarPassword(String email) {
        boolean sucesso = bll.recuperarPassword(email);
        if (sucesso) {
            view.mostrarSucessoRecuperacao(email);
        } else {
            view.mostrarErroEmailInvalido();
        }
    }

    /**
     * Fluxo completo de auto-matrícula de um novo estudante.
     */
    public void executarAutoMatricula() {
        try {
            view.mostrarTituloAutoMatricula();

            String nome;
            do {
                nome = view.pedirInputString("Nome Completo");
                if (!Validador.isNomeValido(nome)) view.mostrarErroNomeInvalido();
            } while (!Validador.isNomeValido(nome));

            String nif;
            boolean nifInvalido, nifDuplicado;
            do {
                nif          = view.pedirInputString("NIF");
                nifInvalido  = !Validador.validarNif(nif);
                nifDuplicado = !nifInvalido && bll.isNifDuplicado(nif);
                if (nifInvalido)       view.mostrarErroNifInvalido();
                else if (nifDuplicado) view.mostrarErroNifDuplicado();
            } while (nifInvalido || nifDuplicado);

            String morada = view.pedirInputString("Morada");

            String dataNasc;
            boolean dataValida = false;
            do {
                dataNasc = view.pedirInputString("Data de Nascimento (DD-MM-AAAA)");
                int resultado = Validador.validarDataNascimentoDetalhado(dataNasc);
                switch (resultado) {
                    case 0:
                        dataValida = true;
                        break;
                    case 1:
                        view.mostrarErroDataInexistente();
                        break;
                    case 2:
                        view.mostrarErroDataFutura();
                        break;
                    case 3:
                        view.mostrarErroIdadeForaLimites();
                        break;
                }
            } while (!dataValida);

            String[] todosCursos = bll.obterListaCursos();
            java.util.List<String> cursosValidos = new java.util.ArrayList<>();

            for (String cursoStr : todosCursos) {
                String sigla = cursoStr.split(" - ")[0];

                boolean temAno1 = ucDAL.contarUcsPorCursoEAno(sigla, 1, PASTA_BD) > 0;
                boolean temAno2 = ucDAL.contarUcsPorCursoEAno(sigla, 2, PASTA_BD) > 0;
                boolean temAno3 = ucDAL.contarUcsPorCursoEAno(sigla, 3, PASTA_BD) > 0;

                if (temAno1 && temAno2 && temAno3) {
                    cursosValidos.add(cursoStr);
                }
            }

            if (cursosValidos.isEmpty()) {
                view.mostrarErroSemCursos();
                return;
            }

            String[] listaParaExibir = cursosValidos.toArray(new String[0]);
            view.mostrarListaCursosDisponiveis(listaParaExibir);
            int escolha = view.pedirOpcaoCurso(listaParaExibir.length);
            if (escolha == -1) { view.mostrarOperacaoCancelada(); return; }

            String siglaCursoSelected = listaParaExibir[escolha - 1].split(" - ")[0];

            String[] credenciais = bll.realizarAutoMatricula(
                    nome, nif, morada, dataNasc, siglaCursoSelected, repositorio.getAnoAtual());

            if (credenciais != null && credenciais.length > 0) {
                view.mostrarSucessoAutoMatricula(credenciais[0]);
            } else {
                view.mostrarErroEmailInvalido();
            }

        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }
}

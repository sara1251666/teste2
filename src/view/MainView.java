package view;

import controller.MainController;
import utils.Consola;

/**
 * View do ecrã principal: login, recuperação de password e auto-matrícula.
 * Main.java instancia esta View e chama iniciar() — a View cria o MainController.
 */
public class MainView {

    /**
     * Ponto de entrada da aplicação.
     * Cria o MainController, passa-se a si própria, e delega o arranque.
     */
    public void iniciar() {
        new MainController(this).iniciar();
    }


    // ---------- MENU INICIAL ----------

    /**
     * Apresenta o ecrã de seleção do modo de persistência e devolve a escolha:
     * 1 = Ficheiros, 2 = Base de Dados.
     * Repete até receber uma opção válida.
     */
    public int pedirModoPersistencia() {
        while (true) {
            Consola.imprimirCabecalho("Sistema ISSMF - 2025/2026");
            Consola.imprimirTitulo("Modo de Persistência");
            System.out.println("  [1] Ficheiros");
            System.out.println("  [2] Base de Dados (SQL Server)");
            Consola.imprimirLinha();
            try {
                int opcao = Consola.lerOpcaoMenu();
                if (opcao == 1 || opcao == 2) return opcao;
                Consola.imprimirErro("Opção inválida. Escolha 1 ou 2.");
            } catch (Exception e) {
                Consola.imprimirErro("Opção inválida. Escolha 1 ou 2.");
            }
        }
    }

    // ---------- MENU ----------

    public int mostrarMenu() {
        Consola.imprimirCabecalho("Sistema ISSMF - 2025/2026");
        Consola.imprimirMenu(new String[]{
                "Login",
                "Recuperar Password",
                "Matricular Estudante"
        }, "Sair");
        return Consola.lerOpcaoMenu();
    }

    // ---------- INPUTS ----------

    /** Lê um campo de texto — "0" lança CancelamentoException. */
    public String pedirInputString(String mensagem) {
        return Consola.lerString(mensagem);
    }

    /**
     * Lê uma password com mascaramento. Delega para Consola.lerPassword().
     * "0" lança CancelamentoException.
     */
    public String pedirPassword(String mensagem) {
        return Consola.lerPassword(mensagem);
    }

    /**
     * Apresenta lista numerada de cursos e pede seleção.
     * Captura CancelamentoException internamente e devolve -1.
     */
    public int pedirOpcaoCurso(int max) {
        while (true) {
            try {
                int opcao = Consola.lerInt("Número do Curso (1-" + max + ")");
                if (opcao >= 1 && opcao <= max) return opcao;
                Consola.imprimirErro("Opção fora do intervalo.");
            } catch (utils.CancelamentoException e) {
                return -1;
            }
        }
    }

    // ---------- LISTAGENS ----------

    public void mostrarListaCursosDisponiveis(String[] cursos) {
        Consola.imprimirTitulo("Cursos Disponíveis");
        for (int i = 0; i < cursos.length; i++)
            System.out.println("  [" + (i + 1) + "] " + cursos[i]);
        Consola.imprimirLinha();
    }

    // ---------- MENSAGENS ----------

    public void mostrarBemVindo() {
        Consola.imprimirCabecalho("Bem-vindo ao Sistema ISSMF");
        Consola.imprimirInfo("Instituto Superior de Santa Maria da Feira");
        Consola.imprimirLinha();
    }
    public void mostrarPastaCriada()           { Consola.imprimirInfo("Pasta de base de dados criada."); }
    public void mostrarErroLoginSufixo()       { Consola.imprimirErro("O e-mail deve terminar em '@issmf.ipp.pt'."); }
    public void mostrarLoginGestor()           { Consola.imprimirSucesso("Login de Gestor efetuado!"); }
    public void mostrarLoginEstudante()        { Consola.imprimirSucesso("Login de Estudante efetuado!"); }
    public void mostrarLoginDocente()          { Consola.imprimirSucesso("Login de Docente efetuado!"); }
    public void mostrarCredenciaisInvalidas()  { Consola.imprimirErro("Credenciais inválidas."); }
    public void mostrarErroEmailInvalido()     { Consola.imprimirErro("E-mail não reconhecido pelo sistema."); }
    public void mostrarSucessoRecuperacao(String email) { Consola.imprimirSucesso("Password enviada para: " + email); }
    public void mostrarDespedida()             { Consola.imprimirInfo("A encerrar o sistema. Até breve!"); }
    public void mostrarOpcaoInvalida()         { Consola.imprimirErro("Opção inválida."); }
    public void mostrarTituloAutoMatricula() {
        Consola.imprimirCabecalho("Auto-Matrícula");
    }
    public void mostrarErroNomeInvalido()      { Consola.imprimirErro("Nome inválido (apenas letras)."); }
    public void mostrarErroNifInvalido()       { Consola.imprimirErro("NIF inválido (9 dígitos)."); }
    public void mostrarErroNifDuplicado()      { Consola.imprimirErro("NIF já registado no sistema."); }
    public void mostrarErroDataInvalida()      { Consola.imprimirErro("Formato de data inválido (DD-MM-AAAA)."); }
    public void mostrarErroSemCursos()         { Consola.imprimirErro("Não existem cursos ativos no sistema."); }

    public void mostrarSucessoAutoMatricula(String email) {
        Consola.imprimirSucesso("Matrícula concluída com sucesso!");
        Consola.imprimirInfo("Email institucional: " + email);
        Consola.imprimirInfo("As credenciais foram enviadas para o seu email.");
        Consola.pausar();
    }

    public void mostrarOperacaoCancelada() { Consola.imprimirInfo("Operação cancelada. A regressar ao menu..."); }

    public void mostrarErroDataInexistente() {
        Consola.imprimirErro("Data de nascimento inválida (ex: 31-06-2005). Formato correcto: DD-MM-AAAA.");
    }

    public void mostrarErroDataFutura() {
        Consola.imprimirErro("Data de nascimento não pode ser futura.");
    }

    public void mostrarErroIdadeForaLimites() {
        Consola.imprimirErro("Idade deve estar entre 16 e 120 anos.");
    }
}
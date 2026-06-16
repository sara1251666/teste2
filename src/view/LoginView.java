package view;

import common.ConfigApp;
import controller.LoginController;
import model.LoginModel;

import java.io.Console;
import java.util.List;
import java.util.Scanner;

/**
 * View de autenticação do módulo Login. Recolhe input do utilizador e delega
 * tudo no {@link LoginController}, sem conhecer o modo de persistência.
 *
 * Inclui um pequeno menu de gestão de credenciais que serve também para
 * demonstrar a paridade total entre os modos SQL e ficheiros.
 *
 * Pode ser executada isoladamente: {@code java -cp ... view.LoginView}
 */
public class LoginView {

    private final LoginController controller;
    private final Scanner scanner;

    public LoginView() {
        this(new LoginController(), new Scanner(System.in));
    }

    public LoginView(LoginController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    public static void main(String[] args) {
        new LoginView().executar();
    }

    public void executar() {
        System.out.println("=== Módulo Login (modo: " + ConfigApp.LOGIN_PERSISTENCE_MODE + ") ===");
        controller.inicializar();

        boolean continuar = true;
        while (continuar) {
            System.out.println("""
                    \n1) Autenticar
                    2) Listar credenciais
                    3) Criar credencial
                    4) Alterar password
                    5) Eliminar credencial
                    0) Sair""");
            System.out.print("Opção: ");
            switch (lerLinha()) {
                case "1" -> autenticar();
                case "2" -> listar();
                case "3" -> criar();
                case "4" -> alterarPassword();
                case "5" -> eliminar();
                case "0" -> continuar = false;
                default  -> System.out.println("Opção inválida.");
            }
        }
    }

    private void autenticar() {
        String email = pedir("Email");
        String pass = pedirPassword("Password");
        LoginModel m = controller.autenticar(email, pass);
        if (m != null) {
            System.out.println("OK — autenticado como " + m.getTipoUtilizador() + " (" + m.getEmail() + ").");
        } else {
            System.out.println("Credenciais inválidas.");
        }
    }

    private void listar() {
        List<LoginModel> todos = controller.listar();
        if (todos.isEmpty()) {
            System.out.println("(sem credenciais)");
            return;
        }
        todos.forEach(m -> System.out.println("  " + m));
    }

    private void criar() {
        String email = pedir("Email");
        String pass = pedirPassword("Password");
        String tipo = pedir("Tipo (GESTOR/DOCENTE/ESTUDANTE)").toUpperCase();
        System.out.println(controller.criarCredencial(email, pass, tipo)
                ? "Credencial criada." : "Falhou (email já existe ou dados inválidos).");
    }

    private void alterarPassword() {
        String email = pedir("Email");
        String pass = pedirPassword("Nova password");
        System.out.println(controller.atualizarPassword(email, pass)
                ? "Password atualizada." : "Falhou (email não existe).");
    }

    private void eliminar() {
        String email = pedir("Email");
        System.out.println(controller.eliminar(email) ? "Eliminada." : "Falhou (email não existe).");
    }

    // ------------------------------------------------------------------

    private String pedir(String etiqueta) {
        System.out.print(etiqueta + ": ");
        return lerLinha();
    }

    private String pedirPassword(String etiqueta) {
        Console console = System.console();
        if (console != null) {
            char[] pw = console.readPassword(etiqueta + ": ");
            return pw == null ? "" : new String(pw);
        }
        return pedir(etiqueta);
    }

    private String lerLinha() {
        return scanner.hasNextLine() ? scanner.nextLine().trim() : "";
    }
}

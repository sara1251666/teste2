package utils;

import java.io.Console;
import java.util.Scanner;

/**
 * Classe utilitária centralizada para toda a interação com a consola.
 *
 * Regras implementadas:
 *  1. Usa sempre nextLine() + parse manual — nunca nextInt/nextDouble.
 *  2. Inputs inválidos re-apresentam o prompt (loop).
 *  3. "0" em inputs de dados → lança CancelamentoException.
 *  4. Em menus, "0" é opção legítima de saída — usa lerOpcaoMenu().
 *  5. lerPassword() usa System.console() para ocultar a digitação.
 *  6. lerDouble() aceita vírgula ou ponto como separador decimal.
 */
public final class Consola {

    private static final Scanner SCANNER    = new Scanner(System.in);
    private static final int     LARGURA    = 78;

    private Consola() {}

    // =========================================================================
    // APRESENTAÇÃO VISUAL
    // =========================================================================

    /** Limpa o ecrã e imprime um cabeçalho com dupla moldura. */
    public static void imprimirCabecalho(String titulo) {
        limparEcra();
        String texto   = " " + titulo.toUpperCase() + " ";
        int    padding = (LARGURA - 2 - texto.length()) / 2;
        int    resto   = (LARGURA - 2 - texto.length()) % 2;

        System.out.print("╔");
        for (int i = 0; i < LARGURA - 2; i++) System.out.print("═");
        System.out.println("╗");

        System.out.print("║");
        for (int i = 0; i < padding; i++) System.out.print(" ");
        System.out.print(texto);
        for (int i = 0; i < padding + resto; i++) System.out.print(" ");
        System.out.println("║");

        System.out.print("╚");
        for (int i = 0; i < LARGURA - 2; i++) System.out.print("═");
        System.out.println("╝");
    }

    /** Sub-título com moldura simples — não limpa o ecrã. */
    public static void imprimirTitulo(String titulo) {
        String texto   = " " + titulo + " ";
        int    padding = Math.max(0, (LARGURA - 6 - texto.length()) / 2);
        int    resto   = Math.max(0, (LARGURA - 6 - texto.length()) % 2);

        System.out.println();
        System.out.print("  ┌");
        for (int i = 0; i < LARGURA - 6; i++) System.out.print("─");
        System.out.println("┐");

        System.out.print("  │");
        for (int i = 0; i < padding; i++) System.out.print(" ");
        System.out.print(texto);
        for (int i = 0; i < padding + resto; i++) System.out.print(" ");
        System.out.println("│");

        System.out.print("  └");
        for (int i = 0; i < LARGURA - 6; i++) System.out.print("─");
        System.out.println("┘");
    }

    /** Linha horizontal separadora. */
    public static void imprimirLinha() {
        System.out.print("  ");
        for (int i = 0; i < LARGURA - 4; i++) System.out.print("─");
        System.out.println();
    }

    /**
     * Imprime um menu numerado em caixa.
     * Acrescenta automaticamente "[0] Sair / Logout" como última opção.
     *
     * @param opcoes  Opções numeradas de 1 a n.
     * @param labelZero Texto da opção 0 (ex: "Sair / Logout", "Voltar").
     */
    public static void imprimirMenu(String[] opcoes, String labelZero) {
        System.out.println();
        System.out.print("┌");
        for (int i = 0; i < LARGURA - 2; i++) System.out.print("─");
        System.out.println("┐");

        for (int i = 0; i < opcoes.length; i++) {
            String linha = "[" + (i + 1) + "] " + opcoes[i];
            System.out.print("│ ");
            System.out.print(linha);
            int esp = LARGURA - 3 - linha.length();
            for (int j = 0; j < Math.max(0, esp); j++) System.out.print(" ");
            System.out.println("│");
        }

        System.out.print("├");
        for (int i = 0; i < LARGURA - 2; i++) System.out.print("─");
        System.out.println("┤");

        String zero = "[0] " + labelZero;
        System.out.print("│ ");
        System.out.print(zero);
        int espZero = LARGURA - 3 - zero.length();
        for (int j = 0; j < Math.max(0, espZero); j++) System.out.print(" ");
        System.out.println("│");

        System.out.print("└");
        for (int i = 0; i < LARGURA - 2; i++) System.out.print("─");
        System.out.println("┘");
        System.out.println("  Escreva 'sair' para cancelar a operação.");
        imprimirLinha();
    }

    /** Versão de imprimirMenu com label "Voltar" por omissão (sub-menus). */
    public static void imprimirMenu(String[] opcoes) {
        imprimirMenu(opcoes, "Voltar");
    }

    /** Dica de cancelamento — chamar UMA VEZ no início de cada formulário. */
    public static void imprimirDicaFormulario() {
        System.out.println("  Escreva 'sair' para cancelar a operação.");
        imprimirLinha();
    }

    // =========================================================================
    // MENSAGENS FORMATADAS
    // =========================================================================

    public static void imprimirErro(String msg)    { System.out.println("  [ERRO]    " + msg); }
    public static void imprimirSucesso(String msg) { System.out.println("  [OK]      " + msg); }
    public static void imprimirInfo(String msg)    { System.out.println("  [INFO]    " + msg); }

    /** Limpa o terminal (funciona em Unix/Mac; em IDE pode não ter efeito visual). */
    public static void limparEcra() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /** Pausa a execução até o utilizador pressionar ENTER. */
    public static void pausar() {
        System.out.println();
        System.out.print("  Pressione [ENTER] para continuar...");
        SCANNER.nextLine();
    }

    // =========================================================================
    // LEITURA DE DADOS — "sair" cancela (menus usam lerOpcaoMenu onde "0" é opção legítima)
    // =========================================================================

    /**
     * Lê uma String não vazia. "sair" → CancelamentoException.
     * @throws CancelamentoException se o utilizador digitar "sair".
     */
    public static String lerString(String prompt) {
        while (true) {
            System.out.print("  " + prompt + ": ");
            String input = SCANNER.nextLine().trim();
            if (input.equalsIgnoreCase("sair")) throw new CancelamentoException();
            if (!input.isEmpty()) return input;
            imprimirErro("O valor não pode ser vazio. Tente novamente.");
        }
    }

    /**
     * Lê um inteiro. "sair" → CancelamentoException.
     * @throws CancelamentoException se o utilizador digitar "sair".
     */
    public static int lerInt(String prompt) {
        while (true) {
            String input = lerString(prompt);
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                imprimirErro("Número inteiro inválido. Tente novamente.");
            }
        }
    }

    /**
     * Lê um double. "0" → CancelamentoException.
     * Aceita vírgula ou ponto como separador decimal.
     * @throws CancelamentoException se o utilizador digitar "0".
     */
    public static double lerDouble(String prompt) {
        while (true) {
            String input = lerString(prompt).replace(",", ".");
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                imprimirErro("Número decimal inválido (ex: 14.5 ou 14,5). Tente novamente.");
            }
        }
    }

    /**
     * Lê uma nota de avaliação: 0–20 ou -1 para falta.
     * "0" como valor de nota é lido normalmente (é uma nota válida).
     * Para cancelar, o utilizador digita "sair" — não "0" neste caso.
     */
    public static double lerNota(String prompt) {
        while (true) {
            System.out.print("  " + prompt + " (0-20 ou -1 para falta): ");
            String input = SCANNER.nextLine().trim().replace(",", ".");
            if (input.equalsIgnoreCase("sair")) throw new CancelamentoException();
            try {
                double nota = Double.parseDouble(input);
                if (nota == -1 || (nota >= 0 && nota <= 20)) return nota;
                imprimirErro("Nota fora do intervalo. Use 0–20 ou -1 para falta.");
            } catch (NumberFormatException e) {
                imprimirErro("Valor inválido. Ex: 14.5 ou -1.");
            }
        }
    }

    /**
     * Método EXCLUSIVO para leitura de opções de menus.
     * "0" é devolvido normalmente — não lança CancelamentoException.
     * Repete o prompt até o utilizador introduzir um inteiro válido.
     */
    public static int lerOpcaoMenu() {
        while (true) {
            System.out.print("  Opção: ");
            String input = SCANNER.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                imprimirErro("Opção inválida. Introduza um número.");
            }
        }
    }

    /**
     * Lê uma password com mascaramento via System.console().
     * Fallback para Scanner simples se estiver em IDE.
     * @throws CancelamentoException se o utilizador digitar "0".
     */
    public static String lerPassword(String prompt) {
        while (true) {
            Console console = System.console();
            String input;
            if (console != null) {
                char[] chars = console.readPassword("  %s: ", prompt);
                if (chars == null) { imprimirErro("Erro ao ler password."); continue; }
                input = new String(chars).trim();
                java.util.Arrays.fill(chars, '\0');
            } else {
                System.out.print("  " + prompt + " (texto visível — modo IDE): ");
                input = SCANNER.nextLine().trim();
            }
            if (input.isEmpty()) { imprimirErro("A password não pode ser vazia."); continue; }
            if (input.equalsIgnoreCase("sair")) throw new CancelamentoException();
            return input;
        }
    }

    /**
     * Lê uma resposta Sim/Não.
     * @return true para S/SIM, false para N/NÃO.
     */
    public static boolean lerSimNao(String pergunta) {
        while (true) {
            System.out.print("  " + pergunta + " (S/N): ");
            String input = SCANNER.nextLine().trim().toUpperCase();
            if (input.equals("S") || input.equals("SIM")) return true;
            if (input.equals("N") || input.equals("NAO") || input.equals("NÃO")) return false;
            imprimirErro("Responda apenas com 'S' ou 'N'.");
        }
    }

    /**
     * Lê uma string que aceita um Enter vazio (para manter o valor atual).
     * Se o utilizador escrever "sair", cancela a operação.
     */
    public static String lerStringOpcional(String prompt) {
        System.out.print("  " + prompt + ": ");
        String input = new java.util.Scanner(System.in).nextLine().trim();
        if (input.equalsIgnoreCase("sair")) {
            throw new CancelamentoException();
        }
        return input;
    }
}
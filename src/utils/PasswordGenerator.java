package utils;

import java.security.SecureRandom;


/**
 * Gera palavras-passe temporárias criptograficamente seguras.
 * Utiliza SecureRandom para garantir entropia suficiente.
 * As passwords geradas têm 12 caracteres e combinam letras,
 * dígitos e símbolos.
 */
public class PasswordGenerator {

    private static final String CARACTERES_PERMITIDOS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
    private static final int TAMANHO_MINIMO = 12;

    private PasswordGenerator() {}

    /**
     * Gera uma password aleatória com 12 caracteres.
     * @return String com a password gerada.
     */
    public static String gerarPasswordSegura() {
        SecureRandom geradorSeguro = new SecureRandom();
        StringBuilder password = new StringBuilder(TAMANHO_MINIMO);

        for (int i = 0; i < TAMANHO_MINIMO; i++) {
            int index = geradorSeguro.nextInt(CARACTERES_PERMITIDOS.length());
            password.append(CARACTERES_PERMITIDOS.charAt(index));
        }

        return password.toString();
    }
}

package common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Hashing de palavras-passe com SHA-256 + salt aleatório por utilizador.
 *
 * Centraliza toda a lógica de segurança do módulo Login. O salt e o hash são
 * guardados em colunas separadas na tabela [logins] (passwordSalt, passwordHash),
 * ambos codificados em Base64. Para o admin de arranque (config.properties) usa-se
 * o formato combinado salt:hash.
 */
public final class PasswordHasher {

    private static final String ALGORITMO = "SHA-256";
    private static final int TAMANHO_SALT = 16;

    private PasswordHasher() {}

    /** Par (salt, hash) resultante de hashear uma password, ambos em Base64. */
    public record Credencial(String salt, String hash) {
        /** Representação combinada salt:hash (usada no config.properties do admin). */
        public String combinado() {
            return salt + ":" + hash;
        }
    }

    /** Gera um salt criptograficamente aleatório de 16 bytes, codificado em Base64. */
    public static String gerarSalt() {
        byte[] salt = new byte[TAMANHO_SALT];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Deriva o hash SHA-256 de (salt || password).
     * @param password   Password em texto limpo.
     * @param saltBase64 Salt do utilizador em Base64.
     * @return Hash em Base64.
     */
    public static String hash(String password, String saltBase64) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITMO);
            md.update(Base64.getDecoder().decode(saltBase64));
            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo " + ALGORITMO + " indisponível", e);
        }
    }

    /** Cria uma credencial nova (salt aleatório + hash) para a password dada. */
    public static Credencial criar(String password) {
        String salt = gerarSalt();
        return new Credencial(salt, hash(password, salt));
    }

    /**
     * Verifica uma password contra o salt e hash guardados.
     * Comparação em tempo constante para evitar timing attacks.
     */
    public static boolean verificar(String password, String saltBase64, String hashBase64) {
        if (password == null || saltBase64 == null || hashBase64 == null) return false;
        try {
            String calculado = hash(password, saltBase64);
            return MessageDigest.isEqual(
                    calculado.getBytes(StandardCharsets.UTF_8),
                    hashBase64.getBytes(StandardCharsets.UTF_8));
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** Verifica uma password contra uma credencial no formato combinado salt:hash. */
    public static boolean verificarCombinado(String password, String saltHash) {
        if (saltHash == null) return false;
        String[] partes = saltHash.split(":", 2);
        if (partes.length != 2) return false;
        return verificar(password, partes[0], partes[1]);
    }
}

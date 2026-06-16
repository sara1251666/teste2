package utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Utilitário de hashing e verificação de palavras-passe com PBKDF2-HMAC-SHA256.
 * Cada credencial é armazenada no formato salt:hash,
 * onde ambos os valores são codificados em Base64.
 * O salt é gerado aleatoriamente por utilizador.
 */
public class SegurancaPasswords {

    private static final int ITERACOES = 65536;
    private static final int TAMANHO_CHAVE = 256;
    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";

    private SegurancaPasswords() {}

    /**
     * Gera um salt criptograficamente aleatório de 16 bytes.
     * @return Salt codificado em Base64.
     */
    public static String gerarSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Deriva um hash irreversível da password com o salt fornecido.
     * @param password   Password em texto limpo.
     * @param saltBase64 Salt único do utilizador, codificado em Base64.
     * @return Hash resultante codificado em Base64.
     */
    public static String gerarHash(String password, String saltBase64) {
        try {
            byte[] salt = Base64.getDecoder().decode(saltBase64);

            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERACOES, TAMANHO_CHAVE);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITMO);

            byte[] hash = factory.generateSecret(spec).getEncoded();

            return Base64.getEncoder().encodeToString(hash);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Erro crítico: Algoritmo de hashing ou chave inválidos.", e);
        }
    }

    /**
     * Cria a credencial completa no formato salt:hash pronta a persistir.
     * @param passwordLimpa Password em texto limpo.
     * @return String no formato saltBase64:hashBase64.
     */
    public static String gerarCredencialMista(String passwordLimpa) {
        String salt = gerarSalt();
        String hash = gerarHash(passwordLimpa, salt);
        return salt + ":" + hash;
    }

    /**
     * Verifica se a password introduzida corresponde à credencial armazenada.
     * @param passwordIntroduzida Password em texto limpo a verificar.
     * @param credencialMista     Credencial no formato salt:hash.
     * @return true se a password for válida.
     */
    public static boolean verificarPassword(String passwordIntroduzida, String credencialMista) {
        try {
            String[] partes = credencialMista.split(":");
            if (partes.length != 2) return false;

            String saltGuardado = partes[0];
            String hashGuardado = partes[1];

            String novoHash = gerarHash(passwordIntroduzida, saltGuardado);

            return novoHash.equals(hashGuardado);
        } catch (Exception e) {
            return false;
        }
    }
}
package dal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso aos dados de autenticação armazenados em credenciais.csv.
 *
 * Formato do ficheiro (5 colunas):
 *   email;passwordSalt;passwordHash;tipo;ativo
 *
 * A API pública mantém compatibilidade com as BLLs existentes:
 * {@link #obterCredenciais} devolve [salt:hash combinado, tipo] para que as BLLs
 * não precisem de ser alteradas neste sprint. Quando forem migradas para delegar
 * no LoginController, esta classe pode ser removida.
 */
public class CredencialDAL {

    private static final String NOME_FICHEIRO = "credenciais.csv";
    private static final String CABECALHO = "email;passwordSalt;passwordHash;tipo;ativo";

    /**
     * Obtém as credenciais de um utilizador pelo email.
     * @return Array [salt:hash combinado, tipo] se encontrado; null caso contrário.
     */
    public static String[] obterCredenciais(String email, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        for (String linha : DALUtil.lerFicheiro(caminho)) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] d = linha.split(";", -1);
            if (d.length >= 4 && d[0].trim().equalsIgnoreCase(email)) {
                String combinado = d[1].trim() + ":" + d[2].trim();
                return new String[]{combinado, d[3].trim().toUpperCase()};
            }
        }
        return null;
    }

    /**
     * Regista uma nova credencial.
     * @param passwordHash Hash no formato salt:hash (produzido por SegurancaPasswords).
     */
    public static void adicionarCredencial(String email, String passwordHash, String tipo, String pastaBase) {
        if (email == null || passwordHash == null) return;
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        DALUtil.garantirFicheiroECabecalho(caminho, CABECALHO);

        int colon = passwordHash.indexOf(':');
        String salt = colon >= 0 ? passwordHash.substring(0, colon) : "";
        String hash = colon >= 0 ? passwordHash.substring(colon + 1) : passwordHash;

        DALUtil.adicionarLinhaCSV(caminho, email + ";" + salt + ";" + hash + ";" + tipo + ";true");
    }

    /**
     * Atualiza o hash da password de um utilizador existente.
     * @param novaPasswordHash Novo hash no formato salt:hash.
     */
    public static void atualizarPassword(String email, String novaPasswordHash, String pastaBase) {
        if (email == null || novaPasswordHash == null) return;
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        if (linhas.isEmpty()) return;

        int colon = novaPasswordHash.indexOf(':');
        String novoSalt = colon >= 0 ? novaPasswordHash.substring(0, colon) : "";
        String novoHash = colon >= 0 ? novaPasswordHash.substring(colon + 1) : novaPasswordHash;

        List<String> novas = new ArrayList<>();
        boolean atualizado = false;
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) { novas.add(linha); continue; }
            String[] d = linha.split(";", -1);
            if (d.length >= 4 && d[0].trim().equalsIgnoreCase(email)) {
                String ativo = d.length >= 5 ? d[4].trim() : "true";
                novas.add(d[0].trim() + ";" + novoSalt + ";" + novoHash + ";" + d[3].trim() + ";" + ativo);
                atualizado = true;
            } else {
                novas.add(linha);
            }
        }
        if (atualizado) DALUtil.reescreverFicheiro(caminho, novas);
    }

    /**
     * Remove a credencial de um utilizador pelo email.
     */
    public static void removerCredencial(String email, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        List<String> novas = new ArrayList<>();
        boolean encontrou = false;
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) { novas.add(linha); continue; }
            String[] d = linha.split(";", -1);
            if (d.length >= 1 && d[0].trim().equalsIgnoreCase(email)) {
                encontrou = true;
            } else {
                novas.add(linha);
            }
        }
        if (encontrou) DALUtil.reescreverFicheiro(caminho, novas);
    }
}

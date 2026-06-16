package dal;

import common.ConfigApp;
import model.LoginModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação de {@link LoginDAL} sobre o ficheiro credenciais.csv na pasta de dados.
 *
 * Formato CSV (delimitador ';'):
 *   email;passwordSalt;passwordHash;tipo;ativo
 *
 * Utiliza PBKDF2 (via {@link SegurancaPasswords}) — o mesmo algoritmo já
 * presente nos registos existentes — para garantir compatibilidade com o código
 * legado que ainda usa credenciais.csv diretamente.
 */
public class LoginDALFile implements LoginDAL {

    private static final String NOME_FICHEIRO = "credenciais.csv";
    private static final String CABECALHO = "email;passwordSalt;passwordHash;tipo;ativo";

    private String caminho() {
        return ConfigApp.PASTA_BD + File.separator + NOME_FICHEIRO;
    }

    @Override
    public void inicializar() {
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
        LoginModel admin = LoginDAL.adminPorOmissao();
        if (!existe(admin.getEmail())) {
            criar(admin);
        }
    }

    @Override
    public LoginModel procurarPorEmail(String email) {
        if (email == null) return null;
        for (LoginModel m : listarTodos()) {
            if (m.getEmail().equalsIgnoreCase(email)) return m;
        }
        return null;
    }

    @Override
    public List<LoginModel> listarTodos() {
        List<LoginModel> registos = new ArrayList<>();
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            LoginModel m = parse(linha);
            if (m != null) registos.add(m);
        }
        return registos;
    }

    @Override
    public LoginModel criar(LoginModel novo) {
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
        DALUtil.adicionarLinhaCSV(caminho(), serializar(novo));
        return novo;
    }

    @Override
    public boolean atualizar(LoginModel login) {
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        if (linhas.isEmpty()) return false;

        List<String> novas = new ArrayList<>();
        boolean atualizou = false;
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) { novas.add(linha); continue; }
            LoginModel m = parse(linha);
            if (m != null && m.getEmail().equalsIgnoreCase(login.getEmail())) {
                novas.add(serializar(login));
                atualizou = true;
            } else {
                novas.add(linha);
            }
        }
        if (atualizou) DALUtil.reescreverFicheiro(caminho(), novas);
        return atualizou;
    }

    @Override
    public boolean eliminar(String email) {
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        if (linhas.isEmpty()) return false;

        List<String> novas = new ArrayList<>();
        boolean removeu = false;
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) { novas.add(linha); continue; }
            LoginModel m = parse(linha);
            if (m != null && m.getEmail().equalsIgnoreCase(email)) {
                removeu = true;
            } else {
                novas.add(linha);
            }
        }
        if (removeu) DALUtil.reescreverFicheiro(caminho(), novas);
        return removeu;
    }

    @Override
    public boolean existe(String email) {
        return procurarPorEmail(email) != null;
    }

    @Override
    public int contar() {
        return listarTodos().size();
    }

    // ------------------------------------------------------------------

    private static String serializar(LoginModel m) {
        return m.getEmail() + ";"
                + nz(m.getPasswordSalt()) + ";"
                + nz(m.getPasswordHash()) + ";"
                + nz(m.getTipoUtilizador()) + ";"
                + m.isAtivo();
    }

    private static LoginModel parse(String linha) {
        String[] d = linha.split(";", -1);
        if (d.length < 5) return null;
        LoginModel m = new LoginModel(
                d[0].trim(), d[2].trim(), d[1].trim(),
                d[3].trim().toUpperCase(), Boolean.parseBoolean(d[4].trim()));
        return m;
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}

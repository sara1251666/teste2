package dal;

import common.ConfigApp;
import model.Gestor;

import java.io.File;
import java.util.List;

/**
 * Implementação de {@link GestorDAL} sobre o ficheiro gestores.csv.
 * Formato CSV (delimitador ';'): email;nome;nif;morada;dataNascimento
 */
public class GestorDALFile implements GestorDAL {

    private static final String NOME_FICHEIRO = "gestores.csv";
    private static final String CABECALHO     = "email;nome;nif;morada;dataNascimento";

    private String caminho() {
        return ConfigApp.PASTA_BD + File.separator + NOME_FICHEIRO;
    }

    @Override
    public void inicializar() {
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
        // CSV já existe com dados — nada mais a fazer em modo ficheiro
    }

    @Override
    public Gestor procurarPorEmail(String email, String hash) {
        if (email == null) return null;
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] d = linha.split(";", -1);
            if (d.length >= 5 && d[0].trim().equalsIgnoreCase(email)) {
                return new Gestor(email, hash,
                        d[1].trim(), d[2].trim(), d[3].trim(), d[4].trim());
            }
        }
        return null;
    }

    @Override
    public int contar() {
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        int count = 0;
        for (String l : linhas) {
            if (!l.equalsIgnoreCase(CABECALHO) && !l.isBlank()) count++;
        }
        return count;
    }
}

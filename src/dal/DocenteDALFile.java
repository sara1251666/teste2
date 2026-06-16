package dal;

import common.ConfigApp;
import model.Docente;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação ficheiro (CSV) de {@link DocenteDAL}.
 * Persiste em docentes.csv na pasta de dados configurada.
 */
public class DocenteDALFile implements DocenteDAL {

    private UcDAL ucDALInstance;
    private UcDAL ucDAL() {
        if (ucDALInstance == null)
            ucDALInstance = ConfigApp.isModoSql() ? new UcDALSql() : new UcDALFile();
        return ucDALInstance;
    }

    private static final String NOME_FICHEIRO = "docentes.csv";
    private static final String CABECALHO     = "sigla;email;nome;nif;morada;dataNascimento";

    private final String pastaBase;

    public DocenteDALFile() {
        this.pastaBase = ConfigApp.PASTA_BD;
    }

    private String caminho() {
        return pastaBase + File.separator + NOME_FICHEIRO;
    }

    @Override
    public void inicializar() {
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
    }

    @Override
    public Docente procurarPorEmail(String email, String hash) {
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] d = linha.split(";", -1);
            if (d.length >= 6 && d[1].trim().equalsIgnoreCase(email)) {
                return new Docente(d[0].trim(), email, hash,
                        d[2].trim(), d[3].trim(), d[4].trim(), d[5].trim());
            }
        }
        return null;
    }

    @Override
    public Docente procurarPorSigla(String sigla) {
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] d = linha.split(";", -1);
            if (d.length >= 6 && d[0].trim().equalsIgnoreCase(sigla)) {
                return new Docente(d[0].trim(), d[1].trim(), "",
                        d[2].trim(), d[3].trim(), d[4].trim(), d[5].trim());
            }
        }
        return null;
    }

    @Override
    public List<Docente> carregarTodos() {
        List<Docente> lista = new ArrayList<>();
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] d = linha.split(";", -1);
            if (d.length >= 6) {
                lista.add(new Docente(d[0].trim(), d[1].trim(), "",
                        d[2].trim(), d[3].trim(), d[4].trim(), d[5].trim()));
            }
        }
        return lista;
    }

    @Override
    public String[] obterListaDocentes() {
        List<String> lista = new ArrayList<>();
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            String[] d = linha.split(";", -1);
            if (d.length < 3 || d[0].trim().equalsIgnoreCase("sigla")) continue;
            lista.add(d[0].trim() + " - " + d[2].trim());
        }
        return lista.toArray(new String[0]);
    }

    @Override
    public boolean adicionarDocente(Docente docente) {
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
        String linha = docente.getSigla() + ";" + docente.getEmail() + ";"
                + docente.getNome() + ";" + docente.getNif() + ";"
                + docente.getMorada() + ";" + docente.getDataNascimento();
        DALUtil.adicionarLinhaCSV(caminho(), linha);
        return true;
    }

    @Override
    public boolean atualizarDocente(Docente docente) {
        List<String> antigas = DALUtil.lerFicheiro(caminho());
        if (antigas.isEmpty()) return false;

        List<String> novas = new ArrayList<>();
        boolean atualizado = false;
        for (String linha : antigas) {
            if (linha.equalsIgnoreCase(CABECALHO)) { novas.add(linha); continue; }
            String[] d = linha.split(";", -1);
            if (d.length >= 6 && d[0].trim().equalsIgnoreCase(docente.getSigla())) {
                novas.add(docente.getSigla() + ";" + docente.getEmail() + ";"
                        + docente.getNome() + ";" + docente.getNif() + ";"
                        + docente.getMorada() + ";" + docente.getDataNascimento());
                atualizado = true;
            } else {
                novas.add(linha);
            }
        }
        if (atualizado) DALUtil.reescreverFicheiro(caminho(), novas);
        return atualizado;
    }

    @Override
    public boolean removerDocente(String sigla) {
        Docente d = procurarPorSigla(sigla);
        if (d == null) return false;

        CredencialDAL.removerCredencial(d.getEmail(), pastaBase);

        List<String> antigas = DALUtil.lerFicheiro(caminho());
        List<String> novas   = new ArrayList<>();
        boolean encontrou    = false;
        for (String linha : antigas) {
            if (linha.equalsIgnoreCase(CABECALHO)) { novas.add(linha); continue; }
            String[] dados = linha.split(";", -1);
            if (dados.length > 0 && dados[0].trim().equalsIgnoreCase(sigla)) {
                encontrou = true;
            } else {
                novas.add(linha);
            }
        }
        if (encontrou) DALUtil.reescreverFicheiro(caminho(), novas);
        return encontrou;
    }

    @Override
    public boolean existeSigla(String sigla) {
        if (sigla == null || sigla.trim().isEmpty()) return false;
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] d = linha.split(";", -1);
            if (d.length >= 1 && d[0].trim().equalsIgnoreCase(sigla.trim())) return true;
        }
        return false;
    }

    @Override
    public boolean existeNif(String nif) {
        if (nif == null || nif.trim().isEmpty()) return false;
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] d = linha.split(";", -1);
            if (d.length >= 4 && d[3].trim().equals(nif.trim())) return true;
        }
        return false;
    }

    @Override
    public boolean temUcAtribuida(String sigla) {
        return !ucDAL().obterSiglasUcsPorDocente(sigla, pastaBase).isEmpty();
    }

    @Override
    public int contar() {
        int count = 0;
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (!linha.equalsIgnoreCase(CABECALHO) && !linha.trim().isEmpty()) count++;
        }
        return count;
    }
}

package dal;

import common.ConfigApp;
import model.Estudante;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação ficheiro (CSV) de {@link EstudanteDAL}.
 * Persiste em estudantes.csv na pasta de dados configurada.
 * Formato das colunas: numMec; email; nome; nif; morada; dataNascimento;
 * anoInscricao; siglaCurso; saldoDevedor; anoCurricular.
 */
public class EstudanteDALFile implements EstudanteDAL {

    private static final String NOME_FICHEIRO = "estudantes.csv";
    private static final String CABECALHO =
            "numMec;email;nome;nif;morada;dataNascimento;anoInscricao;siglaCurso;saldoDevedor;anoCurricular";

    private final String pastaBase;

    /** Construtor por omissão: usa a pasta de dados configurada. */
    public EstudanteDALFile() {
        this.pastaBase = ConfigApp.PASTA_BD;
    }

    /** Construtor com pasta explícita (retrocompatibilidade). */
    public EstudanteDALFile(String pastaBase) {
        this.pastaBase = pastaBase;
    }

    @Override
    public void inicializar() {
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
    }

    @Override
    public void adicionarEstudante(Estudante estudante, String siglaCurso) {
        if (estudante == null) return;
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
        DALUtil.adicionarLinhaCSV(caminho(), serializar(estudante, siglaCurso));
    }

    @Override
    public void atualizarEstudante(Estudante estudante) {
        if (estudante == null) return;

        List<String> linhasAntigas = DALUtil.lerFicheiro(caminho());
        if (linhasAntigas.isEmpty()) return;

        List<String> linhasNovas = new ArrayList<>();
        boolean atualizou = false;

        for (String linha : linhasAntigas) {
            if (isCabecalho(linha)) { linhasNovas.add(linha); continue; }

            String[] dados = linha.split(";", -1);
            if (dados.length >= 10) {
                try {
                    if (Integer.parseInt(dados[0].trim()) == estudante.getNumeroMecanografico()) {
                        String siglaCurso = (estudante.getSiglaCurso() != null && !estudante.getSiglaCurso().isEmpty())
                                ? estudante.getSiglaCurso() : dados[7].trim();
                        linhasNovas.add(serializar(estudante, siglaCurso));
                        atualizou = true;
                        continue;
                    }
                } catch (NumberFormatException ignored) {}
            }
            linhasNovas.add(linha);
        }
        if (atualizou) DALUtil.reescreverFicheiro(caminho(), linhasNovas);
    }

    @Override
    public Estudante carregarPerfil(String email, String hash) {
        if (email == null) return null;
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (isCabecalho(linha)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 7 && dados[1].trim().equalsIgnoreCase(email)) {
                return deserializar(linha, hash);
            }
        }
        return null;
    }

    @Override
    public Estudante procurarPorNumMec(int numMec) {
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (isCabecalho(linha)) continue;
            Estudante e = deserializar(linha, "");
            if (e != null && e.getNumeroMecanografico() == numMec) return e;
        }
        return null;
    }

    @Override
    public List<Estudante> carregarTodos() {
        List<Estudante> lista = new ArrayList<>();
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (isCabecalho(linha)) continue;
            Estudante e = deserializar(linha, "");
            if (e != null) lista.add(e);
        }
        return lista;
    }

    @Override
    public List<Estudante> carregarTodosBasico() {
        return carregarTodos();
    }

    @Override
    public int contarEstudantesPorCursoEAno(String siglaCurso, int anoCurricular) {
        if (siglaCurso == null) return 0;
        int contagem = 0;
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (isCabecalho(linha)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length > 7 && dados[7].trim().equalsIgnoreCase(siglaCurso)) {
                int anoAluno = (dados.length > 9 && !dados[9].trim().isEmpty())
                        ? Integer.parseInt(dados[9].trim()) : 1;
                if (anoAluno == anoCurricular) contagem++;
            }
        }
        return contagem;
    }

    @Override
    public int obterProximoNumeroMecanografico(int anoAtual) {
        int maxSufixo = 0;
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (isCabecalho(linha)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length > 0 && !dados[0].isEmpty()) {
                try {
                    int numAtual = Integer.parseInt(dados[0].trim());
                    if (numAtual / 10000 == anoAtual) {
                        int sufixo = numAtual % 10000;
                        if (sufixo > maxSufixo) maxSufixo = sufixo;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return (anoAtual * 10000) + (maxSufixo + 1);
    }

    @Override
    public boolean existeNif(String nif) {
        if (nif == null || nif.trim().isEmpty()) return false;
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (isCabecalho(linha)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 4 && dados[3].trim().equals(nif.trim())) return true;
        }
        return false;
    }

    @Override
    public boolean removerEstudante(int numMec) {
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        if (linhas.isEmpty()) return false;

        List<String> novas = new ArrayList<>();
        boolean encontrou = false;

        for (String linha : linhas) {
            if (isCabecalho(linha)) { novas.add(linha); continue; }

            String[] dados = linha.split(";", -1);
            if (dados.length > 0) {
                try {
                    if (Integer.parseInt(dados[0].trim()) == numMec) {
                        encontrou = true;
                        continue;
                    }
                } catch (NumberFormatException ignored) {}
            }
            novas.add(linha);
        }
        if (encontrou) DALUtil.reescreverFicheiro(caminho(), novas);
        return encontrou;
    }

    // ==================================================================
    // Helpers PRIVADOS
    // ==================================================================

    private String caminho() {
        return pastaBase + File.separator + NOME_FICHEIRO;
    }

    private boolean isCabecalho(String linha) {
        if (linha == null) return true;
        String trimmed = linha.trim();
        return trimmed.equalsIgnoreCase(CABECALHO) || trimmed.toLowerCase().startsWith("nummec;");
    }

    private String serializar(Estudante e, String siglaCurso) {
        return e.getNumeroMecanografico() + ";" + e.getEmail() + ";" + e.getNome() + ";"
                + e.getNif() + ";" + e.getMorada() + ";" + e.getDataNascimento() + ";"
                + e.getAnoPrimeiraInscricao() + ";" + (siglaCurso == null ? "" : siglaCurso) + ";"
                + e.getSaldoDevedor() + ";" + e.getAnoCurricular();
    }

    private Estudante deserializar(String linha, String hash) {
        String[] dados = linha.split(";", -1);
        if (dados.length < 7) return null;
        try {
            int numMec  = Integer.parseInt(dados[0].trim());
            int anoInsc = Integer.parseInt(dados[6].trim());

            Estudante e = new Estudante(
                    numMec, dados[1].trim(), hash,
                    dados[2].trim(), dados[3].trim(), dados[4].trim(),
                    dados[5].trim(), anoInsc);

            if (dados.length > 7 && !dados[7].trim().isEmpty()) e.setSiglaCurso(dados[7].trim());
            if (dados.length > 8 && !dados[8].trim().isEmpty()) {
                try { e.setSaldoDevedor(Double.parseDouble(dados[8].trim())); } catch (NumberFormatException ignored) {}
            }
            if (dados.length > 9 && !dados[9].trim().isEmpty()) {
                try { e.setAnoCurricular(Integer.parseInt(dados[9].trim())); } catch (NumberFormatException ignored) {}
            }
            return e;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

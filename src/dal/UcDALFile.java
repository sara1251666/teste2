package dal;

import common.ConfigApp;
import model.UnidadeCurricular;
import model.Docente;
import model.Curso;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implementação ficheiro (CSV) de {@link UcDAL}.
 * Persiste em ucs.csv. Formato:
 *   sigla;nome;anoCurricular;siglaDocenteResponsavel;siglaCurso;ects;momentos
 */
public class UcDALFile implements UcDAL {

    private static final String NOME_FICHEIRO = "ucs.csv";
    private static final String CABECALHO =
            "sigla;nome;anoCurricular;siglaDocenteResponsavel;siglaCurso;ects;momentos";

    private InscricaoDAL inscricaoDALInstance;
    private CursoDAL cursoDALInstance;

    private InscricaoDAL inscricaoDAL() {
        if (inscricaoDALInstance == null) {
            inscricaoDALInstance = ConfigApp.isModoSql() ? new InscricaoDALSql() : new InscricaoDALFile();
            inscricaoDALInstance.inicializar();
        }
        return inscricaoDALInstance;
    }

    private CursoDAL cursoDAL() {
        if (cursoDALInstance == null) {
            cursoDALInstance = ConfigApp.isModoSql() ? new CursoDALSql() : new CursoDALFile();
        }
        return cursoDALInstance;
    }

    @Override
    public void inicializar() {
        DALUtil.garantirFicheiroECabecalho(
                ConfigApp.PASTA_BD + File.separator + NOME_FICHEIRO, CABECALHO);
    }

    @Override
    public String[] obterDadosBrutosUC(String sigla, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        for (String linha : linhas) {
            if (linha.toLowerCase().startsWith("sigla")) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 4 && dados[0].trim().equalsIgnoreCase(sigla)) {
                return dados;
            }
        }
        return null;
    }

    @Override
    public UnidadeCurricular procurarUC(String sigla, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        UnidadeCurricular ucEncontrada = null;

        for (String linha : linhas) {
            String[] dados = linha.split(";", -1);
            if (dados.length >= 4 && dados[0].trim().equalsIgnoreCase(sigla)) {
                if (ucEncontrada == null) {
                    try {
                        int ano  = Integer.parseInt(dados[2].trim());
                        int ects = (dados.length >= 6 && !dados[5].trim().isEmpty())
                                ? Integer.parseInt(dados[5].trim())
                                : model.UnidadeCurricular.ECTS_PADRAO;
                        Docente doc = new DocenteDALFile().procurarPorSigla(dados[3].trim());
                        ucEncontrada = new UnidadeCurricular(dados[0].trim(), dados[1].trim(), ano, doc, ects);
                    } catch (NumberFormatException e) { continue; }
                }
                if (dados.length >= 5 && !dados[4].trim().equalsIgnoreCase("N/A")) {
                    Curso curso = cursoDAL().procurarCurso(dados[4].trim(), pastaBase);
                    if (curso != null) ucEncontrada.adicionarCurso(curso);
                }
            }
        }
        return ucEncontrada;
    }

    @Override
    public String[] obterListaUcs(String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        List<String> lista = new ArrayList<>();
        for (String linha : linhas) {
            String[] dados = linha.split(";", -1);
            if (dados.length < 2 || dados[0].trim().toLowerCase().equals("sigla")) continue;
            String entrada = dados[0].trim() + " - " + dados[1].trim();
            if (!lista.contains(entrada)) lista.add(entrada);
        }
        return lista.toArray(new String[0]);
    }

    @Override
    public String[] obterListaUcsPorCurso(String siglaCurso, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        List<String> resultado = new ArrayList<>();
        for (String linha : linhas) {
            if (linha.toLowerCase().startsWith("sigla")) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 5 && dados[4].trim().equalsIgnoreCase(siglaCurso)) {
                String entrada = dados[0].trim() + " - " + dados[1].trim();
                if (!resultado.contains(entrada)) resultado.add(entrada);
            }
        }
        return resultado.toArray(new String[0]);
    }

    @Override
    public int obterMomentos(String siglaUc, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        for (String linha : linhas) {
            if (linha.toLowerCase().startsWith("sigla")) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 1 && dados[0].trim().equalsIgnoreCase(siglaUc)) {
                if (dados.length >= 7 && !dados[6].trim().isEmpty()) {
                    try { return Integer.parseInt(dados[6].trim()); }
                    catch (NumberFormatException ignored) {}
                }
                return 0;
            }
        }
        return 0;
    }

    @Override
    public String listarTodasUc(String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        StringBuilder sb = new StringBuilder("\n--- LISTA DE UNIDADES CURRICULARES ---\n");
        for (String linha : linhas) {
            if (linha.toLowerCase().startsWith("sigla")) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 5) {
                sb.append("Sigla: ").append(dados[0].trim())
                        .append(" | Nome: ").append(dados[1].trim())
                        .append(" | Ano: ").append(dados[2].trim())
                        .append(" | Docente: ").append(dados[3].trim())
                        .append(" | Curso: ").append(dados[4].trim());
                if (dados.length >= 6 && !dados[5].trim().isEmpty())
                    sb.append(" | ECTS: ").append(dados[5].trim());
                if (dados.length >= 7 && !dados[6].trim().isEmpty())
                    sb.append(" | Momentos: ").append(dados[6].trim());
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    @Override
    public String listarUcsPorCurso(String siglaCurso, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        Map<Integer, List<String>> ucsPorAno = new TreeMap<>();
        for (String linha : linhas) {
            if (linha.toLowerCase().startsWith("sigla")) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 5 && dados[4].trim().equalsIgnoreCase(siglaCurso)) {
                try {
                    int ano = Integer.parseInt(dados[2].trim());
                    ucsPorAno.putIfAbsent(ano, new ArrayList<>());
                    ucsPorAno.get(ano).add("[" + dados[0].trim() + "] " + dados[1].trim()
                            + " (Doc: " + dados[3].trim()
                            + " | ECTS: " + (dados.length >= 6 && !dados[5].trim().isEmpty() ? dados[5].trim() : model.UnidadeCurricular.ECTS_PADRAO) + ")");
                } catch (NumberFormatException ignored) {}
            }
        }
        if (ucsPorAno.isEmpty())
            return ">> Não existem UCs associadas ao curso " + siglaCurso + ".";
        StringBuilder sb = new StringBuilder("\n--- PLANO DE ESTUDOS: " + siglaCurso + " ---\n");
        for (Map.Entry<Integer, List<String>> entry : ucsPorAno.entrySet()) {
            sb.append(">> Ano ").append(entry.getKey()).append(":\n");
            for (String ucStr : entry.getValue())
                sb.append("   - ").append(ucStr).append("\n");
        }
        return sb.toString();
    }

    @Override
    public List<String> obterSiglasUcsPorDocente(String siglaDocente, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        List<String> siglas = new ArrayList<>();
        for (String linha : linhas) {
            if (linha.toLowerCase().startsWith("sigla")) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 4 && dados[3].trim().equalsIgnoreCase(siglaDocente)) {
                siglas.add(dados[0].trim());
            }
        }
        return siglas;
    }

    @Override
    public List<UnidadeCurricular> obterUcsPorDocente(Docente docente, String pastaBase) {
        List<String> siglas = obterSiglasUcsPorDocente(docente.getSigla(), pastaBase);
        List<UnidadeCurricular> ucs = new ArrayList<>();
        for (String sigla : siglas) {
            UnidadeCurricular ucCompleta = procurarUC(sigla, pastaBase);
            if (ucCompleta != null) ucs.add(ucCompleta);
        }
        return ucs;
    }

    @Override
    public List<String> obterSiglasUcsPorCursoEAno(String siglaCurso, int ano, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        List<String> siglas = new ArrayList<>();
        for (String linha : linhas) {
            if (linha.toLowerCase().startsWith("sigla")) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 5) {
                try {
                    int anoCurricular = Integer.parseInt(dados[2].trim());
                    if (anoCurricular == ano && dados[4].trim().equalsIgnoreCase(siglaCurso)) {
                        String sigla = dados[0].trim();
                        if (!siglas.contains(sigla)) siglas.add(sigla);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return siglas;
    }

    @Override
    public int contarUcsPorCursoEAno(String siglaCurso, int ano, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        int contagem = 0;
        for (String linha : linhas) {
            if (linha.toLowerCase().startsWith("sigla")) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 5) {
                try {
                    int anoCurricular = Integer.parseInt(dados[2].trim());
                    if (anoCurricular == ano && dados[4].trim().equalsIgnoreCase(siglaCurso)) contagem++;
                } catch (NumberFormatException ignored) {}
            }
        }
        return contagem;
    }

    @Override
    public List<String> obterCursosPorUc(String siglaUc, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        List<String> cursos = new ArrayList<>();
        for (String linha : linhas) {
            if (linha.toLowerCase().startsWith("sigla")) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 5 && dados[0].trim().equalsIgnoreCase(siglaUc)) {
                String curso = dados[4].trim();
                if (!curso.isEmpty() && !curso.equalsIgnoreCase("N/A") && !cursos.contains(curso)) {
                    cursos.add(curso);
                }
            }
        }
        return cursos;
    }

    @Override
    public String listarUcsDetalhadas(String pastaBase, int anoLetivoAtual) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- PAINEL DE UCS ---\n");
        List<String> ucsProcessadas = new ArrayList<>();

        for (String linha : linhas) {
            if (linha.toLowerCase().startsWith("sigla")) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length < 5) continue;

            String siglaUc = dados[0].trim();
            if (ucsProcessadas.contains(siglaUc)) continue;
            ucsProcessadas.add(siglaUc);

            String nomeUc        = dados[1].trim();
            int anoCurricular;
            try { anoCurricular  = Integer.parseInt(dados[2].trim()); }
            catch (NumberFormatException e) { continue; }
            String docente       = dados[3].trim();

            int qtdAlunos    = inscricaoDAL().obterAlunosPorUc(siglaUc, anoLetivoAtual).size();
            int qtdMomentos  = obterMomentos(siglaUc, pastaBase);

            List<String> cursosAssociados = new ArrayList<>();
            for (String l : linhas) {
                String[] d = l.split(";", -1);
                if (d.length >= 5 && d[0].trim().equalsIgnoreCase(siglaUc)) {
                    String sc = d[4].trim();
                    if (!cursosAssociados.contains(sc)) cursosAssociados.add(sc);
                }
            }

            sb.append(anoLetivoAtual).append(" | ")
                    .append(siglaUc).append(" | ").append(nomeUc).append(" | ")
                    .append(docente).append(" | Momentos: ").append(qtdMomentos)
                    .append(" | Alunos: ").append(qtdAlunos)
                    .append(" | Cursos: ").append(String.join(",", cursosAssociados))
                    .append(" | ").append(anoCurricular).append("º Ano\n");
        }
        return sb.toString();
    }

    @Override
    public void adicionarUC(UnidadeCurricular uc, String siglaCurso, String pastaBase) {
        if (uc == null) return;
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        DALUtil.garantirFicheiroECabecalho(caminho, CABECALHO);
        String siglaDocente = (uc.getDocenteResponsavel() != null)
                ? uc.getDocenteResponsavel().getSigla() : "N/A";
        String cursoStr = (siglaCurso != null && !siglaCurso.isEmpty()) ? siglaCurso : "N/A";
        DALUtil.adicionarLinhaCSV(caminho,
                uc.getSigla() + ";" + uc.getNome() + ";"
                        + uc.getAnoCurricular() + ";" + siglaDocente + ";" + cursoStr
                        + ";" + uc.getEcts() + ";0");
    }

    @Override
    public void atualizarMomentos(String siglaUc, int numMomentos, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhasAntigas = DALUtil.lerFicheiro(caminho);
        if (linhasAntigas.isEmpty()) return;
        List<String> linhasAtualizadas = new ArrayList<>();
        boolean atualizou = false;
        for (String linha : linhasAntigas) {
            if (linha.toLowerCase().startsWith("sigla")) {
                linhasAtualizadas.add(CABECALHO);
                continue;
            }
            String[] dados = linha.split(";", -1);
            if (dados.length >= 1 && dados[0].trim().equalsIgnoreCase(siglaUc)) {
                StringBuilder nova = new StringBuilder();
                for (int i = 0; i < 6; i++) {
                    if (i > 0) nova.append(";");
                    nova.append(i < dados.length ? dados[i] : "");
                }
                nova.append(";").append(numMomentos);
                linhasAtualizadas.add(nova.toString());
                atualizou = true;
            } else {
                linhasAtualizadas.add(linha);
            }
        }
        if (atualizou) DALUtil.reescreverFicheiro(caminho, linhasAtualizadas);
    }

    @Override
    public boolean removerUC(String siglaUc, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhasAntigas = DALUtil.lerFicheiro(caminho);
        if (linhasAntigas.isEmpty()) return false;
        List<String> linhasAtualizadas = new ArrayList<>();
        boolean encontrou = false;
        for (String linha : linhasAntigas) {
            if (linha.toLowerCase().startsWith("sigla")) { linhasAtualizadas.add(linha); continue; }
            String[] dados = linha.split(";", -1);
            if (dados.length > 0 && dados[0].trim().equalsIgnoreCase(siglaUc)) {
                encontrou = true;
            } else {
                linhasAtualizadas.add(linha);
            }
        }
        if (encontrou) DALUtil.reescreverFicheiro(caminho, linhasAtualizadas);
        return encontrou;
    }

    @Override
    public boolean removerAssociacaoUcCurso(String siglaUc, String siglaCurso, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        List<String> novasLinhas = new ArrayList<>();
        boolean encontrou = false;

        int totalLinhasUC = 0;
        for (String linha : linhas) {
            String[] dados = linha.split(";", -1);
            if (dados.length >= 1 && dados[0].trim().equalsIgnoreCase(siglaUc)) totalLinhasUC++;
        }

        for (String linha : linhas) {
            if (linha.toLowerCase().startsWith("sigla")) { novasLinhas.add(linha); continue; }
            String[] dados = linha.split(";", -1);
            if (dados.length >= 5
                    && dados[0].trim().equalsIgnoreCase(siglaUc)
                    && dados[4].trim().equalsIgnoreCase(siglaCurso)) {
                encontrou = true;
                if (totalLinhasUC == 1) {
                    dados[4] = "";
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < dados.length; i++) {
                        if (i > 0) sb.append(";");
                        sb.append(dados[i]);
                    }
                    novasLinhas.add(sb.toString());
                }
                continue;
            }
            novasLinhas.add(linha);
        }
        if (encontrou) DALUtil.reescreverFicheiro(caminho, novasLinhas);
        return encontrou;
    }

    @Override
    public boolean temCursoAssociado(String siglaUc, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        for (String linha : linhas) {
            if (linha.toLowerCase().startsWith("sigla")) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 5 && dados[0].trim().equalsIgnoreCase(siglaUc)) {
                String curso = dados[4].trim();
                if (!curso.isEmpty() && !curso.equalsIgnoreCase("N/A")) return true;
            }
        }
        return false;
    }
}

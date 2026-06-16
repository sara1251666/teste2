package dal;

import common.ConfigApp;
import model.Curso;
import model.Departamento;
import utils.Consola;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação ficheiro (CSV) de {@link CursoDAL}.
 * Persiste em cursos.csv. Formato: sigla;nome;siglaDepartamento;propina;estado.
 */
public class CursoDALFile implements CursoDAL {

    private static final String NOME_FICHEIRO = "cursos.csv";
    private static final String CABECALHO = "sigla;nome;siglaDepartamento;propina;estado";

    private InscricaoDAL inscricaoDALInstance;
    private UcDAL ucDALInstance;

    private InscricaoDAL inscricaoDAL() {
        if (inscricaoDALInstance == null) {
            inscricaoDALInstance = ConfigApp.isModoSql() ? new InscricaoDALSql() : new InscricaoDALFile();
            inscricaoDALInstance.inicializar();
        }
        return inscricaoDALInstance;
    }

    private UcDAL ucDAL() {
        if (ucDALInstance == null) {
            ucDALInstance = ConfigApp.isModoSql() ? new UcDALSql() : new UcDALFile();
        }
        return ucDALInstance;
    }

    @Override
    public void inicializar() {
        DALUtil.garantirFicheiroECabecalho(
                ConfigApp.PASTA_BD + File.separator + NOME_FICHEIRO, CABECALHO);
    }

    @Override
    public void adicionarCurso(Curso curso, String pastaBase) {
        if (curso == null) return;
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        DALUtil.garantirFicheiroECabecalho(caminho, CABECALHO);

        String siglaDep = (curso.getDepartamento() != null)
                ? curso.getDepartamento().getSigla() : "N/A";
        String linha = curso.getSigla() + ";" + curso.getNome() + ";" + siglaDep + ";"
                + curso.getValorPropinaAnual() + ";" + curso.getEstado();

        DALUtil.adicionarLinhaCSV(caminho, linha);
    }

    @Override
    public void atualizarCurso(Curso cursoAtualizado, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhasAntigas = DALUtil.lerFicheiro(caminho);
        if (linhasAntigas.isEmpty()) return;

        List<String> linhasAtualizadas = new ArrayList<>();
        boolean atualizado = false;

        for (String linha : linhasAntigas) {
            if (linha.equalsIgnoreCase(CABECALHO)) { linhasAtualizadas.add(linha); continue; }
            String[] dados = linha.split(";", -1);
            if (dados.length > 0 && dados[0].trim().equalsIgnoreCase(cursoAtualizado.getSigla())) {
                String siglaDep = (cursoAtualizado.getDepartamento() != null)
                        ? cursoAtualizado.getDepartamento().getSigla() : "N/A";
                linhasAtualizadas.add(cursoAtualizado.getSigla() + ";" + cursoAtualizado.getNome() + ";"
                        + siglaDep + ";" + cursoAtualizado.getValorPropinaAnual() + ";"
                        + cursoAtualizado.getEstado());
                atualizado = true;
            } else {
                linhasAtualizadas.add(linha);
            }
        }
        if (atualizado) DALUtil.reescreverFicheiro(caminho, linhasAtualizadas);
    }

    @Override
    public boolean removerCurso(String sigla, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhasAntigas = DALUtil.lerFicheiro(caminho);
        if (linhasAntigas.isEmpty()) return false;

        List<String> linhasAtualizadas = new ArrayList<>();
        boolean encontrou = false;

        for (String linha : linhasAntigas) {
            if (linha.equalsIgnoreCase(CABECALHO)) { linhasAtualizadas.add(linha); continue; }
            String[] dados = linha.split(";", -1);
            if (dados.length > 0 && dados[0].trim().equalsIgnoreCase(sigla)) {
                encontrou = true;
            } else {
                linhasAtualizadas.add(linha);
            }
        }
        if (encontrou) DALUtil.reescreverFicheiro(caminho, linhasAtualizadas);
        return encontrou;
    }

    @Override
    public String[] obterDadosBrutosCurso(String sigla, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 3 && dados[0].trim().equalsIgnoreCase(sigla)) {
                return dados;
            }
        }
        return null;
    }

    @Override
    public Curso procurarCurso(String sigla, String pastaBase) {
        String[] dados = obterDadosBrutosCurso(sigla, pastaBase);
        if (dados == null) return null;

        double propina = 0.0;
        if (dados.length >= 4) {
            try { propina = Double.parseDouble(dados[3].trim()); }
            catch (NumberFormatException ignored) {}
        }

        Departamento dep = new DepartamentoDALFile().procurarPorSigla(
                dados.length >= 3 ? dados[2].trim() : "N/A");

        Curso curso = new Curso(dados[0].trim(), dados[1].trim(), dep, propina);

        if (dados.length >= 5 && !dados[4].trim().isEmpty())
            curso.setEstado(dados[4].trim());

        return curso;
    }

    @Override
    public String[] obterListaCursos(String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        List<String> listaCursos = new ArrayList<>();

        for (String linha : linhas) {
            String[] dados = linha.split(";", -1);
            if (dados.length < 2 || dados[0].trim().equalsIgnoreCase("sigla")) continue;
            listaCursos.add(dados[0].trim() + " - " + dados[1].trim());
        }
        return listaCursos.toArray(new String[0]);
    }

    @Override
    public String listarCursosDetalhados(String pastaBase, int anoLetivoAtual) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        StringBuilder sb = new StringBuilder();

        Consola.imprimirTitulo("PAINEL DE CURSOS");

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length < 3) continue;

            String siglaCurso = dados[0].trim();
            String nomeCurso = dados[1].trim();
            String departamento = dados[2].trim();

            for (int ano = 1; ano <= 3; ano++) {
                int qtdUcs = ucDAL().contarUcsPorCursoEAno(siglaCurso, ano, pastaBase);
                int qtdAlunos = 0;

                List<String> siglasUcs =
                        ucDAL().obterSiglasUcsPorCursoEAno(siglaCurso, ano, pastaBase);

                List<Integer> alunosUnicos = new ArrayList<>();
                for (String siglaUc : siglasUcs) {
                    List<Integer> alunosUc = inscricaoDAL().obterAlunosPorUc(siglaUc, anoLetivoAtual);
                    for (Integer num : alunosUc) {
                        if (!alunosUnicos.contains(num)) alunosUnicos.add(num);
                    }
                }
                qtdAlunos = alunosUnicos.size();

                double propina = 0.0;
                try { propina = Double.parseDouble(dados[3].trim()); }
                catch (NumberFormatException ignored) {}

                sb.append(anoLetivoAtual).append(" | ").append(siglaCurso).append(" | ")
                        .append(nomeCurso).append(" | ").append(departamento).append(" | ")
                        .append(String.format("%.0f€", propina)).append(" | ")
                        .append(qtdAlunos).append(" | ").append(qtdUcs).append(" | ")
                        .append(ano).append("º Ano\n");
            }
        }
        return sb.toString();
    }

    @Override
    public List<Curso> carregarTodos(String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        List<Curso> cursos = new ArrayList<>();

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 3) {
                double propina = 0.0;
                if (dados.length >= 4) {
                    try { propina = Double.parseDouble(dados[3].trim()); }
                    catch (NumberFormatException ignored) {}
                }
                Departamento dep = new DepartamentoDALFile().procurarPorSigla(dados[2].trim());
                Curso c = new Curso(dados[0].trim(), dados[1].trim(), dep, propina);
                if (dados.length >= 5 && !dados[4].trim().isEmpty())
                    c.setEstado(dados[4].trim());
                cursos.add(c);
            }
        }
        return cursos;
    }
}

package dal;

import common.ConfigApp;
import dal.db.ConnectionManager;
import model.UnidadeCurricular;
import model.Docente;
import model.Curso;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implementação SQL Server de {@link UcDAL}.
 * Tabela: [uc] (PK (sigla, siglaCurso), siglaDocente FK -> [docente], siglaCurso FK -> [curso]).
 *
 * A mesma UC pode existir em vários cursos (várias linhas). O schema SQL não
 * guarda ECTS — é assumido {@link UnidadeCurricular#ECTS_PADRAO} ao reconstruir.
 * Inicialização automática: cria a tabela se não existe e importa ucs.csv se vazia.
 * NOTA: depende de [curso] e [docente] já existirem (FKs).
 */
public class UcDALSql implements UcDAL {

    private static final String TABELA = "uc";
    private static final String[] CAMINHOS_SCHEMA = {
            "sql/schema_uc.sql", "LP2-Grupo1/sql/schema_uc.sql", "../sql/schema_uc.sql"
    };

    private final ConnectionManager cm;
    private CursoDAL cursoDALInstance;
    private DocenteDAL docenteDALInstance;
    private InscricaoDAL inscricaoDALInstance;

    public UcDALSql() { this(new ConnectionManager()); }
    public UcDALSql(ConnectionManager cm) { this.cm = cm; }

    private CursoDAL cursoDAL() {
        if (cursoDALInstance == null)
            cursoDALInstance = ConfigApp.isModoSql() ? new CursoDALSql() : new CursoDALFile();
        return cursoDALInstance;
    }
    private DocenteDAL docenteDAL() {
        if (docenteDALInstance == null)
            docenteDALInstance = ConfigApp.isModoSql() ? new DocenteDALSql() : new DocenteDALFile();
        return docenteDALInstance;
    }
    private InscricaoDAL inscricaoDAL() {
        if (inscricaoDALInstance == null) {
            inscricaoDALInstance = ConfigApp.isModoSql() ? new InscricaoDALSql() : new InscricaoDALFile();
            inscricaoDALInstance.inicializar();
        }
        return inscricaoDALInstance;
    }

    @Override
    public void inicializar() {
        if (!cm.existeTabela(TABELA)) {
            cm.executarScript(lerSchema());
        }
        if (contar() == 0) {
            importarDeCsv();
        }
    }

    private int contar() {
        List<Integer> r = cm.select("SELECT COUNT(*) AS total FROM [uc]", rs -> rs.getInt("total"));
        return r.isEmpty() ? 0 : r.get(0);
    }

    @Override
    public String[] obterDadosBrutosUC(String sigla, String pastaBase) {
        List<String[]> r = cm.select(
                "SELECT TOP 1 * FROM [uc] WHERE sigla = ?", this::mapRaw, sigla);
        return r.isEmpty() ? null : r.get(0);
    }

    /** Reconstrói o formato CSV: sigla;nome;ano;siglaDocente;siglaCurso;ects;momentos. */
    private String[] mapRaw(java.sql.ResultSet rs) throws java.sql.SQLException {
        String doc = rs.getString("siglaDocente");
        return new String[]{
                rs.getString("sigla"),
                rs.getString("nome"),
                String.valueOf(rs.getInt("anoCurricular")),
                (doc == null || doc.isEmpty()) ? "N/A" : doc,
                rs.getString("siglaCurso"),
                String.valueOf(UnidadeCurricular.ECTS_PADRAO),
                String.valueOf(rs.getInt("numMomentos"))
        };
    }

    @Override
    public UnidadeCurricular procurarUC(String sigla, String pastaBase) {
        List<String[]> linhas = cm.select("SELECT * FROM [uc] WHERE sigla = ?", this::mapRaw, sigla);
        UnidadeCurricular ucEncontrada = null;
        for (String[] dados : linhas) {
            if (ucEncontrada == null) {
                try {
                    int ano = Integer.parseInt(dados[2].trim());
                    Docente d = docenteDAL().procurarPorSigla(dados[3].trim());
                    ucEncontrada = new UnidadeCurricular(dados[0].trim(), dados[1].trim(), ano, d,
                            UnidadeCurricular.ECTS_PADRAO);
                } catch (NumberFormatException e) { continue; }
            }
            if (dados.length >= 5 && !dados[4].trim().equalsIgnoreCase("N/A")) {
                Curso curso = cursoDAL().procurarCurso(dados[4].trim(), pastaBase);
                if (curso != null) ucEncontrada.adicionarCurso(curso);
            }
        }
        return ucEncontrada;
    }

    @Override
    public String[] obterListaUcs(String pastaBase) {
        List<String> r = cm.select(
                "SELECT DISTINCT sigla, nome FROM [uc] ORDER BY sigla",
                rs -> rs.getString("sigla") + " - " + rs.getString("nome"));
        return r.toArray(new String[0]);
    }

    @Override
    public String[] obterListaUcsPorCurso(String siglaCurso, String pastaBase) {
        List<String> r = cm.select(
                "SELECT DISTINCT sigla, nome FROM [uc] WHERE siglaCurso = ? ORDER BY sigla",
                rs -> rs.getString("sigla") + " - " + rs.getString("nome"), siglaCurso);
        return r.toArray(new String[0]);
    }

    @Override
    public int obterMomentos(String siglaUc, String pastaBase) {
        List<Integer> r = cm.select(
                "SELECT TOP 1 numMomentos FROM [uc] WHERE sigla = ?",
                rs -> rs.getInt("numMomentos"), siglaUc);
        return r.isEmpty() ? 0 : r.get(0);
    }

    @Override
    public String listarTodasUc(String pastaBase) {
        List<String[]> linhas = cm.select("SELECT * FROM [uc] ORDER BY sigla", this::mapRaw);
        StringBuilder sb = new StringBuilder("\n--- LISTA DE UNIDADES CURRICULARES ---\n");
        for (String[] dados : linhas) {
            sb.append("Sigla: ").append(dados[0])
                    .append(" | Nome: ").append(dados[1])
                    .append(" | Ano: ").append(dados[2])
                    .append(" | Docente: ").append(dados[3])
                    .append(" | Curso: ").append(dados[4])
                    .append(" | ECTS: ").append(dados[5])
                    .append(" | Momentos: ").append(dados[6])
                    .append("\n");
        }
        return sb.toString();
    }

    @Override
    public String listarUcsPorCurso(String siglaCurso, String pastaBase) {
        List<String[]> linhas = cm.select(
                "SELECT * FROM [uc] WHERE siglaCurso = ?", this::mapRaw, siglaCurso);
        Map<Integer, List<String>> ucsPorAno = new TreeMap<>();
        for (String[] dados : linhas) {
            try {
                int ano = Integer.parseInt(dados[2].trim());
                ucsPorAno.putIfAbsent(ano, new ArrayList<>());
                ucsPorAno.get(ano).add("[" + dados[0].trim() + "] " + dados[1].trim()
                        + " (Doc: " + dados[3].trim() + " | ECTS: " + dados[5].trim() + ")");
            } catch (NumberFormatException ignored) {}
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
        return cm.select("SELECT sigla FROM [uc] WHERE siglaDocente = ?",
                rs -> rs.getString("sigla"), siglaDocente);
    }

    @Override
    public List<UnidadeCurricular> obterUcsPorDocente(Docente docente, String pastaBase) {
        List<String> siglas = obterSiglasUcsPorDocente(docente.getSigla(), pastaBase);
        List<UnidadeCurricular> ucs = new ArrayList<>();
        List<String> vistas = new ArrayList<>();
        for (String sigla : siglas) {
            if (vistas.contains(sigla)) continue;
            vistas.add(sigla);
            UnidadeCurricular uc = procurarUC(sigla, pastaBase);
            if (uc != null) ucs.add(uc);
        }
        return ucs;
    }

    @Override
    public List<String> obterSiglasUcsPorCursoEAno(String siglaCurso, int ano, String pastaBase) {
        return cm.select(
                "SELECT DISTINCT sigla FROM [uc] WHERE siglaCurso = ? AND anoCurricular = ?",
                rs -> rs.getString("sigla"), siglaCurso, ano);
    }

    @Override
    public int contarUcsPorCursoEAno(String siglaCurso, int ano, String pastaBase) {
        List<Integer> r = cm.select(
                "SELECT COUNT(*) AS total FROM [uc] WHERE siglaCurso = ? AND anoCurricular = ?",
                rs -> rs.getInt("total"), siglaCurso, ano);
        return r.isEmpty() ? 0 : r.get(0);
    }

    @Override
    public List<String> obterCursosPorUc(String siglaUc, String pastaBase) {
        return cm.select(
                "SELECT DISTINCT siglaCurso FROM [uc] WHERE sigla = ? AND siglaCurso IS NOT NULL",
                rs -> rs.getString("siglaCurso"), siglaUc);
    }

    @Override
    public String listarUcsDetalhadas(String pastaBase, int anoLetivoAtual) {
        List<String[]> linhas = cm.select("SELECT * FROM [uc] ORDER BY sigla", this::mapRaw);
        StringBuilder sb = new StringBuilder("\n--- PAINEL DE UCS ---\n");
        List<String> ucsProcessadas = new ArrayList<>();
        for (String[] dados : linhas) {
            String siglaUc = dados[0].trim();
            if (ucsProcessadas.contains(siglaUc)) continue;
            ucsProcessadas.add(siglaUc);

            String nomeUc = dados[1].trim();
            int anoCurricular;
            try { anoCurricular = Integer.parseInt(dados[2].trim()); }
            catch (NumberFormatException e) { continue; }
            String docente = dados[3].trim();

            int qtdAlunos   = inscricaoDAL().obterAlunosPorUc(siglaUc, anoLetivoAtual).size();
            int qtdMomentos = obterMomentos(siglaUc, pastaBase);

            List<String> cursosAssociados = new ArrayList<>();
            for (String[] d : linhas) {
                if (d[0].trim().equalsIgnoreCase(siglaUc)) {
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
        String siglaDocente = (uc.getDocenteResponsavel() != null)
                ? uc.getDocenteResponsavel().getSigla() : null;
        if (siglaDocente != null && siglaDocente.equalsIgnoreCase("N/A")) siglaDocente = null;
        if (siglaDocente != null && docenteDAL().procurarPorSigla(siglaDocente) == null) siglaDocente = null;
        String cursoStr = (siglaCurso != null && !siglaCurso.isEmpty()) ? siglaCurso : null;
        cm.update("INSERT INTO [uc] (sigla, nome, anoCurricular, siglaDocente, siglaCurso, numMomentos) "
                + "VALUES (?, ?, ?, ?, ?, ?)",
                uc.getSigla(), uc.getNome(), uc.getAnoCurricular(),
                siglaDocente, cursoStr, 0);
    }

    @Override
    public void atualizarMomentos(String siglaUc, int numMomentos, String pastaBase) {
        cm.update("UPDATE [uc] SET numMomentos = ? WHERE sigla = ?", numMomentos, siglaUc);
    }

    @Override
    public boolean removerUC(String siglaUc, String pastaBase) {
        return cm.update("DELETE FROM [uc] WHERE sigla = ?", siglaUc) > 0;
    }

    @Override
    public boolean removerAssociacaoUcCurso(String siglaUc, String siglaCurso, String pastaBase) {
        return cm.update("DELETE FROM [uc] WHERE sigla = ? AND siglaCurso = ?",
                siglaUc, siglaCurso) > 0;
    }

    @Override
    public boolean temCursoAssociado(String siglaUc, String pastaBase) {
        List<Integer> r = cm.select(
                "SELECT COUNT(*) AS total FROM [uc] WHERE sigla = ? AND siglaCurso IS NOT NULL",
                rs -> rs.getInt("total"), siglaUc);
        return !r.isEmpty() && r.get(0) > 0;
    }

    // ------------------------------------------------------------------

    private void importarDeCsv() {
        String caminho = ConfigApp.PASTA_BD + java.io.File.separator + "ucs.csv";
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        int total = 0;
        for (String linha : linhas) {
            if (linha.toLowerCase().startsWith("sigla")) continue;
            String[] d = linha.split(";", -1);
            if (d.length < 5) continue;
            String sigla = d[0].trim();
            String nome = d[1].trim();
            String siglaCurso = d[4].trim();
            // Sem curso válido não é possível inserir (siglaCurso NOT NULL + FK).
            if (siglaCurso.isEmpty() || siglaCurso.equalsIgnoreCase("N/A")) continue;
            // Curso inexistente na BD: não dá para inserir (FK). Ignora a linha.
            if (cursoDAL().procurarCurso(siglaCurso, ConfigApp.PASTA_BD) == null) {
                System.out.println(">> UC " + d[0].trim() + " ignorada: curso '" + siglaCurso + "' não existe na BD.");
                continue;
            }
            int ano;
            try { ano = Integer.parseInt(d[2].trim()); } catch (NumberFormatException e) { continue; }
            String siglaDoc = d[3].trim();
            if (siglaDoc.isEmpty() || siglaDoc.equalsIgnoreCase("N/A")) siglaDoc = null;
            // Docente inexistente na BD: insere NULL (FK siglaDocente é anulável).
            if (siglaDoc != null && docenteDAL().procurarPorSigla(siglaDoc) == null) siglaDoc = null;
            int momentos = 0;
            if (d.length >= 7 && !d[6].trim().isEmpty()) {
                try { momentos = Integer.parseInt(d[6].trim()); } catch (NumberFormatException ignored) {}
            }
            cm.update("INSERT INTO [uc] (sigla, nome, anoCurricular, siglaDocente, siglaCurso, numMomentos) "
                    + "VALUES (?, ?, ?, ?, ?, ?)", sigla, nome, ano, siglaDoc, siglaCurso, momentos);
            total++;
        }
        if (total > 0) {
            System.out.println(">> Migração: " + total + " linha(s) de UC importada(s) de ucs.csv para SQL.");
        }
    }

    private static String lerSchema() {
        for (String c : CAMINHOS_SCHEMA) {
            Path p = Path.of(c);
            if (Files.exists(p)) {
                try { return Files.readString(p); }
                catch (IOException e) { throw new dal.db.DataAccessException("Falha ao ler " + p, e); }
            }
        }
        return "CREATE TABLE [uc] (\n"
                + "    sigla         NVARCHAR(10)  NOT NULL,\n"
                + "    nome          NVARCHAR(150) NOT NULL,\n"
                + "    anoCurricular INT           NOT NULL,\n"
                + "    siglaDocente  NVARCHAR(10)  REFERENCES [docente](sigla),\n"
                + "    siglaCurso    NVARCHAR(10)  NOT NULL REFERENCES [curso](sigla),\n"
                + "    numMomentos   INT           NOT NULL DEFAULT 1,\n"
                + "    PRIMARY KEY (sigla, siglaCurso)\n"
                + ");\n";
    }
}

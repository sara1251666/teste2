package dal;

import common.ConfigApp;
import dal.db.ConnectionManager;
import dal.db.RowMapper;
import model.Curso;
import model.Departamento;
import utils.Consola;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação SQL Server de {@link CursoDAL}.
 * Tabela: [curso] (sigla PK, nome, siglaDepartamento FK -> [departamento](sigla), propina, estado).
 *
 * Inicialização automática: cria a tabela se não existe e importa cursos.csv se vazia.
 * NOTA: depende da tabela [departamento] já existir (FK siglaDepartamento).
 * Os parâmetros pastaBase são ignorados nas queries (mantidos por compatibilidade).
 */
public class CursoDALSql implements CursoDAL {

    private static final String TABELA = "curso";
    private static final String[] CAMINHOS_SCHEMA = {
            "sql/schema_curso.sql", "LP2-Grupo1/sql/schema_curso.sql", "../sql/schema_curso.sql"
    };

    private final ConnectionManager cm;
    private DepartamentoDAL departamentoDALInstance;
    private InscricaoDAL inscricaoDALInstance;
    private UcDAL ucDALInstance;

    public CursoDALSql() { this(new ConnectionManager()); }
    public CursoDALSql(ConnectionManager cm) { this.cm = cm; }

    private DepartamentoDAL departamentoDAL() {
        if (departamentoDALInstance == null) {
            departamentoDALInstance = ConfigApp.isModoSql() ? new DepartamentoDALSql() : new DepartamentoDALFile();
        }
        return departamentoDALInstance;
    }

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

    private Curso mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        String siglaDep = rs.getString("siglaDepartamento");
        Departamento dep = departamentoDAL().procurarPorSigla(siglaDep);
        Curso c = new Curso(rs.getString("sigla"), rs.getString("nome"), dep, rs.getDouble("propina"));
        String estado = rs.getString("estado");
        if (estado != null && !estado.isEmpty()) c.setEstado(estado);
        return c;
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
        List<Integer> r = cm.select("SELECT COUNT(*) AS total FROM [curso]", rs -> rs.getInt("total"));
        return r.isEmpty() ? 0 : r.get(0);
    }

    @Override
    public void adicionarCurso(Curso curso, String pastaBase) {
        if (curso == null) return;
        String siglaDep = (curso.getDepartamento() != null) ? curso.getDepartamento().getSigla() : "N/A";
        cm.update("INSERT INTO [curso] (sigla, nome, siglaDepartamento, propina, estado) VALUES (?, ?, ?, ?, ?)",
                curso.getSigla(), curso.getNome(), siglaDep,
                curso.getValorPropinaAnual(), curso.getEstado());
    }

    @Override
    public void atualizarCurso(Curso curso, String pastaBase) {
        if (curso == null) return;
        String siglaDep = (curso.getDepartamento() != null) ? curso.getDepartamento().getSigla() : "N/A";
        cm.update("UPDATE [curso] SET nome = ?, siglaDepartamento = ?, propina = ?, estado = ? WHERE sigla = ?",
                curso.getNome(), siglaDep, curso.getValorPropinaAnual(),
                curso.getEstado(), curso.getSigla());
    }

    @Override
    public boolean removerCurso(String sigla, String pastaBase) {
        return cm.update("DELETE FROM [curso] WHERE sigla = ?", sigla) > 0;
    }

    @Override
    public String[] obterDadosBrutosCurso(String sigla, String pastaBase) {
        List<String[]> r = cm.select(
                "SELECT * FROM [curso] WHERE sigla = ?",
                rs -> new String[]{
                        rs.getString("sigla"), rs.getString("nome"),
                        rs.getString("siglaDepartamento"),
                        String.valueOf(rs.getDouble("propina")), rs.getString("estado")
                }, sigla);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public Curso procurarCurso(String sigla, String pastaBase) {
        List<Curso> r = cm.select("SELECT * FROM [curso] WHERE sigla = ?", this::mapRow, sigla);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public String[] obterListaCursos(String pastaBase) {
        List<String> r = cm.select("SELECT sigla, nome FROM [curso] ORDER BY sigla",
                rs -> rs.getString("sigla") + " - " + rs.getString("nome"));
        return r.toArray(new String[0]);
    }

    @Override
    public String listarCursosDetalhados(String pastaBase, int anoLetivoAtual) {
        List<Curso> cursos = carregarTodos(pastaBase);
        StringBuilder sb = new StringBuilder();
        Consola.imprimirTitulo("PAINEL DE CURSOS");

        for (Curso curso : cursos) {
            String siglaCurso = curso.getSigla();
            String nomeCurso = curso.getNome();
            String departamento = (curso.getDepartamento() != null)
                    ? curso.getDepartamento().getSigla() : "N/A";
            for (int ano = 1; ano <= 3; ano++) {
                int qtdUcs = ucDAL().contarUcsPorCursoEAno(siglaCurso, ano, pastaBase);
                List<String> siglasUcs = ucDAL().obterSiglasUcsPorCursoEAno(siglaCurso, ano, pastaBase);
                List<Integer> alunosUnicos = new ArrayList<>();
                for (String siglaUc : siglasUcs) {
                    for (Integer num : inscricaoDAL().obterAlunosPorUc(siglaUc, anoLetivoAtual)) {
                        if (!alunosUnicos.contains(num)) alunosUnicos.add(num);
                    }
                }
                sb.append(anoLetivoAtual).append(" | ").append(siglaCurso).append(" | ")
                        .append(nomeCurso).append(" | ").append(departamento).append(" | ")
                        .append(String.format("%.0f€", curso.getValorPropinaAnual())).append(" | ")
                        .append(alunosUnicos.size()).append(" | ").append(qtdUcs).append(" | ")
                        .append(ano).append("º Ano\n");
            }
        }
        return sb.toString();
    }

    @Override
    public List<Curso> carregarTodos(String pastaBase) {
        return cm.select("SELECT * FROM [curso] ORDER BY sigla", this::mapRow);
    }

    // ------------------------------------------------------------------

    private void importarDeCsv() {
        List<Curso> doFicheiro = new CursoDALFile().carregarTodos(ConfigApp.PASTA_BD);
        if (doFicheiro.isEmpty()) return;
        int total = 0;
        for (Curso c : doFicheiro) {
            if (procurarCurso(c.getSigla(), ConfigApp.PASTA_BD) == null) {
                adicionarCurso(c, ConfigApp.PASTA_BD);
                total++;
            }
        }
        if (total > 0) {
            System.out.println(">> Migração: " + total + " curso(s) importado(s) de cursos.csv para SQL.");
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
        return "CREATE TABLE [curso] (\n"
                + "    sigla             NVARCHAR(10)  NOT NULL PRIMARY KEY,\n"
                + "    nome              NVARCHAR(150) NOT NULL,\n"
                + "    siglaDepartamento NVARCHAR(10)  NOT NULL REFERENCES [departamento](sigla),\n"
                + "    propina           DECIMAL(10,2) NOT NULL DEFAULT 0,\n"
                + "    estado            NVARCHAR(20)  NOT NULL DEFAULT 'Ativo'\n"
                + ");\n";
    }
}

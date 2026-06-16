package dal;

import dal.db.ConnectionManager;
import dal.db.RowMapper;
import model.Estudante;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação SQL Server de {@link EstudanteDAL}.
 * Usa {@link ConnectionManager} + {@link RowMapper} e queries parametrizadas (?).
 *
 * Tabela: [estudante] (numMec PK, email UNIQUE, nome, nif UNIQUE, morada,
 * dataNascimento, anoInscricao, siglaCurso FK -> [curso](sigla), saldoDevedor, anoCurricular).
 *
 * Inicialização automática: cria a tabela se não existe e importa estudantes.csv se vazia.
 * NOTA: depende da tabela [curso] já existir (FK siglaCurso).
 */
public class EstudanteDALSql implements EstudanteDAL {

    private static final String TABELA = "estudante";
    private static final String[] CAMINHOS_SCHEMA = {
            "sql/schema_estudante.sql",
            "LP2-Grupo1/sql/schema_estudante.sql",
            "../sql/schema_estudante.sql"
    };

    private static final RowMapper<Estudante> MAPPER = rs -> mapRow(rs, "");

    private static Estudante mapRow(java.sql.ResultSet rs, String hash) throws java.sql.SQLException {
        Estudante e = new Estudante(
                rs.getInt("numMec"),
                rs.getString("email"),
                hash,
                rs.getString("nome"),
                rs.getString("nif"),
                rs.getString("morada"),
                rs.getString("dataNascimento"),
                rs.getInt("anoInscricao")
        );
        String sigla = rs.getString("siglaCurso");
        if (sigla != null && !sigla.isEmpty()) e.setSiglaCurso(sigla);
        e.setSaldoDevedor(rs.getDouble("saldoDevedor"));
        e.setAnoCurricular(rs.getInt("anoCurricular"));
        return e;
    }

    private final ConnectionManager cm;

    public EstudanteDALSql() {
        this(new ConnectionManager());
    }

    public EstudanteDALSql(ConnectionManager cm) {
        this.cm = cm;
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

    @Override
    public void adicionarEstudante(Estudante e, String siglaCurso) {
        if (e == null) return;
        String sigla = (siglaCurso != null && !siglaCurso.isEmpty()) ? siglaCurso : e.getSiglaCurso();
        cm.update("INSERT INTO [estudante] "
                + "(numMec, email, nome, nif, morada, dataNascimento, anoInscricao, siglaCurso, saldoDevedor, anoCurricular) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                e.getNumeroMecanografico(), e.getEmail(), e.getNome(), e.getNif(),
                e.getMorada(), e.getDataNascimento(), e.getAnoPrimeiraInscricao(),
                sigla, e.getSaldoDevedor(), e.getAnoCurricular());
    }

    @Override
    public void atualizarEstudante(Estudante e) {
        if (e == null) return;
        // Mantém a sigla atual se o objeto não a trouxer preenchida.
        String sigla = (e.getSiglaCurso() != null && !e.getSiglaCurso().isEmpty())
                ? e.getSiglaCurso() : null;
        if (sigla != null) {
            cm.update("UPDATE [estudante] SET email = ?, nome = ?, nif = ?, morada = ?, "
                    + "dataNascimento = ?, anoInscricao = ?, siglaCurso = ?, saldoDevedor = ?, anoCurricular = ? "
                    + "WHERE numMec = ?",
                    e.getEmail(), e.getNome(), e.getNif(), e.getMorada(),
                    e.getDataNascimento(), e.getAnoPrimeiraInscricao(), sigla,
                    e.getSaldoDevedor(), e.getAnoCurricular(), e.getNumeroMecanografico());
        } else {
            cm.update("UPDATE [estudante] SET email = ?, nome = ?, nif = ?, morada = ?, "
                    + "dataNascimento = ?, anoInscricao = ?, saldoDevedor = ?, anoCurricular = ? "
                    + "WHERE numMec = ?",
                    e.getEmail(), e.getNome(), e.getNif(), e.getMorada(),
                    e.getDataNascimento(), e.getAnoPrimeiraInscricao(),
                    e.getSaldoDevedor(), e.getAnoCurricular(), e.getNumeroMecanografico());
        }
    }

    @Override
    public Estudante carregarPerfil(String email, String hash) {
        if (email == null) return null;
        List<Estudante> r = cm.select(
                "SELECT * FROM [estudante] WHERE email = ?",
                rs -> mapRow(rs, hash), email);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public Estudante procurarPorNumMec(int numMec) {
        List<Estudante> r = cm.select(
                "SELECT * FROM [estudante] WHERE numMec = ?", MAPPER, numMec);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public List<Estudante> carregarTodos() {
        return cm.select("SELECT * FROM [estudante] ORDER BY numMec", MAPPER);
    }

    @Override
    public List<Estudante> carregarTodosBasico() {
        return carregarTodos();
    }

    @Override
    public int contarEstudantesPorCursoEAno(String siglaCurso, int anoCurricular) {
        if (siglaCurso == null) return 0;
        List<Integer> r = cm.select(
                "SELECT COUNT(*) AS total FROM [estudante] WHERE siglaCurso = ? AND anoCurricular = ?",
                rs -> rs.getInt("total"), siglaCurso, anoCurricular);
        return r.isEmpty() ? 0 : r.get(0);
    }

    @Override
    public int obterProximoNumeroMecanografico(int anoAtual) {
        int inicio = anoAtual * 10000;
        int fim = (anoAtual + 1) * 10000;
        List<Integer> r = cm.select(
                "SELECT MAX(numMec) AS maximo FROM [estudante] WHERE numMec >= ? AND numMec < ?",
                rs -> rs.getInt("maximo"), inicio, fim);
        int maxNum = r.isEmpty() ? 0 : r.get(0);
        int maxSufixo = (maxNum >= inicio) ? (maxNum % 10000) : 0;
        return inicio + (maxSufixo + 1);
    }

    @Override
    public boolean existeNif(String nif) {
        if (nif == null || nif.trim().isEmpty()) return false;
        List<Integer> r = cm.select(
                "SELECT COUNT(*) AS total FROM [estudante] WHERE nif = ?",
                rs -> rs.getInt("total"), nif.trim());
        return !r.isEmpty() && r.get(0) > 0;
    }

    @Override
    public boolean removerEstudante(int numMec) {
        int linhas = cm.update("DELETE FROM [estudante] WHERE numMec = ?", numMec);
        return linhas > 0;
    }

    public int contar() {
        List<Integer> r = cm.select("SELECT COUNT(*) AS total FROM [estudante]",
                rs -> rs.getInt("total"));
        return r.isEmpty() ? 0 : r.get(0);
    }

    // ------------------------------------------------------------------

    private void importarDeCsv() {
        List<Estudante> doFicheiro = new EstudanteDALFile().carregarTodos();
        if (doFicheiro.isEmpty()) return;
        int total = 0;
        for (Estudante e : doFicheiro) {
            if (procurarPorNumMec(e.getNumeroMecanografico()) == null) {
                adicionarEstudante(e, e.getSiglaCurso());
                total++;
            }
        }
        if (total > 0) {
            System.out.println(">> Migração: " + total
                    + " estudante(s) importado(s) de estudantes.csv para SQL.");
        }
    }

    private static String lerSchema() {
        for (String c : CAMINHOS_SCHEMA) {
            Path p = Path.of(c);
            if (Files.exists(p)) {
                try { return Files.readString(p); }
                catch (IOException e) {
                    throw new dal.db.DataAccessException("Falha ao ler " + p, e);
                }
            }
        }
        return "CREATE TABLE [estudante] (\n"
                + "    numMec         INT           NOT NULL PRIMARY KEY,\n"
                + "    email          NVARCHAR(255) NOT NULL UNIQUE,\n"
                + "    nome           NVARCHAR(100) NOT NULL,\n"
                + "    nif            NVARCHAR(9)   NOT NULL UNIQUE,\n"
                + "    morada         NVARCHAR(255),\n"
                + "    dataNascimento NVARCHAR(20),\n"
                + "    anoInscricao   INT           NOT NULL,\n"
                + "    siglaCurso     NVARCHAR(10)  NOT NULL REFERENCES [curso](sigla),\n"
                + "    saldoDevedor   DECIMAL(10,2) NOT NULL DEFAULT 0,\n"
                + "    anoCurricular  INT           NOT NULL DEFAULT 1\n"
                + ");\n";
    }
}

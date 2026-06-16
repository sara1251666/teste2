package dal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilitário de acesso ao sistema de ficheiros CSV.
 * Centraliza todas as operações de I/O para que nenhuma outra
 * camada precise de importar java.io diretamente.
 */
public class DALUtil {
    private DALUtil() {}

    /** Caminhos onde o ficheiro de schema SQL do AnoLetivo pode estar. */
    public static final String[] SCHEMA_ANO_LETIVO_CAMINHOS = {
            "sql/schema_ano_letivo.sql",
            "LP2-Grupo1/sql/schema_ano_letivo.sql",
            "../sql/schema_ano_letivo.sql"
    };

    /** SQL de fallback caso o ficheiro de schema não exista em nenhum caminho. */
    public static final String SCHEMA_ANO_LETIVO_FALLBACK =
            "CREATE TABLE [anoLetivo] (\n"
            + "    ano    INT          NOT NULL PRIMARY KEY,\n"
            + "    estado NVARCHAR(20) NOT NULL DEFAULT 'PLANEAMENTO'\n"
            + ");\n"
            + "CREATE TABLE [anoLetivoHistorico] (\n"
            + "    ano          INT          NOT NULL PRIMARY KEY,\n"
            + "    estado       NVARCHAR(20) NOT NULL,\n"
            + "    dataArquivo  NVARCHAR(30)\n"
            + ");\n";

    /**
     * Garante que o ficheiro CSV existe com o cabeçalho correto.
     * Se a pasta ou o ficheiro não existirem, são criados.
     * @param caminhoCompleto Caminho para o ficheiro.
     * @param cabecalho       Primeira linha a escrever caso o ficheiro seja criado.
     */
    public static void garantirFicheiroECabecalho(String caminhoCompleto, String cabecalho) {
        File ficheiro = new File(caminhoCompleto);
        File pasta = ficheiro.getParentFile();

        if (pasta != null && !pasta.exists() && !pasta.mkdirs()) {
            System.err.println(">> AVISO: Não foi possível criar a pasta " + pasta.getPath());
        }

        if (!ficheiro.exists()) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(ficheiro))) {
                pw.println(cabecalho);
            } catch (IOException e) {
                System.err.println(">> ERRO CRÍTICO ao criar ficheiro inicial (" + caminhoCompleto + "): " + e.getMessage());
            }
        }
    }

    /**
     * Lê todas as linhas não vazias de um ficheiro CSV.
     * @param caminhoCompleto Caminho para o ficheiro a ler.
     * @return Lista de linhas; lista vazia se o ficheiro não existir.
     */
    public static List<String> lerFicheiro(String caminhoCompleto) {
        List<String> linhas = new ArrayList<>();
        File ficheiro = new File(caminhoCompleto);

        if (!ficheiro.exists()) {
            return linhas;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(ficheiro))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                if (!linha.trim().isEmpty()) {
                    linhas.add(linha);
                }
            }
        } catch (IOException e) {
            System.err.println(">> ERRO ao ler ficheiro (" + caminhoCompleto + "): " + e.getMessage());
        }
        return linhas;
    }

    /**
     * Acrescenta uma linha ao final de um ficheiro CSV.
     * Atualizado para garantir que não cola o registo na mesma linha.
     * @param caminhoCompleto Caminho para o ficheiro destino.
     * @param linha           Linha a adicionar.
     */
    public static void adicionarLinhaCSV(String caminhoCompleto, String linha) {
        File f = new File(caminhoCompleto);
        File pastaParente = f.getParentFile();
        if (pastaParente != null && !pastaParente.exists() && !pastaParente.mkdirs()) {
            System.err.println(">> AVISO: Não foi possível criar a pasta " + pastaParente.getPath());
        }

        List<String> linhas = lerFicheiro(caminhoCompleto);

        linhas.add(linha);

        reescreverFicheiro(caminhoCompleto, linhas);
    }

    /**
     * Lê um ficheiro de schema SQL procurando nos caminhos fornecidos por ordem.
     * Se nenhum ficheiro for encontrado, devolve o fallback embutido.
     *
     * @param caminhos Caminhos a tentar, por ordem de preferência.
     * @param fallback SQL a usar caso nenhum ficheiro exista.
     * @return Conteúdo do schema SQL.
     */
    public static String lerSchema(String[] caminhos, String fallback) {
        for (String c : caminhos) {
            Path p = Path.of(c);
            if (Files.exists(p)) {
                try { return Files.readString(p); }
                catch (IOException e) {
                    throw new dal.db.DataAccessException("Falha ao ler schema: " + p, e);
                }
            }
        }
        return fallback;
    }

    /**
     * Reescreve completamente um ficheiro CSV com o novo conteúdo fornecido.
     * Usado para operações de atualização e remoção de registos.
     * @param caminhoCompleto Caminho para o ficheiro a reescrever.
     * @param linhas          Conteúdo completo que substituirá o ficheiro atual.
     */
    public static void reescreverFicheiro(String caminhoCompleto, List<String> linhas) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(caminhoCompleto, false))) {
            for (String l : linhas) {
                pw.println(l);
            }
        } catch (IOException e) {
            System.err.println(">> ERRO: Falha ao reescrever ficheiro (" + caminhoCompleto + "): " + e.getMessage());
        }
    }
}

package dal;

import model.Avaliacao;

import java.util.List;

/**
 * Contrato de acesso aos dados de avaliações.
 * Cada registo guarda as notas de um estudante numa UC num dado ano letivo.
 * Duas implementações intermutáveis, escolhidas em runtime via
 * {@link common.ConfigApp#isModoSql()}:
 * <ul>
 *     <li>{@link AvaliacaoDALFile} — persiste em avaliacoes.csv</li>
 *     <li>{@link AvaliacaoDALSql} — persiste na tabela [avaliacao]</li>
 * </ul>
 */
public interface AvaliacaoDAL {

    /** Garante que a tabela/ficheiro existe (e importa dados do CSV se necessário em modo SQL). */
    void inicializar();

    /**
     * Verifica se já existe um registo de avaliação para a combinação indicada.
     */
    boolean existeAvaliacao(int numMec, String siglaUc, int anoLetivo);

    /**
     * Persiste um novo registo de avaliação.
     * @param avaliacao Avaliação com as notas a guardar.
     * @param numMec    Número mecanográfico do estudante avaliado.
     */
    void adicionarAvaliacao(Avaliacao avaliacao, int numMec);

    /**
     * Carrega todas as avaliações de um estudante.
     */
    List<Avaliacao> obterAvaliacoesPorAluno(int numMec);

    /**
     * Procura e devolve uma avaliação específica de um aluno num determinado ano.
     */
    Avaliacao obterAvaliacao(int numMec, String siglaUc, int ano);

    /**
     * Atualiza uma avaliação existente (substitui o registo antigo pelo novo com mais notas).
     */
    void atualizarAvaliacao(Avaliacao aval, int numMec);
}

package dal;

import java.util.List;

/**
 * Contrato de acesso aos dados de inscrições em UCs.
 * Cada linha associa um estudante a uma unidade curricular num ano letivo.
 * Duas implementações intermutáveis, escolhidas em runtime via
 * {@link common.ConfigApp#isModoSql()}:
 * <ul>
 *     <li>{@link InscricaoDALFile} — persiste em inscricoes.csv</li>
 *     <li>{@link InscricaoDALSql} — persiste na tabela [inscricao]</li>
 * </ul>
 */
public interface InscricaoDAL {

    /** Garante que a tabela/ficheiro existe (e importa dados do CSV se necessário em modo SQL). */
    void inicializar();

    /**
     * Regista a inscrição de um estudante numa unidade curricular.
     * @param numMec    Número mecanográfico do estudante.
     * @param siglaUC   Sigla da UC em que o estudante se inscreve.
     * @param anoLetivo Ano letivo da inscrição.
     */
    void adicionarInscricao(int numMec, String siglaUC, int anoLetivo);

    /**
     * Remove a inscrição de um estudante numa unidade curricular.
     * Chamado na transição de ano para eliminar inscrições em UCs já aprovadas.
     */
    void removerInscricao(int numMec, String siglaUC, int anoLetivo);

    /**
     * Devolve as siglas de todas as UCs em que um estudante está inscrito num ano letivo.
     */
    List<String> obterSiglasUcsPorAluno(int numMec, int anoLetivo);

    /**
     * Devolve todas as siglas de UCs em que um aluno esteve inscrito em qualquer ano.
     */
    List<String> obterSiglasUcsPorAlunoTodosAnos(int numMec);

    /**
     * Devolve os números mecanográficos de todos os alunos inscritos numa UC num ano letivo.
     */
    List<Integer> obterAlunosPorUc(String siglaUC, int anoLetivo);

    /**
     * Devolve todos os alunos que estiveram inscritos numa UC em qualquer ano.
     */
    List<Integer> obterAlunosPorUcTodosAnos(String siglaUC);

    /** Remove todas as inscrições de um estudante. */
    void removerInscricoesPorAluno(int numMec);
}

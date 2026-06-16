package dal;

import java.util.List;

/**
 * Contrato de acesso ao histórico académico dos estudantes.
 * Cada registo guarda o resultado de um aluno numa UC, num ano letivo.
 * Duas implementações intermutáveis, escolhidas em runtime via
 * {@link common.ConfigApp#isModoSql()}:
 * <ul>
 *     <li>{@link HistoricoDALFile} — persiste em historico_academico.csv</li>
 *     <li>{@link HistoricoDALSql} — persiste na tabela [historicoAcademico]</li>
 * </ul>
 */
public interface HistoricoDAL {

    /** Garante que a tabela/ficheiro existe (e importa dados do CSV se necessário em modo SQL). */
    void inicializar();

    /** Regista um registo de histórico académico (resultado de um aluno numa UC, num ano). */
    void guardarRegistoHistorico(int anoLetivo, int numMec, String siglaUC, String notas, String estado);

    /** Devolve todos os registos de histórico para um dado ano letivo. */
    List<String> consultarHistoricoPorAno(int anoLetivo);

    /** Devolve todos os registos de histórico de um aluno (todos os anos). */
    List<String> consultarHistoricoPorAluno(int numMec);
}

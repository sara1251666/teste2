package dal;

import model.Gestor;

/**
 * Contrato de persistência do módulo Gestor. Tem duas implementações
 * intermutáveis — {@link GestorDALSql} e {@link GestorDALFile} —
 * escolhidas em tempo de execução pela {@link bll.AutenticacaoBLL}
 * consoante config.properties.
 *
 * Âmbito mínimo: suporte ao login e à inicialização com dados do CSV.
 * A gestão de credenciais (password) é da responsabilidade de {@link LoginDAL}.
 */
public interface GestorDAL {

    /**
     * Prepara o armazenamento: cria a tabela/ficheiro se necessário e,
     * se estiver vazio, importa os dados de gestores.csv.
     */
    void inicializar();

    /**
     * Devolve o perfil do gestor com o email indicado, construído com
     * o {@code hash} proveniente de {@link LoginDAL}.
     * Devolve null se o email não existir.
     */
    Gestor procurarPorEmail(String email, String hash);

    /** Número total de gestores registados. */
    int contar();
}

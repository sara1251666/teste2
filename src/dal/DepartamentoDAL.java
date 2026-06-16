package dal;

import model.Departamento;
import java.util.List;

/**
 * Contrato de persistência do módulo Departamento. Tem duas implementações
 * intermutáveis — {@link DepartamentoDALSql} e {@link DepartamentoDALFile} —
 * escolhidas em tempo de execução pelas BLLs consoante config.properties.
 */
public interface DepartamentoDAL {

    /**
     * Prepara o armazenamento: cria a tabela/ficheiro se necessário e,
     * se estiver vazio, importa os dados do CSV.
     */
    void inicializar();

    /** Devolve o departamento com a sigla indicada, ou null se não existir. */
    Departamento procurarPorSigla(String sigla);

    /** Devolve todos os departamentos. */
    List<Departamento> listarTodos();

    /** Devolve um array "SIGLA - Nome" de todos os departamentos (para menus). */
    String[] obterListaFormatada();

    /** Persiste um novo departamento. Devolve true se inseriu. */
    boolean criar(Departamento d);

    /** Atualiza o departamento identificado pela sigla de {@code d}. Devolve true se afetou alguma linha. */
    boolean atualizar(Departamento d);

    /** Remove o departamento com a sigla indicada. Devolve true se removeu. */
    boolean eliminar(String sigla);

    /** Indica se já existe um departamento com a sigla indicada. */
    boolean existe(String sigla);

    /** Número total de departamentos. */
    int contar();
}

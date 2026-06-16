package model;

import common.ConfigApp;
import dal.AnoLetivoDAL;
import dal.AnoLetivoDALFile;
import dal.AnoLetivoDALSql;

/**
 * Repositório de estado da sessão em curso.
 * Mantém o utilizador autenticado e o ano letivo ativo
 * durante toda a execução da aplicação.
 */
public class RepositorioDados {

    private Utilizador utilizadorLogado;
    private int anoAtual;

    /**
     * Inicializa a sessão. Carrega o ano letivo ativo a partir da DAL.
     * Se não existir nenhum registo (primeiro arranque), cria automaticamente
     * o ano 2026 em estado PLANEAMENTO e persiste-o.
     */
    public RepositorioDados() {
        this.utilizadorLogado = null;

        AnoLetivoDAL dal = ConfigApp.isModoSql() ? new AnoLetivoDALSql() : new AnoLetivoDALFile();
        dal.inicializar();

        AnoLetivo ativo = dal.obterAnoAtivo();
        if (ativo == null) {
            ativo = dal.procurarPorAno(2026);
            if (ativo == null) {
                ativo = new AnoLetivo(2026);
                dal.adicionar(ativo);
            }
        }
        this.anoAtual = ativo.getAno();
    }

    /**
     * Define o utilizador autenticado na sessão atual.
     * @param u Utilizador que acabou de fazer login.
     */
    public void setUtilizadorLogado(Utilizador u) { this.utilizadorLogado = u; }

    /** @return Ano letivo ativo no sistema. */
    public int getAnoAtual() { return anoAtual; }

    /**
     * Atualiza o ano letivo após avançar o ciclo académico.
     * @param ano Novo ano letivo.
     */
    public void setAnoAtual(int ano) { this.anoAtual = ano; }

    /** Termina a sessão removendo o utilizador autenticado. */
    public void limparSessao() {
        this.utilizadorLogado = null;
    }
}

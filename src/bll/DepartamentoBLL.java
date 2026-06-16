package bll;

import common.ConfigApp;
import dal.DepartamentoDAL;
import dal.DepartamentoDALFile;
import dal.DepartamentoDALSql;
import model.Curso;
import model.Departamento;

import java.util.List;

public class DepartamentoBLL {

    private final DepartamentoDAL dal;

    public DepartamentoBLL() {
        this.dal = ConfigApp.isModoSql() ? new DepartamentoDALSql() : new DepartamentoDALFile();
    }

    /** Inicializa o armazenamento (cria tabela/ficheiro e importa CSV se vazio). */
    public void inicializar() {
        dal.inicializar();
    }

    public List<Departamento> listarTodos() {
        return dal.listarTodos();
    }

    public Departamento obterPorSigla(String sigla) {
        return dal.procurarPorSigla(sigla);
    }

    public boolean adicionarDepartamento(String sigla, String nome) {
        if (sigla == null || nome == null) return false;
        if (dal.existe(sigla)) return false;
        return dal.criar(new Departamento(sigla.toUpperCase(), nome));
    }

    public boolean atualizarDepartamento(String sigla, String novaSigla, String novoNome) {
        Departamento existente = obterPorSigla(sigla);
        if (existente == null) return false;

        String siglaFinal = (novaSigla != null && !novaSigla.isEmpty())
                ? novaSigla.toUpperCase() : existente.getSigla();
        String nomeFinal  = (novoNome != null && !novoNome.isEmpty())
                ? novoNome : existente.getNome();

        // Verifica conflito de sigla
        if (!siglaFinal.equalsIgnoreCase(existente.getSigla()) && dal.existe(siglaFinal)) {
            return false;
        }

        if (!siglaFinal.equalsIgnoreCase(existente.getSigla())) {
            // Sigla mudou: elimina e recria (PK não pode ser UPDATE direto em SQL)
            if (!dal.eliminar(sigla)) return false;
            return dal.criar(new Departamento(siglaFinal, nomeFinal));
        } else {
            return dal.atualizar(new Departamento(siglaFinal, nomeFinal));
        }
    }

    public boolean removerDepartamento(String sigla) {
        // Não remove se existirem cursos associados
        List<Curso> cursos = new CursoBLL().listarTodos();
        for (Curso c : cursos) {
            if (c.getDepartamento() != null
                    && c.getDepartamento().getSigla().equalsIgnoreCase(sigla)) {
                return false;
            }
        }
        return dal.eliminar(sigla);
    }
}

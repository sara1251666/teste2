package controller;

import common.ConfigApp;
import model.*;
import utils.*;
import view.GestorView;
import dal.UcDAL;
import dal.UcDALFile;
import dal.UcDALSql;
import bll.GestorBLL;
import bll.EstudanteBLL;
import bll.UcBLL;
import bll.DocenteBLL;
import bll.DepartamentoBLL;
import bll.CursoBLL;
import utils.CancelamentoException;
import utils.Validador;
import java.util.List;

/**
 * Controlador responsável por gerir as interações e permissões do Gestor.
 * Liga a GestorView às BLLs correspondentes.
 */
public class GestorController {

    private final RepositorioDados repo;
    private final Gestor gestor;
    private final GestorView view;
    private final GestorBLL gestorBll;
    private final EstudanteBLL estudanteBll;
    private final UcBLL ucBll;
    private final DocenteBLL docenteBll = new DocenteBLL();
    private final UcDAL ucDAL = ConfigApp.isModoSql() ? new UcDALSql() : new UcDALFile();

    public GestorController(RepositorioDados repo, Gestor gestor) {
        this.repo = repo;
        this.gestor = gestor;
        this.view = new GestorView();
        this.gestorBll = new GestorBLL();
        this.estudanteBll = new EstudanteBLL();
        this.ucBll  = new UcBLL();
    }

    /**
     * Inicia o ciclo principal de execução do menu do Gestor.
     * Gere a navegação principal e o logout.
     */
    public void iniciar() {
        boolean correr = true;
        while (correr) {
            try {
                int opcao = view.mostrarMenu();
                switch (opcao) {
                    case 1: menuGerirEstudante(); break;
                    case 2: menuGerirDocente(); break;
                    case 3: menuGerirDepartamento(); break;
                    case 4: menuGerirUcs(); break;
                    case 5: menuGerirCurso(); break;
                    case 6: menuEstatisticas(); break;
                    case 7: menuAnoLetivo(); break;
                    case 8: consultarHistoricoAno(); break;
                    case 9: listarDevedores(); break;
                    case 10: alterarPassword(); break;
                    case 0:
                        view.mostrarDespedida();
                        repo.limparSessao();
                        correr = false;
                        break;
                    default:
                        view.mostrarOpcaoInvalida();
                }
            } catch (Exception e) {
                view.mostrarErroLeituraOpcao();
            }
        }
    }

    private void menuGerirEstudante() {
        boolean voltar = false;
        while (!voltar) {
            int opcao = view.mostrarSubMenuEstudante();
            switch (opcao) {
                case 1: executarCriarEstudante(); break;
                case 2: executarListarEstudantes(); break;
                case 3: executarEditarEstudante(); break;
                case 4: executarApagarEstudante(); break;
                case 0: voltar = true; break;
                default: view.mostrarOpcaoInvalida();
            }
        }
    }

    private void executarCriarEstudante() {
        executarRegistoEstudante();  // reutiliza o método existente
    }

    private void executarListarEstudantes() {
        List<Estudante> estudantes = estudanteBll.listarTodos();
        if (estudantes.isEmpty()) {
            view.mostrarErroNaoEncontrado("Estudantes");
            return;
        }
        view.mostrarTitulo("Lista de Estudantes");
        for (Estudante e : estudantes) {
            view.mostrarEstudante(e);
        }
        Consola.pausar();
    }

    private void executarEditarEstudante() {
        try {
            int numMec = view.pedirNumeroEstudante();
            Estudante e = estudanteBll.obterPorNumMec(numMec);
            if (e == null) {
                view.mostrarErroNaoEncontrado("Estudante");
                return;
            }

            view.mostrarEstudante(e);
            view.mostrarMensagemModoEdicao();

            // Editar Nome
            String novoNome = view.pedirNovoNomeEstudante();
            if (!novoNome.isEmpty()) {
                if (Validador.isNomeValido(novoNome)) {
                    e.setNome(novoNome);
                } else {
                    view.mostrarErroNomeInvalido();
                }
            }

            // Editar NIF
            String novoNif = view.pedirNovoNifEstudante();
            if (!novoNif.isEmpty()) {
                if (Validador.validarNif(novoNif) && !gestorBll.isNifDuplicado(novoNif)) {
                    e.setNif(novoNif);
                } else {
                    view.mostrarErroNifInvalidoOuDuplicado();
                }
            }

            // Editar Data de Nascimento
            String novaData = view.pedirNovaDataNascimentoEstudante();
            if (!novaData.isEmpty()) {
                int resultado = Validador.validarDataNascimentoDetalhado(novaData);
                switch (resultado) {
                    case 0:
                        e.setDataNascimento(novaData);
                        break;
                    case 1:
                        view.mostrarErroDataInexistente();
                        break;
                    case 2:
                        view.mostrarErroDataFutura();
                        break;
                    case 3:
                        view.mostrarErroIdadeForaLimites();
                        break;
                }
            }

            // Editar Morada
            String novaMorada = view.pedirNovaMoradaEstudante();
            if (!novaMorada.isEmpty()) {
                e.setMorada(novaMorada);
            }

            estudanteBll.atualizarEstudante(e);
            view.mostrarSucessoAtualizacao("Estudante");

        } catch (CancelamentoException ex) {
            view.mostrarOperacaoCancelada();
        }
    }

    private void executarApagarEstudante() {
        try {
            int numMec = view.pedirNumeroEstudante();
            Estudante e = estudanteBll.obterPorNumMec(numMec);
            if (e == null) {
                view.mostrarErroNaoEncontrado("Estudante");
                return;
            }
            view.mostrarEstudante(e);
            if (view.confirmarRemocaoBoolean("estudante " + e.getNome())) {
                if (estudanteBll.removerEstudante(numMec)) {
                    view.mostrarSucessoRemocao("Estudante");
                } else {
                    view.mostrarErroRemocao("Estudante");
                }
            }
        } catch (CancelamentoException ex) {
            view.mostrarOperacaoCancelada();
        }
    }

    private void consultarHistoricoAno() {
        try {
            view.mostrarMensagem("--- Consulta de Histórico ---");
            int ano = utils.Consola.lerInt("Introduza o Ano Letivo a pesquisar");

            java.util.List<String> registos = gestorBll.obterHistoricoPorAno(ano);

            if (registos.isEmpty()) {
                view.mostrarMensagem("Nenhum registo encontrado para o ano " + ano);
                return;
            }

            view.mostrarMensagem("Registos do Ano " + ano + " (Ano;NumMec;UC;Notas;Estado):");
            for (String r : registos) {
                view.mostrarMensagem("  " + r);
            }
            utils.Consola.pausar();
        } catch (Exception e) {
            view.mostrarOperacaoCancelada();
        }
    }



    // ------------------------------------------------------------
    // MENU GERIR DOCENTE
    // ------------------------------------------------------------
    private void menuGerirDocente() {
        boolean voltar = false;
        while (!voltar) {
            int opcao = view.mostrarSubMenuDocente();
            switch (opcao) {
                case 1: executarCriarDocente(); break;
                case 2: executarListarDocentes(); break;
                case 3: executarEditarDocente(); break;
                case 4: executarApagarDocente(); break;
                case 0: voltar = true; break;
                default: view.mostrarOpcaoInvalida();
            }
        }
    }

    private void executarCriarDocente() {
        // Reutiliza o método de registo existente (executarRegistoDocente)
        executarRegistoDocente();
    }

    private void executarListarDocentes() {
        List<Docente> docentes = docenteBll.listarTodos();
        if (docentes.isEmpty()) {
            view.mostrarErroNaoEncontrado("Docentes");
            return;
        }
        view.mostrarListaDocentes(docentes);
        Consola.pausar();
    }

    private void executarEditarDocente() {
        try {
            String sigla = view.pedirSiglaDocenteParaGestao();
            Docente d = docenteBll.obterPorSigla(sigla);
            if (d == null) {
                view.mostrarErroNaoEncontrado("Docente");
                return;
            }

            view.mostrarDocente(d);
            view.mostrarMensagemModoEdicao();

            // Editar Nome
            String novoNome = view.pedirNovoNomeDocente();
            if (!novoNome.isEmpty()) {
                if (Validador.isNomeValido(novoNome)) {
                    d.setNome(novoNome);
                } else {
                    view.mostrarErroNomeInvalido();
                }
            }

            // Editar NIF
            String novoNif = view.pedirNovoNifDocente();
            if (!novoNif.isEmpty()) {
                if (Validador.validarNif(novoNif) && !gestorBll.isNifDuplicado(novoNif)) {
                    d.setNif(novoNif);
                } else {
                    view.mostrarErroNifInvalidoOuDuplicado();
                }
            }

            // Editar Morada
            String novaMorada = view.pedirNovaMoradaDocente();
            if (!novaMorada.isEmpty()) {
                d.setMorada(novaMorada);
            }

            // Editar Data Nascimento
            String novaData = view.pedirNovaDataNascimentoDocente();
            if (!novaData.isEmpty()) {
                int resultado = Validador.validarDataNascimentoDetalhado(novaData);
                switch (resultado) {
                    case 0:
                        d.setDataNascimento(novaData);
                        break;
                    case 1:
                        view.mostrarErroDataInexistente();
                        break;
                    case 2:
                        view.mostrarErroDataFutura();
                        break;
                    case 3:
                        view.mostrarErroIdadeForaLimites();
                        break;
                }
            }

            docenteBll.atualizarDocente(d);
            view.mostrarSucessoAtualizacao("Docente");

        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    private void executarApagarDocente() {
        try {
            String sigla = view.pedirSiglaDocenteParaGestao();
            Docente d = docenteBll.obterPorSigla(sigla);
            if (d == null) {
                view.mostrarErroNaoEncontrado("Docente");
                return;
            }
            view.mostrarDocente(d);
            if (!view.confirmarRemocaoBoolean("docente " + d.getNome())) {
                return;
            }

            if (docenteBll.temUcAtribuida(sigla)) {
                view.mostrarErroDocenteComUcs();
                return;
            }

            if (docenteBll.removerDocente(sigla)) {
                view.mostrarSucessoRemocao("Docente");
            } else {
                view.mostrarErroRemocao("Docente");
            }
        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    // --- Métodos de Registo ---

    /**
     * Coordena o registo de um novo docente.
     * Validação de NIF delegada à GestorBLL (que consulta as DALs).
     */
    private void executarRegistoDocente() {
        try {
            view.mostrarTituloRegistoDocente();

            String nome;
            do {
                nome = view.pedirNome();
                if (!Validador.isNomeValido(nome)) view.mostrarErroNomeInvalido();
            } while (!Validador.isNomeValido(nome));

            String nif;
            boolean nifInvalido, nifDuplicado;
            do {
                nif          = view.pedirNif();
                nifInvalido  = !Validador.validarNif(nif);
                nifDuplicado = !nifInvalido && gestorBll.isNifDuplicado(nif);
                if (nifInvalido)       view.mostrarErroNifInvalido();
                else if (nifDuplicado) view.mostrarErroNifDuplicado();
            } while (nifInvalido || nifDuplicado);

            String morada   = view.pedirMorada();
            String dataNasc;
            boolean dataValida = false;
            do {
                dataNasc = view.pedirDataNascimento();
                int resultado = Validador.validarDataNascimentoDetalhado(dataNasc);
                switch (resultado) {
                    case 0:
                        dataValida = true;
                        break;
                    case 1:
                        view.mostrarErroDataInexistente();
                        break;
                    case 2:
                        view.mostrarErroDataFutura();
                        break;
                    case 3:
                        view.mostrarErroIdadeForaLimites();
                        break;
                }
            } while (!dataValida);

            String[] resultado = gestorBll.registarDocente(nome, nif, morada, dataNasc);
            view.mostrarResumoRegistoDocente(resultado[0], resultado[1]);

        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    /**
     * Fluxo de registo de um novo Departamento.
     * Valida sigla não duplicada e nome não vazio.
     */
    private void executarRegistoDepartamento() {
        try {
            view.mostrarTituloRegistoDepartamento();

            String sigla;
            do {
                sigla = view.pedirSiglaDepartamento().toUpperCase().trim();
                if (sigla.isEmpty()) {
                    view.mostrarMensagem("ERRO: Sigla não pode estar vazia.");
                } else if (gestorBll.isDepartamentoDuplicado(sigla)) {
                    view.mostrarErroDepartamentoDuplicado();
                    sigla = "";
                }
            } while (sigla.isEmpty());

            String nome;
            do {
                nome = view.pedirNomeDepartamento().trim();
                if (nome.isEmpty()) view.mostrarMensagem("ERRO: Nome não pode estar vazio.");
            } while (nome.isEmpty());

            gestorBll.registarDepartamento(sigla, nome);
            view.mostrarResumoRegistoDepartamento(sigla, nome);

        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }


    /**
     * Coordena o registo de um novo estudante.
     * Número mecanográfico gerado automaticamente via EstudanteBLL.
     */
    private void executarRegistoEstudante() {
        try {
            view.mostrarTituloRegistoEstudante();
            int anoInscricao = repo.getAnoAtual();

            String nome;
            do {
                nome = view.pedirNome();
                if (!utils.Validador.isNomeValido(nome)) view.mostrarErroNomeInvalido();
            } while (!utils.Validador.isNomeValido(nome));

            String nif;
            boolean nifInvalido, nifDuplicado;
            do {
                nif          = view.pedirNif();
                nifInvalido  = !utils.Validador.validarNif(nif);
                nifDuplicado = !nifInvalido && gestorBll.isNifDuplicado(nif);
                if (nifInvalido)       view.mostrarErroNifInvalido();
                else if (nifDuplicado) view.mostrarErroNifDuplicado();
            } while (nifInvalido || nifDuplicado);

            String morada = view.pedirMorada();

            String dataNasc;
            boolean dataValida = false;
            do {
                dataNasc = view.pedirDataNascimento();
                int resultado = Validador.validarDataNascimentoDetalhado(dataNasc);
                switch (resultado) {
                    case 0:
                        dataValida = true;
                        break;
                    case 1:
                        view.mostrarErroDataInexistente();
                        break;
                    case 2:
                        view.mostrarErroDataFutura();
                        break;
                    case 3:
                        view.mostrarErroIdadeForaLimites();
                        break;
                }
            } while (!dataValida);

            String[] todosCursos = gestorBll.obterListaCursos();
            java.util.List<String> cursosAptos = new java.util.ArrayList<>();

            for (String cursoStr : todosCursos) {
                String sigla = cursoStr.split(" - ")[0];

                boolean temAno1 = ucDAL.contarUcsPorCursoEAno(sigla, 1, ConfigApp.PASTA_BD) > 0;
                boolean temAno2 = ucDAL.contarUcsPorCursoEAno(sigla, 2, ConfigApp.PASTA_BD) > 0;
                boolean temAno3 = ucDAL.contarUcsPorCursoEAno(sigla, 3, ConfigApp.PASTA_BD) > 0;

                if (temAno1 && temAno2 && temAno3) {
                    cursosAptos.add(cursoStr);
                }
            }

            if (cursosAptos.isEmpty()) {
                view.mostrarMensagem("Erro: Não existem cursos com UCs configuradas em todos os anos (1, 2 e 3)."); //
                return;
            }

            String[] listaParaExibir = cursosAptos.toArray(new String[0]);
            view.mostrarListaCursos(listaParaExibir); 

            int escolha = view.pedirOpcaoCurso(listaParaExibir.length); //
            if (escolha == -1) { view.mostrarOperacaoCancelada(); return; }

            String siglaCurso = listaParaExibir[escolha - 1].split(" - ")[0];

            String email = gestorBll.registarEstudante(nome, nif, morada, dataNasc, siglaCurso, anoInscricao);

            if (email != null && !email.isEmpty()) {
                view.mostrarResumoRegistoEstudante(email);
            } else {
                view.mostrarMensagem("Erro ao processar o registo do estudante.");
            }

        } catch (utils.CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    // ------------------------------------------------------------
    // MENU GERIR CURSO
    // ------------------------------------------------------------

    private final CursoBLL cursoBll = new CursoBLL();

    private void menuGerirCursos() {
        boolean voltar = false;
        while (!voltar) {
            int opcao = view.mostrarSubMenuCurso();
            switch (opcao) {
                case 1: executarCriarCurso(); break;
                case 2: executarListarCursos(); break;
                case 3: executarEditarCurso(); break;
                case 4: executarApagarCurso(); break;
                case 0: voltar = true; break;
                default: view.mostrarOpcaoInvalida();
            }
        }
    }

    private void executarCriarCurso() {
        try {
            view.mostrarTitulo("Criar Novo Curso");
            String sigla = view.pedirSiglaCurso();
            // Verifica se já existe
            if (cursoBll.obterPorSigla(sigla) != null) {
                view.mostrarErroCursoExistente();
                return;
            }
            String nome = view.pedirNomeCurso();

            // Listar departamentos para escolha
            String[] depts = gestorBll.obterListaDepartamentos();
            if (depts.length == 0) {
                view.mostrarErroSemDepartamentos();
                return;
            }
            view.mostrarListaCursos(depts); // reutiliza,
            String siglaDep;
            do {
                siglaDep = view.pedirDepartamento().toUpperCase();
                if (!gestorBll.isDepartamentoDuplicado(siglaDep)) {
                    view.mostrarErroDepartamentoNaoEncontrado();
                }
            } while (!gestorBll.isDepartamentoDuplicado(siglaDep));

            double propina = view.pedirPropinaCurso();
            if (propina < 0) {
                view.mostrarErroPropinaNegativa();
                return;
            }

            if (cursoBll.adicionarCurso(sigla, nome, siglaDep, propina)) {
                view.mostrarSucessoCriacao("Curso");
            } else {
                view.mostrarErroCriacaoCurso();
            }
        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    private void executarListarCursos() {
        List<Curso> cursos = cursoBll.listarTodos();
        if (cursos.isEmpty()) {
            view.mostrarErroNaoEncontrado("Cursos");
            return;
        }
        view.mostrarListaCursos(cursos);
        Consola.pausar();
    }

    private void executarEditarCurso() {
        try {
            String sigla = view.pedirSiglaCurso();
            Curso curso = cursoBll.obterPorSigla(sigla);
            if (curso == null) {
                view.mostrarErroNaoEncontrado("Curso");
                return;
            }
            if (!cursoBll.isAlteravel(sigla)) {
                view.mostrarErroCursoComAlocacoes();
                return;
            }
            view.mostrarCurso(curso);
            view.mostrarMensagemModoEdicao();

            String novoNome = view.pedirNovoNomeCurso();
            if (novoNome.isEmpty()) novoNome = null;

            String novaSiglaDep = view.pedirNovoSiglaDepartamentoCurso();
            if (novaSiglaDep.isEmpty()) novaSiglaDep = null;

            Double novaPropina = view.pedirNovaPropinaCurso();
            if (novaPropina != null) {
                double prop = novaPropina;
                if (prop < 0) {
                    view.mostrarErroPropinaNegativaMantida();
                } else {
                    // verificar 2 casas decimais
                    if (Math.round(prop * 100.0) / 100.0 != prop) {
                        view.mostrarErroPropinaDuasCasas();
                    } else {
                        novaPropina = prop;
                    }
                }
            }

            if (cursoBll.atualizarCurso(sigla, novoNome, novaSiglaDep, novaPropina)) {
                view.mostrarSucessoAtualizacao("Curso");
            } else {
                view.mostrarErroAtualizacaoCurso();
            }
        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    private void executarApagarCurso() {
        try {
            String sigla = view.pedirSiglaCurso();
            Curso curso = cursoBll.obterPorSigla(sigla);
            if (curso == null) {
                view.mostrarErroNaoEncontrado("Curso");
                return;
            }
            view.mostrarCurso(curso);
            if (!cursoBll.isAlteravel(sigla)) {
                view.mostrarErroCursoComAlocacoes();
                return;
            }
            if (view.confirmarRemocaoBoolean("curso " + curso.getNome())) {
                if (cursoBll.removerCurso(sigla)) {
                    view.mostrarSucessoRemocao("Curso");
                } else {
                    view.mostrarErroRemocao("Curso");
                }
            }
        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    // ------------------------------------------------------------
    // MENU GERIR DEPARTAMENTO
    // ------------------------------------------------------------

    private final DepartamentoBLL departamentoBll = new DepartamentoBLL();

    private void menuGerirDepartamento() {
        boolean voltar = false;
        while (!voltar) {
            int opcao = view.mostrarSubMenuDepartamento();
            switch (opcao) {
                case 1: executarCriarDepartamento(); break;
                case 2: executarListarDepartamentos(); break;
                case 3: executarEditarDepartamento(); break;
                case 4: executarApagarDepartamento(); break;
                case 0: voltar = true; break;
                default: view.mostrarOpcaoInvalida();
            }
        }
    }

    private void executarCriarDepartamento() {
        try {
            view.mostrarTitulo("Criar Departamento");
            String sigla = view.pedirSiglaDepartamento().toUpperCase();
            if (departamentoBll.obterPorSigla(sigla) != null) {
                view.mostrarErroDepartamentoDuplicado();
                return;
            }
            String nome = view.pedirNomeDepartamento();
            if (departamentoBll.adicionarDepartamento(sigla, nome)) {
                view.mostrarSucessoCriacao("Departamento");
            } else {
                view.mostrarErroCriarDepartamento();
            }
        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    private void executarListarDepartamentos() {
        List<Departamento> depts = departamentoBll.listarTodos();
        if (depts.isEmpty()) {
            view.mostrarErroNaoEncontrado("Departamentos");
            return;
        }
        view.mostrarListaDepartamentos(depts);
        Consola.pausar();
    }

    private void executarEditarDepartamento() {
        try {
            String sigla = view.pedirSiglaDepartamento().toUpperCase();
            Departamento dept = departamentoBll.obterPorSigla(sigla);
            if (dept == null) {
                view.mostrarErroNaoEncontrado("Departamento");
                return;
            }
            view.mostrarDepartamento(dept);
            view.mostrarMensagemModoEdicao();

            String novaSigla = view.pedirNovoSiglaDepartamento();
            if (novaSigla.isEmpty()) novaSigla = null;
            else novaSigla = novaSigla.toUpperCase();

            String novoNome = view.pedirNovoNomeDepartamento();
            if (novoNome.isEmpty()) novoNome = null;

            if (departamentoBll.atualizarDepartamento(sigla, novaSigla, novoNome)) {
                view.mostrarSucessoAtualizacao("Departamento");
            } else {
                view.mostrarErroAtualizarDepartamento();
            }
        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    private void executarApagarDepartamento() {
        try {
            String sigla = view.pedirSiglaDepartamento().toUpperCase();
            Departamento dept = departamentoBll.obterPorSigla(sigla);
            if (dept == null) {
                view.mostrarErroNaoEncontrado("Departamento");
                return;
            }
            view.mostrarDepartamento(dept);
            if (!view.confirmarRemocaoBoolean("departamento " + dept.getNome())) {
                return;
            }
            if (departamentoBll.removerDepartamento(sigla)) {
                view.mostrarSucessoRemocao("Departamento");
            } else {
                view.mostrarErroRemoverDepartamentoComCursos();
            }
        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    // --- Métodos de Estatísticas e Listagens ---

    /**
     * Solicita à BLL os dados estatísticos globais e apresenta a média
     * institucional através da View.
     */
    private void mostrarMediaGlobal() {
        view.mostrarCabecalhoMediaGlobal();
        double[] stats = gestorBll.calcularEstatisticasGlobais();
        if (stats == null)          { view.mostrarErroCarregarDados("Estudantes"); return; }
        if (stats[1] == 0)          { view.mostrarSemNotasRegistadas();           return; }
        view.mostrarMediaGlobal(stats[0] / stats[1], (int) stats[1]);
    }

    /**
     * Obtém o estudante com melhor desempenho académico através da BLL
     * e exibe os seus detalhes.
     */
    private void mostrarMelhorAluno() {
        view.mostrarCabecalhoMelhorAluno();
        Object[] resultado = gestorBll.obterMelhorAluno();
        if (resultado != null) {
            Estudante melhor = (Estudante) resultado[0];
            double media     = (double) resultado[1];
            view.mostrarInfoMelhorAluno(melhor.getNome(), melhor.getNumeroMecanografico(), media);
        } else {
            view.mostrarSemAlunosAvaliados();
        }
    }

    /**
     * Lista todos os estudantes que possuem saldo devedor (propinas em atraso).
     */
    private void listarDevedores() {
        view.mostrarCabecalhoDevedores();
        List<Estudante> devedores = gestorBll.obterListaDevedores();
        if (devedores.isEmpty()) { view.mostrarSemDevedores(); return; }
        for (Estudante e : devedores)
            view.mostrarEstudanteDevedor(
                    e.getNumeroMecanografico(), e.getNome(), e.getSaldoDevedor());
    }

    // --- Gestão de UCs ---

    /**
     * Recolhe dados da View para criar uma nova Unidade Curricular e
     * valida o limite máximo de UCs por ano via BLL.
     * A operação pode ser cancelada durante a introdução dos dados.
     */
    private void adicionarUc() {
        try {
            String[] cursos = gestorBll.obterListaCursos();
            String siglaCurso = null;
            int anoUc = 1;  // valor por defeito

            if (cursos.length == 0) {
                view.mostrarMensagem("Aviso: Não existem cursos registados. A UC será criada sem associação a curso e no 1º ano.");
            } else {
                view.mostrarListaCursos(cursos);
                view.mostrarOpcaoNaoAssociarCurso();

                int escolha = -1;
                while (escolha < 0 || escolha > cursos.length) {
                    try {
                        escolha = Consola.lerInt("Número do Curso (0-" + cursos.length + ")");
                        if (escolha < 0 || escolha > cursos.length) {
                            Consola.imprimirErro("Opção inválida. Escolha entre 0 e " + cursos.length + ".");
                        }
                    } catch (CancelamentoException e) {
                        throw e;
                    } catch (Exception e) {
                        Consola.imprimirErro("Número inválido.");
                    }
                }

                if (escolha == 0) {
                    // Não associar a curso - ano será 1 por defeito
                    siglaCurso = null;
                    view.mostrarMensagem("UC não associada a nenhum curso. Ano curricular definido por defeito como 1º.");
                } else {
                    siglaCurso = cursos[escolha - 1].split(" - ")[0];
                    // Se associou a curso, pedir o ano
                    try {
                        anoUc = Integer.parseInt(view.pedirAnoCurricular());
                        if (anoUc < 1 || anoUc > 3) {
                            view.mostrarMensagem("ERRO: Ano curricular deve ser 1, 2 ou 3. A operação foi cancelada.");
                            return;
                        }
                    } catch (NumberFormatException ex) {
                        view.mostrarMensagem("ERRO: Ano curricular inválido. A operação foi cancelada.");
                        return;
                    }
                }
            }

            // Se não existiam cursos, siglaCurso já é null, anoUc já é 1

            String siglaUc = view.pedirSiglaUc();
            String nomeUc  = view.pedirNomeUc();

            if (view.perguntarVerListagem("Docentes")) {
                view.mostrarResultadosListagem(gestorBll.obterListaDocentes());
            }

            String docente;
            do {
                docente = view.pedirSiglaDocente();
                if (!gestorBll.existeDocente(docente)) {
                    view.mostrarMensagem("ERRO: Docente não encontrado. Introduza uma sigla de um docente existente.");
                }
            } while (!gestorBll.existeDocente(docente));

            // Chamar a BLL (que deverá aceitar siglaCurso = null e ignorar o limite de 5 UCs por ano)
            if (gestorBll.adicionarUc(siglaCurso, anoUc, siglaUc, nomeUc, docente))
                view.mostrarSucessoCriacao("UC");
            else
                view.mostrarErroLimiteUcs(anoUc);
        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    /**
     * Permite a edição de uma UC existente, substituindo os dados antigos
     * pelos novos introduzidos pelo Gestor, garantindo docentes válidos.
     */
    private void editarUc() {
        try {
            String[] ucs = ucBll.obterListaUcs();
            if (ucs.length == 0) {
                view.mostrarErroNaoEncontrado("UCs");
                return;
            }
            view.mostrarListaUcs(ucs);
            int escolha = view.pedirOpcaoUc(ucs.length);
            if (escolha == -1) return;
            String siglaAntiga = ucs[escolha - 1].split(" - ")[0];

            UnidadeCurricular ucOriginal = ucBll.procurarUCCompleta(siglaAntiga);
            if (ucOriginal == null) {
                view.mostrarErroUcNaoEncontrada();
                return;
            }

            int anoOriginal = ucOriginal.getAnoCurricular();
            String cursoOriginal = "N/A";
            Curso[] cursos = ucOriginal.getCursos();
            if (cursos.length > 0 && cursos[0] != null) {
                cursoOriginal = cursos[0].getSigla();
            }

            view.mostrarMensagemModoEdicao();

            String novaSigla = view.pedirNovaSiglaUc();
            if (novaSigla.isEmpty()) novaSigla = siglaAntiga;

            String novoNome = view.pedirNovoNomeUc();
            if (novoNome.isEmpty()) novoNome = ucOriginal.getNome();

            if (view.perguntarVerListagem("Docentes")) {
                view.mostrarResultadosListagem(gestorBll.obterListaDocentes());
            }

            String novaSiglaDocente = view.pedirNovaSiglaDocenteUc();
            if (novaSiglaDocente.isEmpty()) {
                novaSiglaDocente = ucOriginal.getDocenteResponsavel().getSigla();
            } else {
                while (!gestorBll.existeDocente(novaSiglaDocente)) {
                    view.mostrarMensagem("ERRO: Docente não encontrado.");
                    novaSiglaDocente = view.pedirNovaSiglaDocenteUc();
                    if (novaSiglaDocente.isEmpty()) {
                        novaSiglaDocente = ucOriginal.getDocenteResponsavel().getSigla();
                        break;
                    }
                }
            }

            boolean sucesso = gestorBll.editarUc(
                    siglaAntiga,
                    novaSigla,
                    novoNome,
                    String.valueOf(anoOriginal),
                    novaSiglaDocente,
                    cursoOriginal);

            if (sucesso) {
                view.mostrarSucessoAtualizacao("UC");
            } else {
                view.mostrarErroEditarUc();
            }

        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    private void removerUc() {
        String[] ucs = ucBll.obterListaUcs();
        if (ucs.length == 0) {
            view.mostrarErroNaoEncontrado("UCs");
            return;
        }

        view.mostrarListaUcs(ucs);
        int escolha = view.pedirOpcaoUc(ucs.length);
        if (escolha == -1) {
            view.mostrarOperacaoCancelada();
            return;
        }
        String siglaUc = ucs[escolha - 1].split(" - ")[0];

        // Obter cursos associados antes de tentar remover
        List<String> cursosAssociados = ucDAL.obterCursosPorUc(siglaUc, ConfigApp.PASTA_BD);

        if (view.confirmarRemocaoBoolean(siglaUc)) {
            if (gestorBll.removerUc(siglaUc)) {
                view.mostrarSucessoRemocao("UC");
            } else {
                view.mostrarErroRemocaoUcComCursos(siglaUc, cursosAssociados);
            }
        }
    }

    private void removerAssociacaoUcCurso() {
        try {
            String[] ucs = ucBll.obterListaUcs();
            if (ucs.length == 0) {
                view.mostrarErroNaoEncontrado("UCs");
                return;
            }
            view.mostrarListaUcs(ucs);
            int escolhaUc = view.pedirOpcaoUc(ucs.length);
            if (escolhaUc == -1) return;
            String siglaUc = ucs[escolhaUc - 1].split(" - ")[0];

            // Obter cursos associados a esta UC
            List<String> cursosAssociados = ucDAL.obterCursosPorUc(siglaUc, ConfigApp.PASTA_BD);
            if (cursosAssociados.isEmpty()) {
                view.mostrarMensagem("Esta UC não está associada a nenhum curso.");
                return;
            }

            view.mostrarMensagem("Cursos associados a " + siglaUc + ":");
            for (int i = 0; i < cursosAssociados.size(); i++) {
                view.mostrarMensagem("  [" + (i+1) + "] " + cursosAssociados.get(i));
            }
            view.mostrarMensagem("  [0] Cancelar");
            int escolhaCurso = view.pedirOpcaoCurso(cursosAssociados.size());
            if (escolhaCurso == -1 || escolhaCurso == 0) return;
            String siglaCurso = cursosAssociados.get(escolhaCurso - 1);

            if (ucDAL.removerAssociacaoUcCurso(siglaUc, siglaCurso, ConfigApp.PASTA_BD)) {
                view.mostrarSucessoAssociacaoRemovida();
            } else {
                view.mostrarErroAssociacaoRemovida();
            }
        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    /**
     * Gere o sub-menu dedicado a consultas estatísticas.
     */
    private void menuEstatisticas() {
        boolean correr = true;
        while (correr) {
            int opcao = view.mostrarMenuEstatisticas();
            switch (opcao) {
                case 1: mostrarMediaGlobal(); break;
                case 2: mostrarMelhorAluno(); break;
                case 0: correr = false;       break;
                default: view.mostrarOpcaoInvalida();
            }
        }
    }

    /**
     * Gere o sub-menu para operações CRUD (Criar, Ler, Atualizar, Remover) em UCs.
     */
    private void menuGerirUcs() {
        boolean correr = true;
        while (correr) {
            int opcao = view.mostrarMenuCRUD("Unidades Curriculares");
            switch (opcao) {
                case 1: adicionarUc();                                          break;
                case 2: view.mostrarResultadosListagem(new String[] { gestorBll.listarTodasUcs() });break;
                case 3: editarUc();                                             break;
                case 4: removerUc();break;
                case 5: associarUcExistente(); break;
                case 6: removerAssociacaoUcCurso(); break;
                case 0: correr = false;                                         break;
                default: view.mostrarOpcaoInvalida();
            }
        }
    }

    /**
     * Gere o sub-menu para operações CRUD em Cursos.
     */
    private void menuGerirCurso() {
        boolean correr = true;
        while (correr) {
            int opcao = view.mostrarMenuCRUD("Cursos");
            switch (opcao) {
                case 1: executarCriarCurso(); break;
                case 2: view.mostrarResultadosListagem(new String[] { gestorBll.obterPainelCursos() }); break;
                case 3: executarEditarCurso(); break;
                case 4: executarApagarCurso(); break;
                case 5: listarUcsCurso(); break;
                case 0: correr = false;   break;
                default: view.mostrarOpcaoInvalida();
            }
        }
    }

    /**
     * Permite a criação de um novo curso, validando o departamento
     * e garantindo que a propina é válida (máx 2 casas decimais e positiva).
     */
    private void adicionarCurso() {
        try {
            String siglaCurso = view.pedirSiglaCurso();
            String nomeCurso = view.pedirNomeCurso();

            if (view.perguntarVerListagem("Departamentos")) {
                view.mostrarResultadosListagem(gestorBll.obterListaDepartamentos());
            }

            String siglaDep;
            do {
                siglaDep = view.pedirDepartamento();
                if (!gestorBll.isDepartamentoDuplicado(siglaDep)) {
                    view.mostrarMensagem("ERRO: Departamento não encontrado. Introduza uma sigla existente.");
                }
            } while (!gestorBll.isDepartamentoDuplicado(siglaDep));

            double propina;
            boolean propinaValida = false;
            do {
                propina = view.pedirValorDouble("Propina anual (€)");

                if (propina != Math.round(propina * 100.0) / 100.0) {
                    view.mostrarMensagem("ERRO: A propina só pode ter até 2 casas decimais (cêntimos).");
                } else if (propina < 0) {
                    view.mostrarMensagem("ERRO: A propina não pode ter um valor negativo.");
                } else {
                    propinaValida = true;
                }
            } while (!propinaValida);

            gestorBll.adicionarCurso(siglaCurso, nomeCurso, siglaDep, propina);
            view.mostrarSucessoCriacao("Curso");

        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    /** Permite editar o nome, departamento e propina de um curso sem alocações. */
    private void editarCurso() {
        try {
            String[] cursos = gestorBll.listarTodosCursos();
            if (cursos.length == 0) { view.mostrarErroNaoEncontrado("Cursos"); return; }
            view.mostrarListaCursos(cursos);
            int escolha = view.pedirOpcaoCurso(cursos.length);
            if (escolha == -1) { view.mostrarOperacaoCancelada(); return; }
            String sigla = cursos[escolha - 1].split(" - ")[0];

            if (!gestorBll.isCursoAlteravel(sigla)) {
                view.mostrarErroCursoComAlocacoes(); return;
            }
            view.mostrarMensagemModoEdicao();

            String novoNome = view.pedirNomeCurso();

            String siglaDep;
            do {
                siglaDep = view.pedirDepartamento();
                if (!gestorBll.isDepartamentoDuplicado(siglaDep)) {
                    view.mostrarMensagem("ERRO: Departamento não encontrado. Introduza uma sigla existente.");
                }
            } while (!gestorBll.isDepartamentoDuplicado(siglaDep));

            double novaPropina;
            boolean propinaValida = false;
            do {
                novaPropina = view.pedirValorDouble("Nova Propina anual (€)");
                if (novaPropina != Math.round(novaPropina * 100.0) / 100.0) {
                    view.mostrarMensagem("ERRO: A propina só pode ter até 2 casas decimais (cêntimos).");
                } else if (novaPropina < 0) {
                    view.mostrarMensagem("ERRO: A propina não pode ter um valor negativo.");
                } else {
                    propinaValida = true;
                }
            } while (!propinaValida);

            boolean sucesso = gestorBll.editarCurso(sigla, novoNome, siglaDep, novaPropina);
            if (sucesso) view.mostrarSucessoAtualizacao("Curso");
        } catch (CancelamentoException e) { view.mostrarOperacaoCancelada(); }
    }

    /** Remove um curso que não tenha estudantes nem docentes alocados. */
    private void removerCurso() {
        String[] cursos = gestorBll.listarTodosCursos();
        if (cursos.length == 0) { view.mostrarErroNaoEncontrado("Cursos"); return; }
        view.mostrarListaCursos(cursos);
        int escolha = view.pedirOpcaoCurso(cursos.length);
        if (escolha == -1) { view.mostrarOperacaoCancelada(); return; }
        String sigla = cursos[escolha - 1].split(" - ")[0];

        if (!gestorBll.isCursoAlteravel(sigla)) {
            view.mostrarErroCursoComAlocacoes(); return;
        }
        if (view.confirmarRemocaoBoolean(sigla)) {
            if (gestorBll.removerCurso(sigla)) view.mostrarSucessoRemocao("Curso");
            else                               view.mostrarErroRemocao("Curso");
        }
    }

    /** Lista as UCs de um curso agrupadas por ano curricular. */
    private void listarUcsCurso() {
        try {
            String siglaCurso = obterSiglaCursoPelaView(false);
            if (siglaCurso == null || siglaCurso.isEmpty()) return;
            view.mostrarMensagem(gestorBll.listarUcsPorCurso(siglaCurso));
            utils.Consola.pausar();
        } catch (CancelamentoException e) { view.mostrarOperacaoCancelada(); }
    }

    /**
     * Fluxo para associar uma UC existente a um curso, com listagem opcional.
     */
    private void associarUcExistente() {
        try {
            String[] ucs = ucBll.obterListaUcs();
            if (ucs.length == 0) { view.mostrarErroNaoEncontrado("UCs"); return; }
            view.mostrarListaUcs(ucs);
            int escolhaUc = view.pedirOpcaoUc(ucs.length);
            if (escolhaUc == -1) return;
            String siglaUc = ucs[escolhaUc - 1].split(" - ")[0];
            String siglaCurso = obterSiglaCursoPelaView(false);

            int ano = Integer.parseInt(view.pedirAnoCurricular());
            if (ano < 1 || ano > 3) {
                view.mostrarMensagem("ERRO: Ano deve ser 1, 2 ou 3.");
                return;
            }

            if (gestorBll.associarUcExistente(siglaUc, siglaCurso, ano)) {
                view.mostrarSucessoAtualizacao("UC associada ao curso " + siglaCurso);
            } else {
                view.mostrarErroLimiteUcs(ano);
            }

        } catch (CancelamentoException | NumberFormatException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    /**
     * Mostra a lista de cursos e devolve a sigla escolhida.
     * @param apenasComUcs Se true, obriga a escolher um curso que já tenha UCs (para Estudantes).
     * Se false, permite escolher qualquer curso (para Gestão de UCs).
     */
    private String obterSiglaCursoPelaView(boolean apenasComUcs) {
        String[] cursos = gestorBll.obterListaCursos();
        if (cursos.length == 0) return view.pedirSiglaCurso();

        bll.CursoBLL cursoBLL = new bll.CursoBLL();
        view.mostrarListaCursos(cursos);

        while (true) {
            int escolha = view.pedirOpcaoCurso(cursos.length);
            if (escolha == -1) throw new CancelamentoException();

            String sigla = cursos[escolha - 1].split(" - ")[0];

           if (apenasComUcs && !cursoBLL.verificarCursoTemUcs(sigla)) {
                view.mostrarMensagem("ERRO: Este curso ainda não tem UCs configuradas para o 1º ano. Escolha outro.");
                continue;
            }
            return sigla;
        }
    }

    private void menuAnoLetivo() {
        new AnoLetivoController(repo).iniciar(view);
    }

    private void alterarPassword() {
        view.mostrarCabecalhoAlterarPassword();
        String novaPass = view.pedirNovaPassword();
        if (!novaPass.trim().isEmpty()) {
            gestorBll.alterarPasswordGestor(gestor, novaPass);
            view.mostrarSucessoAlteracaoPassword();
        } else {
            view.mostrarCancelamentoPassword();
        }
    }
}
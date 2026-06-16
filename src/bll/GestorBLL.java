package bll;

import common.ConfigApp;

import dal.*;
import dal.DepartamentoDAL;
import dal.DepartamentoDALFile;
import dal.DepartamentoDALSql;
import dal.DocenteDAL;
import dal.DocenteDALFile;
import dal.DocenteDALSql;
import model.*;
import controller.LoginController;
import utils.*;
import view.GestorView;
import utils.Config;
import java.util.ArrayList;
import java.util.List;


/**
 * Lógica de negócio do perfil Gestor.
 * Centraliza as operações de backoffice: avanço do ano letivo,
 * registo de utilizadores, gestão de cursos e UCs,
 * estatísticas e listagem de devedores.
 */
public class GestorBLL {

    private static final String PASTA_BD = ConfigApp.PASTA_BD;
    private final CursoDAL cursoDAL = ConfigApp.isModoSql() ? new CursoDALSql() : new CursoDALFile();
    private final UcDAL ucDAL = ConfigApp.isModoSql() ? new UcDALSql() : new UcDALFile();
    private final EstudanteDAL estudanteDAL = ConfigApp.isModoSql() ? new EstudanteDALSql() : new EstudanteDALFile();
    private final LoginController loginController = new LoginController();
    private final DepartamentoDAL departamentoDAL =
            ConfigApp.isModoSql() ? new DepartamentoDALSql() : new DepartamentoDALFile();
    private final DocenteDAL docenteDAL =
            ConfigApp.isModoSql() ? new DocenteDALSql() : new DocenteDALFile();
    private final InscricaoDAL inscricaoDAL =
            ConfigApp.isModoSql() ? new InscricaoDALSql() : new InscricaoDALFile();
    private final HistoricoDAL historicoDAL =
            ConfigApp.isModoSql() ? new HistoricoDALSql() : new HistoricoDALFile();

    public GestorBLL() {
        inscricaoDAL.inicializar();
        historicoDAL.inicializar();
    }

    // ─────────────────────────── ANO LETIVO ────────────────────────────

    /**
     * Avança o ano letivo para todos os estudantes do sistema.
     * Verifica o quórum mínimo de 5 alunos no 1.º ano de cada curso e
     * delega a transição dos alunos para InscricaoBLL.
     *
     * PATCH (Bug 4): O bloco de guardar histórico académico foi REMOVIDO daqui.
     * Essa responsabilidade pertence exclusivamente a AnoLetivoBLL.fechar(),
     * que é sempre chamado antes de avancar(). Manter o bloco aqui causava
     * registos duplicados no ficheiro historico.csv.
     *
     * @param repo Repositório de sessão; o ano letivo é incrementado no final.
     * @param view Vista do gestor para feedback de cada passo.
     */
    public void avancarAnoLetivo(RepositorioDados repo, GestorView view) {
        view.mostrarCabecalhoArranqueAnoLetivo();

        int anoLetivoCorrente = repo.getAnoAtual();
        int proximoAnoLetivo  = anoLetivoCorrente + 1;

        // ── Verificar se existem alunos com propinas em dívida ─────────
        List<Estudante> todosEstudantes = estudanteDAL.carregarTodos();
        List<Estudante> devedores = new ArrayList<>();
        for (Estudante e : todosEstudantes) {
            if (e != null && e.getSaldoDevedor() > 0) {
                devedores.add(e);
            }
        }
        if (!devedores.isEmpty()) {
            view.mostrarErroAvancoBloqueadoPorDividas(devedores);
            return; // interrompe o avanço
        }

        // ── Verificação de quórum ──────────────────────────────────────
        String[] cursos = cursoDAL.obterListaCursos(PASTA_BD);
        if (cursos.length == 0) {
            view.mostrarErroCarregarDados("Cursos");
            return;
        }

        view.mostrarVerificacaoQuorum();
        CursoBLL cursoBll = new CursoBLL();
        for (String c : cursos) {
            String sigla = c.split(" - ")[0];
            Curso curso  = cursoBll.procurarCursoCompleto(sigla);
            if (curso == null) continue;

            // Chamadas atualizadas usando a instância estudanteDAL
            int a1 = estudanteDAL.contarEstudantesPorCursoEAno(sigla, 1);
            int a2 = estudanteDAL.contarEstudantesPorCursoEAno(sigla, 2);
            int a3 = estudanteDAL.contarEstudantesPorCursoEAno(sigla, 3);

            if (a1 > 0 && a1 < 5) {
                view.mostrarErroQuorum(sigla, a1);
                curso.setEstado("Inativo");
            } else if (a1 >= 5 || a2 >= 1 || a3 >= 1) {
                view.mostrarSucessoQuorum(sigla);
                curso.setEstado("Ativo");
            } else {
                curso.setEstado("Inativo");
            }
            cursoDAL.atualizarCurso(curso, PASTA_BD);
        }

        // ── Transição dos alunos ───────────────────────────────────────
        view.mostrarProcessamentoTransicoes();

        AnoLetivo anoAtualObj    = new AnoLetivo(anoLetivoCorrente);
        AnoLetivo anoSeguinteObj = new AnoLetivo(proximoAnoLetivo);

        InscricaoBLL inscricaoBLL  = new InscricaoBLL();
        OperationResult resultado  = inscricaoBLL.transitarAlunos(anoAtualObj, anoSeguinteObj);

        if (!resultado.isSucesso()) {
            view.mostrarMensagem("ERRO: " + resultado.getMensagem());
            return; // não avança o ano se a transição falhar
        } else {
            view.mostrarMensagem(resultado.getMensagem());
        }

        repo.setAnoAtual(proximoAnoLetivo);
        view.mostrarSucessoAvancoAno(proximoAnoLetivo);
    }

    // ─────────────────────────── REGISTO ───────────────────────────────

    /**
     * Regista um novo docente no sistema.
     * A sigla é gerada automaticamente a partir das iniciais do nome,
     * garantindo unicidade. O email e a password são gerados e enviados
     * ao docente sem visualização na consola.
     *
     * @param nome     Nome completo do docente.
     * @param nif      NIF com 9 dígitos.
     * @param morada   Morada de residência.
     * @param dataNasc Data de nascimento (DD-MM-AAAA).
     * @return Array [email, sigla] com as credenciais atribuídas.
     */
    public String[] registarDocente(String nome, String nif, String morada, String dataNasc) {
        String sigla     = gerarSiglaUnica(nome);
        String email     = EmailGenerator.gerarEmailDocente(sigla);
        String passLimpa = PasswordGenerator.gerarPasswordSegura();
        EmailService.enviarCredenciaisTodos(nome, email, passLimpa);
        docenteDAL.adicionarDocente(
                new Docente(sigla, email, "", nome, nif, morada, dataNasc));
        loginController.criarCredencial(email, passLimpa, "DOCENTE");
        return new String[]{email, sigla};
    }

    /**
     * Regista um novo estudante no sistema.
     * Gera número mecanográfico, email e password; cria a propina inicial;
     * inscreve nas UCs do 1.º ano; envia as credenciais por email.
     *
     * @param nome         Nome completo.
     * @param nif          NIF com 9 dígitos.
     * @param morada       Morada de residência.
     * @param dataNasc     Data de nascimento (DD-MM-AAAA).
     * @param siglaCurso   Sigla do curso.
     * @param anoInscricao Ano letivo da matrícula.
     * @return Email institucional gerado.
     */
    public String registarEstudante(String nome, String nif, String morada,
                                    String dataNasc, String siglaCurso, int anoInscricao) {

        AnoLetivoBLL anoBll = new AnoLetivoBLL();
        EstadoAnoLetivo estado = anoBll.getEstadoAnoAtual();
        if (estado != EstadoAnoLetivo.PLANEAMENTO) {
            System.err.println(">> Registo de estudante bloqueado: ano letivo não está em PLANEAMENTO (estado atual: " + estado + ").");
            return null;
        }

        // Chamada atualizada usando a instância estudanteDAL
        int    numMec    = estudanteDAL.obterProximoNumeroMecanografico(anoInscricao);
        String email     = EmailGenerator.gerarEmailEstudante(numMec);
        String passLimpa = PasswordGenerator.gerarPasswordSegura();
        EmailService.enviarCredenciaisTodos(nome, email, passLimpa);

        Estudante novo = new Estudante(numMec, email, "", nome, nif, morada, dataNasc, anoInscricao);
        Curso curso    = new CursoBLL().procurarCursoCompleto(siglaCurso);
        if (curso != null) novo.setSaldoDevedor(curso.getValorPropinaAnual());

        // Chamada atualizada usando a instância estudanteDAL
        loginController.criarCredencial(email, passLimpa, "ESTUDANTE");
        estudanteDAL.adicionarEstudante(novo, siglaCurso);

        for (String siglaUc : ucDAL.obterSiglasUcsPorCursoEAno(siglaCurso, 1, PASTA_BD)) {
            inscricaoDAL.adicionarInscricao(numMec, siglaUc, anoInscricao);
        }
        return email;
    }

    /**
     * Regista um novo departamento no sistema.
     *
     * @param sigla Sigla do departamento.
     * @param nome  Nome completo.
     */
    public void registarDepartamento(String sigla, String nome) {
        Departamento dep = new Departamento(sigla.toUpperCase(), nome);
        departamentoDAL.criar(dep);
    }

    // ─────────────────────────── CURSOS E UCs ──────────────────────────

    /**
     * Adiciona uma nova UC a um curso, respeitando o limite de 5 UCs por ano.
     *
     * @param siglaCurso   Sigla do curso (pode ser null para UC sem curso).
     * @param anoUc        Ano curricular da UC (1, 2 ou 3).
     * @param siglaUc      Sigla da nova UC.
     * @param nomeUc       Nome completo.
     * @param siglaDocente Sigla do docente responsável.
     * @return true se a UC foi adicionada; false se o limite de 5 foi atingido.
     */
    public boolean adicionarUc(String siglaCurso, int anoUc, String siglaUc,
                               String nomeUc, String siglaDocente) {
        if (siglaCurso != null && !siglaCurso.equals("N/A")) {
            if (ucDAL.contarUcsPorCursoEAno(siglaCurso, anoUc, PASTA_BD) >= 5) {
                return false;
            }
        }
        Docente doc = docenteDAL.procurarPorSigla(siglaDocente);
        String cursoParaGuardar = (siglaCurso == null || siglaCurso.isEmpty()) ? "N/A" : siglaCurso;
        ucDAL.adicionarUC(new UnidadeCurricular(siglaUc, nomeUc, anoUc, doc), cursoParaGuardar, PASTA_BD);
        return true;
    }

    /**
     * Edita uma UC existente substituindo o registo antigo pelo novo.
     *
     * PATCH (Bug 1 — CRÍTICO): O parsing de {@code ano} é agora validado ANTES
     * de remover a UC antiga. Na versão anterior, a UC era apagada primeiro e,
     * se {@code parseInt} lançasse {@code NumberFormatException}, o registo
     * desaparecia sem ser recriado — perda permanente de dados no CSV.
     *
     * @return true se a edição foi bem-sucedida; false caso contrário.
     */
    public boolean editarUc(String siglaAntiga, String novaSigla, String nome,
                            String ano, String siglaDocente, String siglaCurso) {
        int anoInt;
        try {
            anoInt = Integer.parseInt(ano);
        } catch (NumberFormatException ex) {
            return false;
        }

        if (!ucDAL.removerUC(siglaAntiga, PASTA_BD)) return false;

        Docente doc = docenteDAL.procurarPorSigla(siglaDocente);
        ucDAL.adicionarUC(new UnidadeCurricular(novaSigla, nome, anoInt, doc), siglaCurso, PASTA_BD);
        return true;
    }

    /**
     * Remove uma UC pela sua sigla.
     * Falha se a UC ainda estiver associada a um curso.
     *
     * @return true se a UC existia e foi removida.
     */
    public boolean removerUc(String siglaUc) {
        if (ucDAL.temCursoAssociado(siglaUc, PASTA_BD)) {
            return false;
        }
        return ucDAL.removerUC(siglaUc, PASTA_BD);
    }

    /**
     * Associa uma UC já existente no sistema a um novo curso e ano curricular.
     *
     * @return true se associada com sucesso; false se o limite de 5 UCs for atingido.
     */
    public boolean associarUcExistente(String siglaUc, String siglaCurso, int ano) {
        if (ucDAL.contarUcsPorCursoEAno(siglaCurso, ano, PASTA_BD) >= 5) return false;

        UnidadeCurricular uc = ucDAL.procurarUC(siglaUc, PASTA_BD);
        if (uc == null) return false;

        UnidadeCurricular novaAssociacao = new UnidadeCurricular(
                uc.getSigla(), uc.getNome(), ano, uc.getDocenteResponsavel(), uc.getEcts());
        ucDAL.adicionarUC(novaAssociacao, siglaCurso, PASTA_BD);
        return true;
    }

    /**
     * Cria um novo curso no estado "Inativo".
     *
     * @param sigla    Sigla identificadora.
     * @param nome     Nome completo.
     * @param siglaDep Sigla do departamento.
     * @param propina  Valor da propina anual em euros.
     */
    public void adicionarCurso(String sigla, String nome, String siglaDep, double propina) {
        Departamento dep = departamentoDAL.procurarPorSigla(siglaDep);
        Curso c = new Curso(sigla, nome, dep, propina);
        c.setEstado("Inativo");
        cursoDAL.adicionarCurso(c, PASTA_BD);
    }

    /**
     * Verifica se um curso pode ser editado ou removido.
     * Um curso não pode ser alterado enquanto tiver estudantes ou UCs alocadas.
     *
     * @param sigla Sigla do curso a verificar.
     * @return true se o curso não tiver alocações.
     */
    public boolean isCursoAlteravel(String sigla) {
        int totalAlunos =
                estudanteDAL.contarEstudantesPorCursoEAno(sigla, 1)
                        + estudanteDAL.contarEstudantesPorCursoEAno(sigla, 2)
                        + estudanteDAL.contarEstudantesPorCursoEAno(sigla, 3);
        int totalUcs =
                ucDAL.contarUcsPorCursoEAno(sigla, 1, PASTA_BD)
                        + ucDAL.contarUcsPorCursoEAno(sigla, 2, PASTA_BD)
                        + ucDAL.contarUcsPorCursoEAno(sigla, 3, PASTA_BD);
        return totalAlunos == 0 && totalUcs == 0;
    }

    /**
     * Edita o nome, departamento e propina de um curso sem alocações.
     *
     * @return false se o curso tiver alocações ou não existir.
     */
    public boolean editarCurso(String sigla, String novoNome, String siglaDep, double novaPropina) {
        if (!isCursoAlteravel(sigla)) return false;
        Curso original = new CursoBLL().procurarCursoCompleto(sigla);
        if (original == null) return false;
        Departamento dep = departamentoDAL.procurarPorSigla(siglaDep);
        Curso atualizado = new Curso(sigla, novoNome, dep, novaPropina);
        atualizado.setEstado(original.getEstado());
        cursoDAL.atualizarCurso(atualizado, PASTA_BD);
        return true;
    }

    /**
     * Remove um curso sem alocações.
     *
     * @return false se o curso tiver alocações.
     */
    public boolean removerCurso(String sigla) {
        if (!isCursoAlteravel(sigla)) return false;
        return cursoDAL.removerCurso(sigla, PASTA_BD);
    }

    // ─────────────────────────── ESTATÍSTICAS ──────────────────────────

    /**
     * Calcula a soma total de notas e o número de momentos lançados.
     *
     * @return Array [soma, total] para calcular a média global.
     */
    public double[] calcularEstatisticasGlobais() {
        return Estatisticas.calcularDadosMediaGlobal();
    }

    /**
     * Devolve o estudante com melhor média global.
     *
     * @return Array [Estudante, Double] com o melhor aluno e a sua média.
     */
    public Object[] obterMelhorAluno() {
        return Estatisticas.calcularMelhorAluno();
    }

    // ─────────────────────────── LISTAGENS ─────────────────────────────

    /**
     * @return Array "SIGLA - Nome" de todos os cursos.
     */
    public String[] obterListaCursos() {
        return cursoDAL.obterListaCursos(PASTA_BD);
    }

    /**
     * @return Array "SIGLA - Nome" de todos os cursos.
     *
     * PATCH (Bug 3): Mantido por compatibilidade com chamadas existentes no
     * GestorController (editarCurso, removerCurso). Delega em obterListaCursos().
     * Numa refatoração futura, substituir todas as chamadas por obterListaCursos()
     * e remover este método.
     */
    public String[] listarTodosCursos() {
        return obterListaCursos(); // delega — elimina código duplicado
    }

    /**
     * @return Painel formatado com todas as UCs (texto multi-linha, não um array).
     *
     * PATCH (Bug 2): O Javadoc e o nome do método sugeriam um array (String[]),
     * mas o método sempre devolveu uma String formatada de ucDAL.listarTodasUc().
     * O comentário "Removed the []" foi removido; o contrato está agora correto.
     */
    public String listarTodasUcs() {
        return ucDAL.listarTodasUc(PASTA_BD);
    }

    /** @return Painel detalhado de cursos com estado e ano letivo atual. */
    public String obterPainelCursos() {
        return cursoDAL.listarCursosDetalhados(PASTA_BD, Config.getAnoAtual());
    }

    /** @return Painel detalhado de UCs com docentes, momentos e alunos inscritos. */
    public String obterPainelUcs() {
        return ucDAL.listarUcsDetalhadas(PASTA_BD, Config.getAnoAtual());
    }

    /** @return Texto formatado com as UCs de um curso agrupadas por ano curricular. */
    public String listarUcsPorCurso(String siglaCurso) {
        return ucDAL.listarUcsPorCurso(siglaCurso, PASTA_BD);
    }

    /**
     * Devolve os estudantes com saldo devedor positivo.
     *
     * @return Lista de estudantes com dívida de propina.
     */
    public List<Estudante> obterListaDevedores() {
        List<Estudante> devedores = new ArrayList<>();
        for (Estudante e : estudanteDAL.carregarTodos())
            if (e != null && e.getSaldoDevedor() > 0) devedores.add(e);
        return devedores;
    }

    /** @return Array "SIGLA - Nome" de todos os departamentos. */
    public String[] obterListaDepartamentos() {
        return departamentoDAL.obterListaFormatada();
    }

    /** @return Array "SIGLA - Nome" de todos os docentes. */
    public String[] obterListaDocentes() {
        return docenteDAL.obterListaDocentes();
    }

    /** @return Lista de linhas do histórico académico para um dado ano letivo. */
    public List<String> obterHistoricoPorAno(int ano) {
        return historicoDAL.consultarHistoricoPorAno(ano);
    }

    // ─────────────────────── SEGURANÇA E VALIDAÇÃO ─────────────────────

    /**
     * Altera a password do gestor com hashing e persistência.
     *
     * @param gestor   Gestor autenticado.
     * @param novaPass Nova password em texto limpo.
     */
    public void alterarPasswordGestor(Gestor gestor, String novaPass) {
        loginController.atualizarPassword(gestor.getEmail(), novaPass);
    }

    /**
     * Verifica se um NIF já está registado em estudantes ou docentes.
     *
     * @param nif NIF a verificar.
     * @return true se o NIF já existir.
     */
    public boolean isNifDuplicado(String nif) {
        return estudanteDAL.existeNif(nif) || docenteDAL.existeNif(nif);
    }

    /**
     * Verifica se já existe um departamento com a sigla fornecida.
     *
     * @param sigla Sigla a verificar.
     * @return true se o departamento já existir.
     */
    public boolean isDepartamentoDuplicado(String sigla) {
        return departamentoDAL.existe(sigla);
    }

    /**
     * Verifica se já existe um docente com a sigla fornecida.
     *
     * @param sigla Sigla a verificar.
     * @return true se o docente já existir.
     */
    public boolean existeDocente(String sigla) {
        return docenteDAL.existeSigla(sigla);
    }

    // ─────────────────────────── UTILITÁRIOS ───────────────────────────

    /**
     * Gera uma sigla de exatamente 3 letras única para um docente.
     *
     * Passo 1: Extrai as iniciais das primeiras 3 palavras (ex: Ana Sofia Gomes → ASG).
     * Passo 2: Em caso de colisão, mantém as 2 primeiras letras e itera a 3.ª
     *          pelas restantes letras do nome (ex: ASG → ASO → ASF…).
     * Passo 3: Se as letras do nome se esgotarem, itera todo o alfabeto (A–Z).
     * Passo 4: Em último recurso, combina o alfabeto na 2.ª e 3.ª posições.
     *
     * @param nome Nome completo inserido pelo utilizador.
     * @return Sigla única de 3 letras maiúsculas.
     */
    private String gerarSiglaUnica(String nome) {
        String   nomeLimpo = nome.trim().toUpperCase().replaceAll("[^A-Z ]", "");
        String[] palavras  = nomeLimpo.split("\\s+");
        StringBuilder base = new StringBuilder();

        for (int i = 0; i < Math.min(3, palavras.length); i++) {
            if (!palavras[i].isEmpty()) base.append(palavras[i].charAt(0));
        }

        int idx = 1;
        while (base.length() < 3 && palavras.length > 0 && idx < palavras[0].length()) {
            base.append(palavras[0].charAt(idx++));
        }

        while (base.length() < 3) base.append('X');

        String candidata = base.toString().substring(0, 3);

        if (!docenteDAL.existeSigla(candidata)) return candidata;

        String prefixo           = candidata.substring(0, 2);
        String todasLetrasDoNome = nomeLimpo.replace(" ", "");

        for (int i = 0; i < todasLetrasDoNome.length(); i++) {
            String tentativa = prefixo + todasLetrasDoNome.charAt(i);
            if (!tentativa.equals(candidata) && !docenteDAL.existeSigla(tentativa))
                return tentativa;
        }

        for (char c = 'A'; c <= 'Z'; c++) {
            String tentativa = prefixo + c;
            if (!docenteDAL.existeSigla(tentativa)) return tentativa;
        }

        String primeiraLetra = candidata.substring(0, 1);
        for (char c2 = 'A'; c2 <= 'Z'; c2++) {
            for (char c3 = 'A'; c3 <= 'Z'; c3++) {
                String tentativa = primeiraLetra + c2 + c3;
                if (!docenteDAL.existeSigla(tentativa)) return tentativa;
            }
        }

        return candidata;
    }

    /**
     * Devolve as siglas das UCs em que o estudante já obteve aprovação.
     * Usado na transição de ano para remover essas inscrições do ficheiro.
     *
     * @param e Estudante com percurso académico carregado.
     * @return Lista de siglas de UCs aprovadas.
     */
    private List<String> obterSiglasUcsAprovadas(Estudante e) {
        List<String> aprovadas = new ArrayList<>();
        for (int i = 0; i < e.getPercurso().getTotalAvaliacoes(); i++) {
            model.Avaliacao av = e.getPercurso().getHistoricoAvaliacoes()[i];
            if (av != null && av.isAprovado() && av.getUc() != null) {
                String sigla = av.getUc().getSigla();
                if (!aprovadas.contains(sigla)) aprovadas.add(sigla);
            }
        }
        return aprovadas;
    }
}
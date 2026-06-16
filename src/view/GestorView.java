package view;

import model.Docente;
import model.Estudante;
import utils.CancelamentoException;
import utils.Consola;
import model.Departamento;
import model.Curso;
import java.util.List;


/**
 * View do Gestor. Usa Consola para toda a apresentação e leitura.
 * pedirInput() → Consola.lerString() — "sair" lança CancelamentoException.
 * Menus → Consola.lerOpcaoMenu() — "0" é saída legítima.
 */
public class GestorView {

    // ---------- MENUS ----------

    public int mostrarMenu() {
        Consola.imprimirCabecalho("Portal Gestor — ISSMF");
        Consola.imprimirMenu(new String[]{
                "Gerir Estudante",
                "Gerir Docente",
                "Gerir Departamento",
                "Gerir Unidades Curriculares",
                "Gerir Cursos",
                "Ver Estatísticas",
                "Ano Letivo",
                "Consultar Histórico de Anos Anteriores",
                "Listar Devedores de Propinas",
                "Alterar Password",
        }, "Sair / Logout");
        return Consola.lerOpcaoMenu();
    }

    // ---------- SUBMENUS ----------

    public int mostrarSubMenuEstudante() {
        Consola.imprimirCabecalho("Gerir Estudantes");
        Consola.imprimirMenu(new String[]{
                "Criar Estudante",
                "Listar Estudantes",
                "Editar Estudante",
                "Apagar Estudante"
        }, "Voltar");
        return Consola.lerOpcaoMenu();
    }

    public int mostrarSubMenuDocente() {
        Consola.imprimirCabecalho("Gerir Docentes");
        Consola.imprimirMenu(new String[]{
                "Criar Docente",
                "Listar Docentes",
                "Editar Docente",
                "Apagar Docente"
        }, "Voltar");
        return Consola.lerOpcaoMenu();
    }

    public int mostrarSubMenuDepartamento() {
        Consola.imprimirCabecalho("Gerir Departamentos");
        Consola.imprimirMenu(new String[]{
                "Criar Departamento",
                "Listar Departamentos",
                "Editar Departamento",
                "Apagar Departamento"
        }, "Voltar");
        return Consola.lerOpcaoMenu();
    }

    public int mostrarSubMenuCurso() {
        Consola.imprimirCabecalho("Gerir Cursos");
        Consola.imprimirMenu(new String[]{
                "Criar Curso",
                "Listar Cursos",
                "Editar Curso",
                "Apagar Curso"
        }, "Voltar");
        return Consola.lerOpcaoMenu();
    }

    public int mostrarMenuCRUD(String entidade) {
        boolean ehUC    = entidade.equalsIgnoreCase("Unidades Curriculares");
        boolean ehCurso = entidade.equalsIgnoreCase("Cursos");
        String[] opcoes;

        if (ehUC) {
            opcoes = new String[]{
                    "Adicionar " + entidade,
                    "Listar " + entidade,
                    "Editar " + entidade,
                    "Remover " + entidade,
                    "Associar UC Existente a um Curso",
                    "Remover UC de um Curso"
            };
        } else if (ehCurso) {
            opcoes = new String[]{
                    "Adicionar " + entidade,
                    "Listar " + entidade,
                    "Editar " + entidade,
                    "Remover " + entidade,
                    "Listar UCs do Curso por Ano"
            };
        } else {
            opcoes = new String[]{
                    "Adicionar " + entidade,
                    "Listar " + entidade,
                    "Editar " + entidade,
                    "Remover " + entidade
            };
        }

        Consola.imprimirCabecalho("Gerir " + entidade);
        Consola.imprimirMenu(opcoes);
        return Consola.lerOpcaoMenu();
    }

    public int mostrarMenuEstatisticas() {
        Consola.imprimirCabecalho("Estatísticas — ISSMF");
        Consola.imprimirMenu(new String[]{
                "Média Global Institucional",
                "Melhor Aluno"
        });
        return Consola.lerOpcaoMenu();
    }

    // ---------- MÉTODOS AUXILIARES ----------

    public void mostrarTitulo(String titulo) { Consola.imprimirTitulo(titulo); }

    // ---------- MÉTODOS PARA ESTUDANTES ----------

    public int pedirNumeroEstudante() {
        return Consola.lerInt("Número Mecanográfico do Estudante");
    }

    public void mostrarEstudante(Estudante e) {
        System.out.printf("  %d - %s | %s | Ano: %d | Saldo: %.2f€%n",
                e.getNumeroMecanografico(), e.getNome(), e.getSiglaCurso(),
                e.getAnoCurricular(), e.getSaldoDevedor());
    }

    public void mostrarListaEstudantes(List<Estudante> estudantes) {
        Consola.imprimirTitulo("Lista de Estudantes");
        for (Estudante e : estudantes) mostrarEstudante(e);
        Consola.imprimirLinha();
    }

    public String pedirNovoNomeEstudante()         { return lerStringOpcional("Novo Nome (Enter mantém o actual)"); }
    public String pedirNovoNifEstudante()          { return lerStringOpcional("Novo NIF (Enter mantém o actual)"); }
    public String pedirNovaDataNascimentoEstudante(){ return lerStringOpcional("Nova Data Nascimento (DD-MM-AAAA) (Enter mantém a actual)"); }

    // ---------- MÉTODOS PARA DOCENTES ----------

    public String pedirSiglaDocenteParaGestao()    { return lerStringOpcional("Sigla do Docente (ex: JMS)"); }

    public void mostrarDocente(Docente d) {
        System.out.printf("  %s - %s | NIF: %s | %s%n",
                d.getSigla(), d.getNome(), d.getNif(), d.getEmail());
    }

    public void mostrarListaDocentes(List<Docente> docentes) {
        Consola.imprimirTitulo("Lista de Docentes");
        for (Docente d : docentes) mostrarDocente(d);
        Consola.imprimirLinha();
    }

    public String pedirNovoNomeDocente()           { return lerStringOpcional("Novo Nome (Enter mantém o actual)"); }
    public String pedirNovoNifDocente()            { return lerStringOpcional("Novo NIF (Enter mantém o actual)"); }
    public String pedirNovaMoradaDocente()         { return lerStringOpcional("Nova Morada (Enter mantém a actual)"); }
    public String pedirNovaDataNascimentoDocente() { return lerStringOpcional("Nova Data Nascimento (DD-MM-AAAA) (Enter mantém a actual)"); }

    public void mostrarErroDocenteComUcs() {
        Consola.imprimirErro("Não é possível remover o docente pois lecciona uma ou mais UCs.");
    }

    // ---------- MÉTODOS PARA DEPARTAMENTOS ----------

    public void mostrarDepartamento(Departamento d) {
        System.out.printf("  %s - %s%n", d.getSigla(), d.getNome());
    }

    public void mostrarListaDepartamentos(List<Departamento> departamentos) {
        Consola.imprimirTitulo("Lista de Departamentos");
        for (Departamento d : departamentos) mostrarDepartamento(d);
        Consola.imprimirLinha();
    }

    public String pedirNovoSiglaDepartamento() { return lerStringOpcional("Nova Sigla (Enter mantém a actual)"); }
    public String pedirNovoNomeDepartamento()  { return lerStringOpcional("Novo Nome (Enter mantém o actual)"); }

    // ---------- MÉTODOS PARA CURSOS ----------

    public double pedirPropinaCurso() { return Consola.lerDouble("Propina anual (€)"); }

    public void mostrarCurso(Curso c) {
        String dep = (c.getDepartamento() != null) ? c.getDepartamento().getSigla() : "N/A";
        System.out.printf("  %s - %s | Dep: %s | Propina: %.2f€ | Estado: %s%n",
                c.getSigla(), c.getNome(), dep, c.getValorPropinaAnual(), c.getEstado());
    }

    public void mostrarListaCursos(List<Curso> cursos) {
        Consola.imprimirTitulo("Lista de Cursos");
        for (Curso c : cursos) mostrarCurso(c);
        Consola.imprimirLinha();
    }

    public void mostrarListaCursos(String[] cursos) {
        Consola.imprimirTitulo("Cursos Disponíveis");
        for (int i = 0; i < cursos.length; i++) System.out.println("  [" + (i + 1) + "] " + cursos[i]);
        Consola.imprimirLinha();
    }

    public String pedirNovoNomeCurso()              { return lerStringOpcional("Novo Nome (Enter mantém o actual)"); }
    public String pedirNovoSiglaDepartamentoCurso() { return lerStringOpcional("Nova Sigla do Departamento (Enter mantém a actual)"); }

    public Double pedirNovaPropinaCurso() {
        String input = lerStringOpcional("Nova Propina (€) (Enter mantém a actual)");
        if (input.isEmpty()) return null;
        try { return Double.parseDouble(input.replace(",", ".")); }
        catch (NumberFormatException e) { return null; }
    }

    // ---------- ERROS DE CURSO ----------

    // FIX: método em falta — causava "cannot find symbol" no GestorController linha 550
    public void mostrarErroCursoExistente() {
        Consola.imprimirErro("Já existe um curso com esta sigla.");
    }

    public void mostrarErroCriacaoCurso() {
        Consola.imprimirErro("Erro ao criar curso (departamento inválido ou curso já existe).");
    }

    public void mostrarErroAtualizacaoCurso() {
        Consola.imprimirErro("Erro ao actualizar curso.");
    }

    public void mostrarErroPropinaNegativa()       { Consola.imprimirErro("Propina não pode ser negativa."); }
    public void mostrarErroPropinaNegativaMantida(){ Consola.imprimirErro("Propina não pode ser negativa. Mantido o valor anterior."); }
    public void mostrarErroPropinaDuasCasas()      { Consola.imprimirErro("Propina deve ter no máximo 2 casas decimais. Mantido o valor anterior."); }

    // ---------- ERROS DE DEPARTAMENTO ----------

    // FIX: método em falta — causava "cannot find symbol" no GestorController linha 558
    public void mostrarErroSemDepartamentos() {
        Consola.imprimirErro("Não existem departamentos registados. Crie um departamento primeiro.");
    }

    // FIX: método em falta — causava "cannot find symbol" no GestorController linha 566
    public void mostrarErroDepartamentoNaoEncontrado() {
        Consola.imprimirErro("Departamento não encontrado. Introduza uma sigla existente.");
    }

    // FIX: método em falta — causava "cannot find symbol" no GestorController linha 700
    public void mostrarErroCriarDepartamento() {
        Consola.imprimirErro("Erro ao criar departamento.");
    }

    public void mostrarErroAtualizarDepartamento() {
        Consola.imprimirErro("Não foi possível actualizar (sigla já existe ou departamento não encontrado).");
    }

    public void mostrarErroRemoverDepartamentoComCursos() {
        Consola.imprimirErro("Não é possível remover o departamento pois existem cursos associados.");
    }

    public void mostrarErroDepartamentoDuplicado() {
        Consola.imprimirErro("Já existe um departamento com essa sigla.");
    }

    // ---------- LISTAGENS ----------

    public void mostrarListaUcs(String[] ucs) {
        Consola.imprimirTitulo("Unidades Curriculares");
        for (int i = 0; i < ucs.length; i++) System.out.println("  [" + (i + 1) + "] " + ucs[i]);
        Consola.imprimirLinha();
    }

    public void mostrarResultadosListagem(String[] resultados) {
        Consola.imprimirTitulo("Resultados");
        if (resultados == null || resultados.length == 0) Consola.imprimirInfo("Sem resultados.");
        else for (String r : resultados) System.out.println("  " + r);
        Consola.imprimirLinha();
        Consola.pausar();
    }

    public boolean confirmarRemocaoBoolean(String sigla) {
        return Consola.lerSimNao("Confirmar remoção de '" + sigla + "'?");
    }

    public int pedirOpcaoCurso(int max) {
        while (true) {
            try {
                int opcao = Consola.lerInt("Número do Curso (1-" + max + ")");
                if (opcao >= 1 && opcao <= max) return opcao;
                Consola.imprimirErro("Opção fora do intervalo. Escolha entre 1 e " + max + ".");
            } catch (utils.CancelamentoException e) { return -1; }
        }
    }

    public int pedirOpcaoUc(int max) {
        while (true) {
            try {
                int opcao = Consola.lerInt("Número da UC (1-" + max + ")");
                if (opcao >= 1 && opcao <= max) return opcao;
                Consola.imprimirErro("Opção fora do intervalo. Escolha entre 1 e " + max + ".");
            } catch (utils.CancelamentoException e) { return -1; }
        }
    }

    // FIX: método em falta — causava "cannot find symbol" no GestorController linha 826
    public void mostrarOpcaoNaoAssociarCurso() {
        System.out.println("  [0] Não associar a nenhum curso");
    }

    // ---------- CAMPOS DE FORMULÁRIO ----------

    public String pedirNome()              { return pedirInput("Nome Completo"); }
    public String pedirNif()               { return pedirInput("NIF"); }
    public String pedirMorada()            { return pedirInput("Morada"); }
    public String pedirDataNascimento()    { return pedirInput("Data de Nascimento (DD-MM-AAAA)"); }
    public String pedirSiglaCurso()        { return pedirInput("Sigla do Curso"); }
    public String pedirAnoCurricular()     { return pedirInput("Ano Curricular (ex: 1, 2, 3)"); }
    public String pedirSiglaUc()           { return pedirInput("Sigla da UC (ex: POO, BD)"); }
    public String pedirNomeUc()            { return pedirInput("Nome da UC"); }
    public String pedirSiglaDocente()      { return pedirInput("Sigla do Docente Responsável"); }
    public String pedirNovaSiglaDocente()  { return pedirInput("Nova Sigla Docente"); }
    public String pedirNovoNome()          { return pedirInput("Novo Nome"); }
    public String pedirNovoAnoCurricular() { return pedirInput("Novo Ano Curricular"); }
    public String pedirNovaSiglaCurso()    { return pedirInput("Nova Sigla Curso"); }
    public String pedirNomeCurso()         { return pedirInput("Nome do Curso"); }
    public String pedirDepartamento()      { return pedirInput("Departamento (ex: DEIS)"); }
    public double pedirValorDouble(String msg) { return Consola.lerDouble(msg); }
    public int    pedirAnoHistorico()      { return Consola.lerInt("Ano Letivo a consultar (ex: 2025)"); }

    // ---------- REGISTO DEPARTAMENTO ----------

    public void   mostrarTituloRegistoDepartamento() {
        Consola.imprimirCabecalho("Registar Departamento");
        Consola.imprimirDicaFormulario();
    }
    public String pedirSiglaDepartamento() { return pedirInput("Sigla do Departamento (ex: DEIS)"); }
    public String pedirNomeDepartamento()  { return pedirInput("Nome do Departamento"); }
    public void   mostrarResumoRegistoDepartamento(String sigla, String nome) {
        Consola.imprimirSucesso("Departamento '" + nome + "' (" + sigla + ") registado com sucesso!");
        Consola.pausar();
    }

    // ---------- REGISTO DOCENTE ----------

    public void mostrarTituloRegistoDocente() {
        Consola.imprimirCabecalho("Registar Docente");
        Consola.imprimirDicaFormulario();
    }
    public void mostrarResumoRegistoDocente(String email, String sigla) {
        Consola.imprimirSucesso("Docente registado com sucesso!");
        Consola.imprimirInfo("Email institucional: " + email);
        Consola.imprimirInfo("Sigla atribuída:      " + sigla);
        Consola.pausar();
    }

    // ---------- REGISTO ESTUDANTE ----------

    // FIX (geração anterior): método em falta — causava erro no GestorController linha 440
    public void mostrarTituloRegistoEstudante() {
        Consola.imprimirCabecalho("Registar Estudante");
        Consola.imprimirDicaFormulario();
    }
    public void mostrarNumMecanograficoAtribuido(int num) {
        Consola.imprimirInfo("Nº Mecanográfico atribuído: " + num);
    }
    public void mostrarResumoRegistoEstudante(String email) {
        Consola.imprimirSucesso("Estudante registado! Email institucional: " + email);
        Consola.pausar();
    }

    // ---------- PASSWORD ----------

    public void mostrarCabecalhoAlterarPassword() {
        Consola.imprimirCabecalho("Alterar Password");
        Consola.imprimirDicaFormulario();
    }
    public String pedirNovaPassword() { return Consola.lerPassword("Nova Password"); }

    // ---------- ESTATÍSTICAS ----------

    public void mostrarCabecalhoMediaGlobal()  { Consola.imprimirTitulo("Média Global Institucional"); }
    public void mostrarMediaGlobal(double media, int total) {
        Consola.imprimirInfo(String.format("Média global: %.2f valores  (baseada em %d avaliações)", media, total));
        Consola.pausar();
    }
    public void mostrarSemNotasRegistadas()    { Consola.imprimirInfo("Sem notas registadas no sistema."); }

    public void mostrarCabecalhoMelhorAluno()  { Consola.imprimirTitulo("Melhor Aluno da Instituição"); }
    public void mostrarInfoMelhorAluno(String nome, int numMec, double media) {
        Consola.imprimirInfo(String.format("%-30s | Nº %d | Média: %.2f", nome, numMec, media));
        Consola.pausar();
    }
    public void mostrarSemAlunosAvaliados()    { Consola.imprimirInfo("Nenhum aluno com avaliações registadas."); }

    // ---------- DEVEDORES ----------

    public void mostrarCabecalhoDevedores()    { Consola.imprimirTitulo("Alunos Devedores de Propinas"); }
    public void mostrarEstudanteDevedor(int numMec, String nome, double divida) {
        System.out.printf("  [%d] %-30s | Dívida: %.2f€%n", numMec, nome, divida);
    }
    public void mostrarSemDevedores()          { Consola.imprimirInfo("Nenhum aluno devedor."); }

    // ---------- AVANÇAR ANO LETIVO ----------

    public void mostrarCabecalhoArranqueAnoLetivo() { Consola.imprimirCabecalho("Avançar Ano Letivo"); }
    public void mostrarVerificacaoQuorum()     { Consola.imprimirInfo("A verificar quórum mínimo por curso..."); }
    public void mostrarErroQuorum(String siglaCurso, int totalAlunos) {
        Consola.imprimirErro(String.format("Curso %-6s — quórum insuficiente (%d aluno(s), mínimo 5). Curso marcado como Inativo.", siglaCurso, totalAlunos));
    }
    public void mostrarSucessoQuorum(String siglaCurso) {
        Consola.imprimirInfo(String.format("Curso %-6s — quórum OK. Curso marcado como Ativo.", siglaCurso));
    }
    public void mostrarProcessamentoTransicoes() {
        Consola.imprimirInfo("A processar transições de ano dos estudantes...");
        Consola.imprimirLinha();
    }
    public void mostrarTransicaoSucedida(int numMec, int novoAno) {
        Consola.imprimirSucesso(String.format("[%d] Transitou para o %dº ano.", numMec, novoAno));
    }
    public void mostrarConclusaoCurso(int numMec) {
        Consola.imprimirSucesso(String.format("[%d] Curso concluído com sucesso!", numMec));
    }
    public void mostrarSucessoAvancoAno(int novoAno) {
        Consola.imprimirLinha();
        Consola.imprimirSucesso("Ano letivo avançado. Ano atual: " + novoAno);
        Consola.pausar();
    }

    public boolean perguntarVerListagem(String entidade) {
        return utils.Consola.lerSimNao("Deseja ver a listagem de " + entidade + " disponíveis?");
    }

    // ---------- MENSAGENS GENÉRICAS ----------

    public void mostrarMensagem(String msg)           { System.out.println("  " + msg); }
    public void mostrarErroCursoComAlocacoes()        { Consola.imprimirErro("Este curso tem estudantes ou docentes alocados e não pode ser alterado."); Consola.pausar(); }
    public void mostrarErroNomeInvalido()             { Consola.imprimirErro("Nome inválido (apenas letras)."); }
    public void mostrarErroNifInvalido()              { Consola.imprimirErro("NIF inválido (9 dígitos)."); }
    public void mostrarErroNifDuplicado()             { Consola.imprimirErro("Este NIF já se encontra registado no sistema."); }
    public void mostrarErroDataInvalida()             { Consola.imprimirErro("Formato de data inválido (DD-MM-AAAA)."); }
    public void mostrarErroCarregarDados(String ent)  { Consola.imprimirErro("Não foi possível carregar os dados de " + ent + "."); }
    public void mostrarErroNaoEncontrado(String ent)  { Consola.imprimirErro("Nenhuma " + ent + " encontrada."); }
    public void mostrarErroLimiteUcs(int ano)         { Consola.imprimirErro("Limite de 5 UCs para o " + ano + "º ano atingido."); }
    public void mostrarErroRemocao(String ent)        { Consola.imprimirErro("Não foi possível remover. UC associada a um Curso."); }
    public void mostrarSucessoCriacao(String ent)     { Consola.imprimirSucesso(ent + " criada com sucesso!"); Consola.pausar(); }
    public void mostrarSucessoAtualizacao(String ent) { Consola.imprimirSucesso(ent + " atualizada com sucesso!"); Consola.pausar(); }
    public void mostrarSucessoRemocao(String ent)     { Consola.imprimirSucesso(ent + " removida com sucesso!"); Consola.pausar(); }
    public void mostrarMensagemModoEdicao()           { Consola.imprimirInfo("Introduza os novos valores ('sair' para cancelar):"); }
    public void mostrarSucessoAlteracaoPassword()     { Consola.imprimirSucesso("Password alterada com sucesso!"); Consola.pausar(); }
    public void mostrarCancelamentoPassword()         { Consola.imprimirInfo("Operação cancelada."); }
    public void mostrarErroLeituraOpcao()             { Consola.imprimirErro("Erro de leitura. Tente novamente."); }
    public void mostrarOpcaoInvalida()                { Consola.imprimirErro("Opção inválida."); }
    public void mostrarDespedida()                    { Consola.imprimirInfo("Logout efetuado. Até breve!"); }
    public void mostrarOperacaoCancelada()            { Consola.imprimirInfo("Operação cancelada. A regressar ao menu..."); }
    public void mostrarSucessoAssociacaoRemovida()    { Consola.imprimirSucesso("Associação removida com sucesso."); Consola.pausar(); }
    public void mostrarErroAssociacaoRemovida()       { Consola.imprimirErro("Erro ao remover associação."); }

    // ---------- MENSAGENS DE ERRO (DATAS / NIF) ----------

    public void mostrarErroNifInvalidoOuDuplicado() {
        Consola.imprimirErro("NIF inválido ou já existente. Campo mantido.");
    }

    // FIX (geração anterior): método em falta — causava erro no GestorController linhas 147, 297, 381, 471
    public void mostrarErroDataInexistente() {
        Consola.imprimirErro("Data de nascimento inválida (ex: 31-06-2005). Formato correcto: DD-MM-AAAA.");
    }

    public void mostrarErroDataFutura() {
        Consola.imprimirErro("Data de nascimento não pode ser futura.");
    }

    public void mostrarErroIdadeForaLimites() {
        Consola.imprimirErro("Idade deve estar entre 16 e 120 anos.");
    }

    // ---------- MENSAGENS DE ERRO (UCs) ----------

    // FIX (geração anterior): método em falta — causava erro no GestorController linha 907
    public void mostrarErroUcNaoEncontrada() {
        Consola.imprimirErro("UC não encontrada.");
    }

    public void mostrarErroEditarUc() {
        Consola.imprimirErro("Erro ao editar UC.");
    }

    public void mostrarErroRemocaoUcComCursos(String siglaUc, List<String> cursos) {
        Consola.imprimirErro("Não é possível remover a UC " + siglaUc + " pois está associada aos cursos:");
        for (String curso : cursos) System.out.println("    - " + curso);
    }
    /**
     * Mostra uma mensagem de erro quando o avanço do ano letivo é bloqueado
     * devido a alunos com propinas em dívida.
     */
    public void mostrarErroAvancoBloqueadoPorDividas(List<Estudante> devedores) {
        Consola.imprimirErro("Não é possível avançar o ano letivo: existem alunos com propinas em dívida.");
        Consola.imprimirTitulo("Lista de Alunos Devedores");
        for (Estudante e : devedores) {
            System.out.printf("  %d - %s | Dívida: %.2f€%n",
                    e.getNumeroMecanografico(), e.getNome(), e.getSaldoDevedor());
        }
        Consola.imprimirLinha();
        Consola.pausar();
    }

    // ---------- CAMPOS OPCIONAIS DE EDIÇÃO ----------

    public String pedirNovaSiglaUc()        { return lerStringOpcional("Nova Sigla (Enter mantém a actual)"); }
    public String pedirNovoNomeUc()         { return lerStringOpcional("Novo Nome (Enter mantém a actual)"); }
    public String pedirNovaSiglaDocenteUc() { return lerStringOpcional("Nova Sigla do Docente (Enter mantém a actual)"); }
    public String pedirNovaMoradaEstudante(){ return lerStringOpcional("Nova Morada (Enter mantém a actual)"); }

    // ---------- INPUTS GENÉRICOS ----------

    /** Lê um campo de texto — "sair" lança CancelamentoException. */
    public String pedirInput(String msg)        { return Consola.lerString(msg); }
    public double pedirValorDouble2(String msg) { return Consola.lerDouble(msg); }

    // ---------- HELPER INTERNO ----------

    /**
     * Lê uma string opcional — Enter devolve ""; "sair" lança CancelamentoException.
     *
     * FIX: na geração anterior este método era private e chamava
     * Consola.lerStringOpcional() que NÃO EXISTE na classe Consola.
     * Agora é public e implementa a leitura diretamente com Scanner.
     */
    public String lerStringOpcional(String prompt) {
        System.out.print("  " + prompt + ": ");
        String input = new java.util.Scanner(System.in).nextLine().trim();
        if (input.equalsIgnoreCase("sair")) throw new CancelamentoException();
        return input; // pode ser vazia — o chamador trata o vazio como "manter"
    }
}
package view;

import model.Avaliacao;
import model.Docente;
import model.UnidadeCurricular;
import utils.Consola;

import java.util.List;

/**
 * Interface de utilizador do portal do Docente.
 * Apenas mostra informação e recolhe inputs — sem lógica de negócio.
 */
public class DocenteView {

    /**
     * Apresenta o menu principal do docente e lê a opção escolhida.
     *
     * @return Número da opção selecionada (0 a 3).
     */
    public int mostrarMenu() {
        Consola.imprimirCabecalho("Portal Docente — ISSMF");
        Consola.imprimirMenu(new String[]{
                "Consultar os Meus Alunos e Médias",
                "Lançar Nota Individual",
                "Lançar Nota em Lote (turma inteira)",
                "Alterar Password",
                "Ver Dados Pessoais",
                "Ver as Minhas Unidades Curriculares",
                "Consultar Histórico de um Aluno",
                "Definir Momentos de Avaliação"
        }, "Sair / Logout");
        return Consola.lerOpcaoMenu();
    }

    /**
     * Mostra a ficha completa do docente autenticado.
     */
    public void mostrarFichaDocente(Docente d) {
        Consola.imprimirTitulo("Dados Pessoais");
        Consola.imprimirInfo("Sigla:           " + d.getSigla());
        Consola.imprimirInfo("Nome:            " + d.getNome());
        Consola.imprimirInfo("Email:           " + d.getEmail());
        Consola.imprimirInfo("NIF:             " + d.getNif());
        Consola.imprimirInfo("Data Nascimento: " + d.getDataNascimento());
        Consola.imprimirInfo("Morada:          " + d.getMorada());
        Consola.pausar();
    }

    /**
     * Mostra a lista de UCs lecionadas pelo docente autenticado.
     */

    public void mostrarUcsDocente(Docente d) {
        Consola.imprimirTitulo("As Minhas Unidades Curriculares");
        if (d.getTotalUcsLecionadas() == 0) {
            Consola.imprimirInfo("Não tem unidades curriculares atribuídas.");
        } else {
            UnidadeCurricular[] ucs = d.getUcsLecionadas();
            for (int i = 0; i < d.getTotalUcsLecionadas(); i++) {
                if (ucs[i] != null) {
                    System.out.printf("  [%d] %-8s | %-35s | %dº Ano%n",
                            i + 1, ucs[i].getSigla(), ucs[i].getNome(), ucs[i].getAnoCurricular());
                }
            }
        }
        Consola.pausar();
    }

    // --- MÉTODOS DE LISTAGEM DE ALUNOS ---

    /** Exibe o cabeçalho da lista de alunos. */
    public void mostrarCabecalhoAlunos() {
        Consola.imprimirTitulo("Alunos e Médias");
    }

    public void mostrarLinha(String texto) { System.out.println("  " + texto); }


    // ---------- LANÇAMENTO DE NOTAS ----------

    /** Exibe o cabeçalho da secção de lançamento de notas. */
    public void mostrarCabecalhoLancamentoNotas() {
        Consola.imprimirCabecalho("Lançar Avaliações");
        Consola.imprimirDicaFormulario();
    }

    public int    pedirNumeroAluno()  { return Consola.lerInt("Nº Mecanográfico do Aluno"); }
    public String pedirSiglaUc()      { return Consola.lerString("Sigla da UC"); }
    public int    pedirAnoLetivo()    { return Consola.lerInt("Ano Letivo (ex: 2026)"); }
    public double pedirNotaNormal()   { return Consola.lerNota("Nota Normal"); }
    public double pedirNotaRecurso()  { return Consola.lerNota("Nota Recurso"); }
    public double pedirNotaEspecial() { return Consola.lerNota("Nota Especial"); }
    public double pedirNotaMomento()  { return Consola.lerNota("Nota do momento de avaliação (0 a 20)"); }

    /**
     * Mostra o número de momentos de avaliação configurados para a UC selecionada.
     * @param siglaUc     Sigla da UC.
     * @param numMomentos Número de momentos.
     */
    public void mostrarNumMomentosDaUC(String siglaUc, int numMomentos) {
        Consola.imprimirInfo("UC " + siglaUc.toUpperCase()
                + " tem " + numMomentos + " momento(s) de avaliação.");
    }

    /**
     * Pede a nota de um momento específico (ex.: "Momento 1 de 2").
     * @param momentoAtual Índice do momento atual (1-based).
     * @param totalMomentos Total de momentos da UC.
     * @return Nota introduzida.
     */
    public double pedirNotaPorMomento(int momentoAtual, int totalMomentos) {
        return Consola.lerNota("Nota do Momento " + momentoAtual + " de " + totalMomentos + " (0 a 20)");
    }

    /**
     * Mostra a nota final calculada (média dos momentos lançados).
     * @param notaFinal Valor da média.
     */
    public void mostrarNotaFinalCalculada(double notaFinal) {
        Consola.imprimirInfo(String.format("Nota final calculada (média): %.2f", notaFinal));
    }

    // ---------- PASSWORD ----------

    /**
     * Solicita a nova password ao utilizador, com ocultação de caracteres quando a consola o permite.
     * <p>
     * O cancelamento é feito premindo Enter sem introduzir texto, o que retorna uma string vazia.
     *
     * @return A nova password introduzida, ou uma string vazia se o utilizador premir Enter.
     */
    public String pedirNovaPassword() {
        Consola.imprimirTitulo("Alterar Password");
        Consola.imprimirDicaFormulario();
        return Consola.lerPassword("Nova Password");
    }

    // ---------- MENSAGENS ----------

    public void mostrarSucessoLancamento()        { Consola.imprimirSucesso("Avaliação registada com sucesso!"); }
    public void mostrarSucessoAlteracaoPassword() { Consola.imprimirSucesso("Password alterada com sucesso!"); }
    public void mostrarCancelamentoPassword()     { Consola.imprimirInfo("Operação cancelada."); }
    public void mostrarErroLeituraOpcao()         { Consola.imprimirErro("Erro de leitura. Tente novamente."); }
    public void mostrarOpcaoInvalida()            { Consola.imprimirErro("Opção inválida."); }
    public void mostrarDespedida()                { Consola.imprimirInfo("Logout efetuado. Até breve!"); }
    public void mostrarOperacaoCancelada()        { Consola.imprimirInfo("Operação cancelada. A regressar ao menu..."); }
    /**
     * Mostra o ID e Nome do aluno de forma simplificada para processos de seleção.
     * * @param numMec Número mecanográfico do aluno.
     * @param nome   Nome completo do aluno.
     */
    public void mostrarAlunoSimples(int numMec, String nome) {
        System.out.printf("  [%d] %s%n", numMec, nome);
    }

    public void mostrarAlunoComMedia(int numMec, String nome, double media, String ucs) {
        String mediaTexto = (media == 0.0) ? "n/a" : String.format("%.1f", media);

        System.out.printf("  [%d] %-25s | UCs: %-15s | Média: %s%n",
                numMec, nome, ucs, mediaTexto);
    }

    public void mostrarErroCarregarAlunos() { Consola.imprimirErro("Não foi possível carregar a lista de alunos.");
    }

    public void mostrarCabecalhoLancamentoNotasLote() {
        Consola.imprimirCabecalho("Lançar Nota em Lote");
        Consola.imprimirDicaFormulario();
    }
    public void mostrarListaAlunosParaLote(String siglaUc, List<String> alunosFormatados) {
        Consola.imprimirTitulo("Alunos inscritos em " + siglaUc);
        for (String linha : alunosFormatados) {
            System.out.println("  " + linha);
        }
        Consola.imprimirLinha();
    }

    public void mostrarResultadoLote(String relatorio) {
        Consola.imprimirSucesso("Lançamento concluído");
        System.out.println(relatorio);
        Consola.pausar();
    }

    public void mostrarErro(String msg) {
        Consola.imprimirErro(msg);
    }

    public void mostrarPedidoNotaParaAluno(int numMec, String nome) {
        Consola.imprimirTitulo("Lançar nota para " + nome + " (" + numMec + ")");
        Consola.imprimirDicaFormulario();
    }
    public void mostrarCabecalhoDefinirMomentos() {
        Consola.imprimirCabecalho("Definir Momentos de Avaliação");
        Consola.imprimirDicaFormulario();
    }

    public void mostrarUcsParaDefinicao(Docente docente) {
        Consola.imprimirTitulo("Suas Unidades Curriculares");
        if (docente.getTotalUcsLecionadas() == 0) {
            Consola.imprimirInfo("Não leciona nenhuma UC.");
            return;
        }
        UnidadeCurricular[] ucs = docente.getUcsLecionadas();
        for (int i = 0; i < docente.getTotalUcsLecionadas(); i++) {
            System.out.printf("  [%d] %s - %s%n", i + 1, ucs[i].getSigla(), ucs[i].getNome());
        }
        Consola.imprimirLinha();
    }

    public int pedirNumeroMomentos() {
        return Consola.lerInt("Número de momentos (1 a 3)");
    }

    public void mostrarSucessoMomentos(String siglaUc, int momentos) {
        Consola.imprimirSucesso(String.format("UC %s: momentos definidos para %d.", siglaUc, momentos));
        Consola.pausar();
    }

    /**
     * Alerta mostrado ao docente no login quando o ano está em PLANEAMENTO
     * e existem UCs suas sem momentos de avaliação definidos.
     */
    public void mostrarAlertaMomentosPendentes(java.util.List<String> ucs) {
        Consola.imprimirLinha();
        System.out.println("  ⚠  ATENÇÃO — Momentos de avaliação por definir:");
        for (String uc : ucs) {
            System.out.println("     ► " + uc);
        }
        System.out.println("  Aceda à opção 8 — Definir Momentos de Avaliação.");
        Consola.imprimirLinha();
        Consola.pausar();
    }

    /** Mostra o valor atual de momentos antes de pedir o novo. */
    public void mostrarMomentosAtuais(String siglaUc, int momentosAtuais) {
        if (momentosAtuais == 0) {
            Consola.imprimirInfo("UC " + siglaUc + ": momentos ainda não definidos.");
        } else {
            Consola.imprimirInfo("UC " + siglaUc + ": momentos atuais = " + momentosAtuais + ".");
        }
    }
}


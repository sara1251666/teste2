package view;

import model.Avaliacao;
import model.Estudante;
import model.Pagamento;
import utils.Consola;

import java.util.List;

/**
 * Interface de utilizador do portal do Estudante.
 * Responsável exclusivamente por mostrar informação e recolher inputs.
 * Não contém lógica de negócio.
 */
public class EstudanteView {

    public EstudanteView() {
    }

    /**
     * Apresenta o menu principal do estudante e lê a opção escolhida.
     *
     * @return Número da opção selecionada (0 a 4).
     */
    public int mostrarMenuPrincipal() {
        Consola.imprimirCabecalho("Portal Estudante — ISSMF");
        Consola.imprimirMenu(new String[]{
                "Ver Dados Pessoais e Avaliações",
                "Atualizar Morada",
                "Alterar Password",
                "Consultar Dados Financeiros / Pagar",
                "Ver UCs em que estou inscrito",
                "Ver minhas notas por UC",
                "Consultar Histórico Académico"
        }, "Sair / Logout");
        return Consola.lerOpcaoMenu();
    }

    public void mostrarDadosPessoais(Estudante e) {
        Consola.imprimirCabecalho("Dados Pessoais");
        Consola.imprimirInfo("Nº Mecanográfico:  " + e.getNumeroMecanografico());
        Consola.imprimirInfo("Nome:              " + e.getNome());
        Consola.imprimirInfo("Email:             " + e.getEmail());
        Consola.imprimirInfo("NIF:               " + e.getNif());
        Consola.imprimirInfo("Data Nascimento:   " + e.getDataNascimento());
        Consola.imprimirInfo("Morada:            " + e.getMorada());
        Consola.imprimirInfo("Curso:             " + e.getSiglaCurso());
        Consola.imprimirInfo("Ano Curricular:    " + e.getAnoCurricular() + "º Ano");

        Consola.imprimirTitulo("Avaliações");
        int total = e.getPercurso().getTotalAvaliacoes();
        if (total == 0) {
            Consola.imprimirInfo("Sem avaliações registadas.");
        } else {
            Avaliacao[] historico = e.getPercurso().getHistoricoAvaliacoes();
            Consola.imprimirLinha();
            System.out.printf("  %-8s | %-28s | Ano  | %-18s | Estado%n",
                    "UC", "Nome", "Notas");
            Consola.imprimirLinha();
            for (int i = 0; i < total; i++) {
                Avaliacao av = historico[i];
                if (av == null || av.getUc() == null) continue;
                double[] notas = av.getResultados();
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < av.getTotalAvaliacoesLancadas(); j++) {
                    if (j > 0) sb.append(" | ");
                    sb.append(String.format("%.1f", notas[j]));
                }
                System.out.printf("  %-8s | %-28s | %-4d | %-18s | %s%n",
                        av.getUc().getSigla(), av.getUc().getNome(),
                        av.getAnoLetivo(), sb,
                        av.isAprovado() ? "APROVADO" : "REPROVADO");
            }
            Consola.imprimirLinha();
        }
        Consola.pausar();
    }

    public void mostrarDadosFinanceiros(Estudante e) {
        Consola.imprimirCabecalho("Dados Financeiros");
        System.out.printf("  Saldo devedor:  %.2f€%n", e.getSaldoDevedor());
        Consola.imprimirTitulo("Histórico de Pagamentos");

        int total = e.getTotalPagamentos();
        if (total == 0) {
            Consola.imprimirInfo("Sem pagamentos registados.");
        } else {
            List<Pagamento> hist = e.getHistoricoPagamentos();

            for (Pagamento p : hist) {
                System.out.println("  " + p);
            }
        }
        Consola.imprimirLinha();
    }

    public int pedirTipoPagamento(double divida) {
        Consola.imprimirMenu(new String[]{
                String.format("Pagamento Total   (%.2f€)", divida),
                "Pagamento Parcial"
        }, "Cancelar");
        return Consola.lerOpcaoMenu();
    }

    public double pedirValorPagamentoParcial(double max) {
        return Consola.lerDouble(String.format("Valor a pagar (máx %.2f€)", max));
    }


    // ---------- ATUALIZAR DADOS ----------

    public String pedirNovaMorada() {
        Consola.imprimirTitulo("Atualizar Morada");
        Consola.imprimirDicaFormulario();
        return Consola.lerStringOpcional("Nova Morada (Enter mantém a atual)");
    }

    public String pedirNovaPassword() {
        Consola.imprimirTitulo("Alterar Password");
        Consola.imprimirDicaFormulario();
        return Consola.lerPassword("Nova Password");
    }

    public void mostrarInscricoes(String info) {
        Consola.imprimirTitulo("Minhas Inscrições");
        System.out.println(info);
        Consola.pausar();
    }

    // ---------- MENSAGENS ----------

    public void mostrarSucessoAtualizacaoMorada()   { Consola.imprimirSucesso("Morada atualizada com sucesso!"); }
    public void mostrarSemAlteracaoMorada()          { Consola.imprimirInfo("Sem alterações efetuadas."); }
    public void mostrarSucessoAtualizacaoPassword()  { Consola.imprimirSucesso("Password alterada com sucesso!"); }
    public void mostrarCancelamentoPassword()        { Consola.imprimirInfo("Operação cancelada."); }
    public void mostrarSucessoPagamento(double pago, double resto) {
        Consola.imprimirSucesso(String.format("Pagamento de %.2f€ registado. Saldo restante: %.2f€", pago, resto));
    }
    public void mostrarSemPagamentosPendentes() { Consola.imprimirInfo("Não tem pagamentos pendentes."); }
    public void mostrarErroValorInvalido()       { Consola.imprimirErro("Valor inválido ou superior à dívida."); }
    public void mostrarErroLeitura()             { Consola.imprimirErro("Erro de leitura. Tente novamente."); }
    public void mostrarOpcaoInvalida()           { Consola.imprimirErro("Opção inválida."); }
    public void mostrarDespedida()               { Consola.imprimirInfo("Logout efetuado. Até breve!"); }
    public void mostrarOperacaoCancelada()       { Consola.imprimirInfo("Operação cancelada. A regressar ao menu..."); }
}
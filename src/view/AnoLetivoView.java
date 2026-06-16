package view;

import model.AnoLetivo;
import model.Curso;
import utils.Consola;

import java.util.List;

/**
 * View do módulo Ano Letivo.
 * Sprint 6: removidas opções Editar e Eliminar (ano letivo é imutável após criação).
 * Adicionados métodos para relatório de aptidão, histórico e momentos de avaliação.
 */
public class AnoLetivoView {

    // ---------- MENU ----------

    public int mostrarMenu() {
        Consola.imprimirCabecalho("Gestão de Ano Letivo");
        Consola.imprimirMenu(new String[]{
                "Criar Ano Letivo",
                "Iniciar Ano Letivo",
                "Fechar Ano Letivo",
                "Avançar Ano Letivo",
                "Listar Anos Letivos",
                "Estado do Ano Atual",
                "Histórico de Anos Fechados",
                "Alterar Momentos de Avaliação de UC"
        }, "Voltar");
        return Consola.lerOpcaoMenu();
    }

    // ---------- INPUTS ----------

    public int pedirAno(String mensagem) {
        return Consola.lerInt(mensagem);
    }

    /**
     * Mostra a lista de anos existentes com estado e pede ao gestor para selecionar um.
     * @return O ano letivo selecionado, ou null se a lista estiver vazia.
     */
    public AnoLetivo pedirSelecaoAno(List<AnoLetivo> anos) {
        Consola.imprimirTitulo("Anos Letivos Disponíveis");
        if (anos == null || anos.isEmpty()) {
            Consola.imprimirInfo("Não existem anos letivos em PLANEAMENTO para iniciar.");
            Consola.pausar();
            return null;
        }
        System.out.printf("  %-4s  %-8s  %s%n", "Nº", "Ano", "Estado");
        Consola.imprimirLinha();
        for (int i = 0; i < anos.size(); i++) {
            AnoLetivo a = anos.get(i);
            System.out.printf("  [%d]  %-8d  %s%n", i + 1, a.getAno(), a.getEstado());
        }
        Consola.imprimirLinha();
        while (true) {
            try {
                int op = Consola.lerInt("Selecione o número do ano");
                if (op >= 1 && op <= anos.size()) return anos.get(op - 1);
                Consola.imprimirErro("Opção fora do intervalo (1-" + anos.size() + ").");
            } catch (utils.CancelamentoException e) {
                throw e;
            }
        }
    }

    public String pedirSiglaUc(String mensagem) {
        return Consola.lerString(mensagem).toUpperCase().trim();
    }

    public int pedirNumeroMomentos() {
        return Consola.lerInt("Número de momentos de avaliação (1, 2 ou 3)");
    }

    // ---------- LISTAGENS DE ANOS ----------

    public void mostrarListaAnos(List<AnoLetivo> anos) {
        Consola.imprimirTitulo("Anos Letivos Registados");
        if (anos == null || anos.isEmpty()) {
            Consola.imprimirInfo("Nenhum ano letivo registado.");
        } else {
            System.out.printf("  %-8s  %s%n", "Ano", "Estado");
            Consola.imprimirLinha();
            for (AnoLetivo a : anos) {
                System.out.printf("  %-8d  %s%n", a.getAno(), a.getEstado());
            }
        }
        Consola.imprimirLinha();
        Consola.pausar();
    }

    public void mostrarResumo(List<String> linhas) {
        Consola.imprimirTitulo("Estado do Ano Letivo");
        if (linhas == null || linhas.isEmpty()) {
            Consola.imprimirInfo("Sem informação disponível.");
        } else {
            for (String l : linhas) Consola.imprimirInfo(l);
        }
        Consola.imprimirLinha();
        Consola.pausar();
    }

    // ---------- RELATÓRIO DE APTIDÃO (Iniciar Ano) ----------

    /**
     * Mostra o cabeçalho do relatório de aptidão antes de iniciar o ano.
     * @param anos Lista de todos os anos com estado, para contexto.
     */
    public void mostrarCabecalhoRelatorioAptidao(List<AnoLetivo> anos) {
        Consola.imprimirCabecalho("Iniciar Ano Letivo — Relatório de Aptidão");
        Consola.imprimirTitulo("Anos Letivos Existentes");
        if (anos != null) {
            for (AnoLetivo a : anos) {
                System.out.printf("  %-8d  %s%n", a.getAno(), a.getEstado());
            }
        }
        Consola.imprimirLinha();
    }

    /**
     * Mostra o resultado da verificação de aptidão por curso.
     * @param sigla      Sigla do curso.
     * @param apto       true se o curso está pronto para arrancar.
     * @param motivo     Descrição do motivo de bloqueio (se não apto).
     */
    public void mostrarAptidaoCurso(String sigla, boolean apto, String motivo) {
        if (apto) {
            Consola.imprimirSucesso(String.format("  Curso %-8s — APTO", sigla));
        } else {
            Consola.imprimirErro(String.format("  Curso %-8s — NÃO APTO: %s", sigla, motivo));
        }
    }

    /**
     * Pergunta ao gestor o que fazer com um curso não apto.
     * @param siglaCurso Sigla do curso em causa.
     * @return true se o gestor escolhe Inativar e continuar; false para Cancelar arranque.
     */
    public boolean perguntarInativarOuCancelar(String siglaCurso) {
        Consola.imprimirTitulo("Curso " + siglaCurso + " não está apto");
        Consola.imprimirMenu(new String[]{
                "Inativar curso " + siglaCurso + " e continuar arranque",
        }, "Cancelar arranque do ano letivo");
        int op = Consola.lerOpcaoMenu();
        return op == 1;
    }

    public void mostrarCursoInativado(String sigla) {
        Consola.imprimirInfo("Curso " + sigla + " marcado como Inativo — excluído do arranque.");
    }

    // ---------- FECHO ----------

    /**
     * Mostra as pendências que bloqueiam o fecho do ano letivo.
     * @param pendenciasNotas     Lista de alunos sem notas lançadas.
     * @param pendenciasPropinas  Lista de alunos com propinas em dívida.
     */
    public void mostrarPendenciasFecho(List<String> pendenciasNotas, List<String> pendenciasPropinas) {
        Consola.imprimirCabecalho("Fecho de Ano Letivo — Pendências");
        if (!pendenciasNotas.isEmpty()) {
            Consola.imprimirTitulo("Notas por lançar");
            for (String p : pendenciasNotas) Consola.imprimirErro("  " + p);
        }
        if (!pendenciasPropinas.isEmpty()) {
            Consola.imprimirTitulo("Propinas em dívida");
            for (String p : pendenciasPropinas) Consola.imprimirErro("  " + p);
        }
        Consola.imprimirLinha();
        Consola.pausar();
    }

    // ---------- HISTÓRICO ----------

    public void mostrarHistoricoAnos(List<String> linhas) {
        Consola.imprimirCabecalho("Histórico de Anos Letivos Fechados");
        if (linhas == null || linhas.isEmpty()) {
            Consola.imprimirInfo("Nenhum ano letivo fechado no histórico.");
        } else {
            for (String l : linhas) System.out.println("  " + l);
        }
        Consola.imprimirLinha();
        Consola.pausar();
    }

    // ---------- MOMENTOS DE AVALIAÇÃO ----------

    public void mostrarMomentosUc(String siglaUc, int momentosAtuais) {
        System.out.printf("  UC %-8s — momentos de avaliação atuais: %d%n", siglaUc, momentosAtuais);
    }

    public void mostrarSucessoMomentos(String siglaUc, int novos) {
        Consola.imprimirSucesso("UC " + siglaUc + " — momentos de avaliação atualizados para " + novos + ".");
        Consola.pausar();
    }

    /**
     * Mostra a lista de UCs que não têm momentos de avaliação definidos.
     */
    public void mostrarPendenciasMomentosUc(List<String> pendentes) {
        Consola.imprimirTitulo("UCs sem momentos de avaliação definidos");
        for (String p : pendentes) {
            System.out.println("  - " + p);
        }
        Consola.imprimirLinha();
    }

    // ---------- MENSAGENS ----------

    public void mostrarSucesso(String msg)  { Consola.imprimirSucesso(msg); Consola.pausar(); }
    public void mostrarErro(String msg)     { Consola.imprimirErro(msg);    Consola.pausar(); }
    public void mostrarMensagem(String msg) { Consola.imprimirInfo(msg);    Consola.pausar(); }
}

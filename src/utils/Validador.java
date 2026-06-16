package utils;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Centraliza as regras de validação de dados introduzidos pelo utilizador.
 * Todos os métodos são estáticos e devolvem apenas um booleano.
 */
public class Validador {

    private Validador() {}

    /**
     * Valida o formato de um NIF português.
     * Aceita NIFs com 9 dígitos cujo primeiro algarismo seja 1, 2, 3, 5, 6, 7, 8 ou 9.
     * @param nif NIF a validar.
     * @return true se o formato for válido.
     */
    public static boolean validarNif(String nif) {
        return nif != null && nif.matches("[12356789]\\d{8}");
    }

    /**
     * Verifica se um email pertence a um domínio institucional reconhecido.
     * Domínios aceites: @issmf.ipp.pt, @isep.ipp.pt e admin@issmf.pt.
     * @param email Endereço a verificar.
     * @return true se o email pertencer a um domínio válido.
     */
    public static boolean isEmailInstitucionalValido(String email) {
        if (email == null || email.trim().isEmpty()) return false;

        String e = email.trim().toLowerCase();

        if (!e.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            return false;
        }

        if (e.equals("admin@issmf.pt")) return true;

        return e.endsWith("@issmf.ipp.pt") || e.endsWith("@isep.ipp.pt");
    }

    /**
     * Valida o email introduzido no formulário de login.
     * Requer estritamente o domínio @issmf.ipp.pt.
     * @param email Email introduzido.
     * @return true se o email terminar com @issmf.ipp.pt.
     */
    public static boolean validarSufixoLogin(String email) {
        if (email == null || email.trim().isEmpty()) return false;

        String e = email.trim().toLowerCase();

        return e.endsWith("@issmf.ipp.pt") && e.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }

    /**
     * Verifica se um nome é composto apenas por letras e espaços.
     * @param nome Nome a validar.
     * @return true se o nome contiver apenas letras e espaços.
     */
    public static boolean isNomeValido(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return false;
        }

        return nome.matches("^[a-zA-ZÀ-ÿ\\s]+$");
    }

    /**
     * Valida o formato e a existência calendárica de uma data de nascimento.
     * Formato aceite: DD-MM-AAAA. A data tem de ser anterior à data atual.
     * @param data Data a validar.
     * @return true se a data for válida e anterior a hoje.
     */
    public static boolean isDataNascimentoValida(String data) {
        if (data == null || !data.matches("^(0[1-9]|[12][0-9]|3[01])-(0[1-9]|1[0-2])-[0-9]{4}$")) {
            return false;
        }

        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDate dataNasc = LocalDate.parse(data, dtf);
            LocalDate hoje = LocalDate.now();

            return dataNasc.isBefore(hoje);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Valida a data de nascimento e retorna um código indicando o tipo de erro.
     * @param dataNascimento Data no formato "dd-MM-yyyy"
     * @return 0 se válida, 1 se formato inválido ou data inexistente (ex: 31-06-2005),
     *         2 se data futura, 3 se idade fora do intervalo (16-120 anos).
     */
    public static int validarDataNascimentoDetalhado(String dataNascimento) {
        if (!isDataReal(dataNascimento)) {
            return 1; // formato inválido ou data inexistente
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate nascimento = LocalDate.parse(dataNascimento, formatter);
        LocalDate hoje = LocalDate.now();

        if (nascimento.isAfter(hoje)) {
            return 2; // data futura
        }

        int idade = Period.between(nascimento, hoje).getYears();
        if (idade < 16 || idade > 120) {
            return 3; // idade fora dos limites
        }
        return 0; // válida
    }

    private static final DateTimeFormatter DATE_FORMATTER_STRICT =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);

    public static boolean isDataReal(String data) {
        if (data == null) return false;
        try {
            LocalDate.parse(data, DATE_FORMATTER_STRICT);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

}
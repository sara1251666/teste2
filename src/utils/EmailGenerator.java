package utils;

/**
 * Gera endereços de email institucionais para estudantes e docentes.
 * Todos os endereços utilizam o domínio @issmf.ipp.pt.
 */
public class EmailGenerator {

    /**
     * Gera o email de um estudante a partir do número mecanográfico.
     * @param numeroMecanografico Número mecanográfico único do estudante.
     * @return Endereço no formato numMec@issmf.ipp.pt.
     */
    public static String gerarEmailEstudante(int numeroMecanografico) {
        return numeroMecanografico + "@issmf.ipp.pt";
    }


    /**
     * Gera o email de um docente a partir da sua sigla.
     * @param sigla Sigla de 3 caracteres do docente.
     * @return Endereço no formato sigla@issmf.ipp.pt em minúsculas.
     */
    public static String gerarEmailDocente(String sigla) {
        return sigla.toLowerCase() + "@issmf.ipp.pt";
    }
}


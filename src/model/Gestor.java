package model;

/**
 * Representa o gestor de backoffice do sistema.
 * Herda todos os dados pessoais e credenciais de Utilizador.
 */
public class Gestor extends Utilizador {

    /**
     * Cria um gestor com todos os dados.
     * @param email          Email institucional.
     * @param password       Hash PBKDF2 da palavra-chave.
     * @param nome           Nome completo.
     * @param nif            NIF com 9 dígitos.
     * @param morada         Morada de residência.
     * @param dataNascimento Data de nascimento (DD-MM-AAAA).
     */
    public Gestor(String email, String password, String nome,
                  String nif, String morada, String dataNascimento) {
        super(email, password, nome, nif, morada, dataNascimento);
    }
}
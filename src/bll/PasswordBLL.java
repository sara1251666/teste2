package bll;

import controller.LoginController;
import utils.PasswordGenerator;
import utils.EmailService;

/**
 * Lógica de negócio para a gestão de palavras-passe.
 * Gera uma nova password segura, delega a persistência no LoginController
 * e envia a nova password por email ao utilizador.
 */
public class PasswordBLL {

    private final LoginController loginController = new LoginController();

    public void recuperarPassword(String email) {
        String novaPassLimpa = PasswordGenerator.gerarPasswordSegura();
        loginController.atualizarPassword(email, novaPassLimpa);
        EmailService.enviarRecuperacaoPassword("Utilizador", email, novaPassLimpa);
    }
}
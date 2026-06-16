package utils;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Serviço de envio de emails do sistema via SMTP (Gmail TLS 587).
 * Todo o envio é feito numa Thread separada, sem bloquear a consola.
 * As credenciais SMTP são lidas do ficheiro email.properties na raiz do projeto.
 * Formato de email.properties:
 *   mail.smtp.user=issmfsistema@gmail.com
 *   mail.smtp.password=a_tua_app_password_aqui
 *   email.equipa.1=1251666@isep.ipp.pt
 *   email.equipa.2=1251943@isep.ipp.pt
 *   email.equipa.3=1220492@isep.ipp.pt
 *   email.equipa.4=1251663@isep.ipp.pt
 */
public class EmailService {

    private static final String   EMAIL_SISTEMA;
    private static final String   APP_PASSWORD;
    private static final String[] EMAILS_EQUIPA;


    /*
     * Inicializa as constantes de configuracao SMTP a partir do ficheiro email.properties.
     * Se o ficheiro nao existir, sao usados os valores predefinidos no codigo.
     * Erros de leitura sao ignorados silenciosamente - o sistema continua sem enviar emails.
     *
     * Constantes inicializadas:
     *   EMAIL_SISTEMA - endereco remetente do sistema.
     *   APP_PASSWORD  - password da conta SMTP (App Password do Gmail).
     *   EMAILS_EQUIPA - enderecos da equipa que recebe copia de cada email enviado.
     */
    static {
        Properties config = new Properties();
        File configFile = new File("email.properties");

        String   emailSistema = "issmfsistema@gmail.com";
        String   appPassword  = "";
        String[] emailsEquipa = {
                "1251666@isep.ipp.pt",
                "1251943@isep.ipp.pt",
                "1220492@isep.ipp.pt",
                "1251663@isep.ipp.pt",
                ""
        };

        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                config.load(fis);
                emailSistema = config.getProperty("mail.smtp.user",     emailSistema);
                appPassword  = config.getProperty("mail.smtp.password", appPassword);

                for (int i = 1; i <= 5; i++) {
                    String key = "email.equipa." + i;
                    if (config.containsKey(key))
                        emailsEquipa[i - 1] = config.getProperty(key);
                }
            } catch (IOException e) {
            }
        }

        EMAIL_SISTEMA = emailSistema;
        APP_PASSWORD  = appPassword;
        EMAILS_EQUIPA = emailsEquipa;
    }

    private EmailService() {}

    /**
     * Envia as credenciais de acesso ao novo utilizador e em cópia à equipa.
     * Não produz nenhuma saída na consola.
     * @param nomeUtilizador  Nome do utilizador para saudação no email.
     * @param emailUtilizador Endereço de destino principal.
     * @param passLimpa       Password em texto limpo a incluir no email.
     */
    public static void enviarCredenciaisTodos(String nomeUtilizador,
                                              String emailUtilizador,
                                              String passLimpa) {
        if (nomeUtilizador == null || emailUtilizador == null || passLimpa == null) return;
        if (APP_PASSWORD.isEmpty()) return;

        String assunto = "[ISSMF] As suas credenciais de acesso — " + nomeUtilizador;
        String corpo   = construirCorpo(nomeUtilizador, emailUtilizador, passLimpa);

        new Thread(() -> {
            enviarUmEmail(emailUtilizador, assunto, corpo);
            for (String backup : EMAILS_EQUIPA)
                if (backup != null && !backup.isEmpty())
                    enviarUmEmail(backup, assunto, corpo);
            enviarUmEmail(EMAIL_SISTEMA, "[ARQUIVO] " + assunto, corpo);
        }, "email-sender").start();
    }

    /**
     * Envia uma nova password temporária para recuperação de conta.
     * Executa em background sem saída na consola.
     * @param nomeUtilizador  Nome do utilizador.
     * @param emailUtilizador Endereço de destino.
     * @param novaPassLimpa   Nova password gerada em texto limpo.
     */
    public static void enviarRecuperacaoPassword(String nomeUtilizador,
                                                 String emailUtilizador,
                                                 String novaPassLimpa) {
        if (nomeUtilizador == null || emailUtilizador == null || novaPassLimpa == null) return;
        if (APP_PASSWORD.isEmpty()) return;

        String assunto = "[ISSMF] Recuperação de password — " + emailUtilizador;
        String corpo   = "Caro(a) " + nomeUtilizador + ",\n\n"
                + "Foi solicitada a recuperação da sua conta no sistema ISSMF.\n\n"
                + "  E-mail:   " + emailUtilizador + "\n"
                + "  Password: " + novaPassLimpa   + "\n\n"
                + "Por favor altere a password no próximo acesso.\n\n"
                + "Mensagem gerada automaticamente — não responda.\n"
                + "— Sistema ISSMF";

        new Thread(() -> {
            enviarUmEmail(emailUtilizador, assunto, corpo);
            for (String backup : EMAILS_EQUIPA)
                if (backup != null && !backup.isEmpty())
                    enviarUmEmail(backup, assunto, corpo);
            enviarUmEmail(EMAIL_SISTEMA, "[ARQUIVO] " + assunto, corpo);
        }, "email-sender").start();
    }

    /**
     * Envia um email para um único destinatário.
     * Erros de envio são ignorados silenciosamente para não interromper o fluxo.
     * @param destinatario Endereço de email do destinatário.
     * @param assunto      Assunto do email.
     * @param corpo        Corpo do email em texto simples.
     */
    private static void enviarUmEmail(String destinatario, String assunto, String corpo) {
        try {
            Message msg = criarMensagem(destinatario);
            msg.setSubject(assunto);
            msg.setText(corpo);
            Transport.send(msg);
        } catch (MessagingException e) {
        }
    }

    /**
     * Constrói o corpo do email de boas-vindas com as credenciais do utilizador.
     * @param nome  Nome do utilizador.
     * @param email Email institucional atribuído.
     * @param pass  Password em texto limpo.
     * @return Corpo do email formatado.
     */
    private static String construirCorpo(String nome, String email, String pass) {
        return "Caro(a) " + nome + ",\n\n"
                + "A sua conta no sistema ISSMF foi criada com sucesso.\n\n"
                + "  E-mail:   " + email + "\n"
                + "  Password: " + pass  + "\n\n"
                + "Por favor altere a password no primeiro acesso.\n\n"
                + "Mensagem gerada automaticamente — não responda.\n"
                + "— Sistema ISSMF";
    }

    /**
     * Cria e configura uma mensagem SMTP pronta a enviar.
     * @param destinatario Endereço de email do destinatário.
     * @return Mensagem configurada com remetente e destinatário.
     * @throws MessagingException Se ocorrer erro na configuração SMTP.
     */
    private static Message criarMensagem(String destinatario) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            "smtp.gmail.com");
        props.put("mail.smtp.port",            "587");
        props.put("mail.smtp.ssl.protocols",   "TLSv1.2");

        final String password = APP_PASSWORD.replace(" ", "");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_SISTEMA, password);
            }
        });

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(EMAIL_SISTEMA));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
        return msg;
    }
}
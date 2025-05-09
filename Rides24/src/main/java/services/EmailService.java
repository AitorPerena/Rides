package services;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailService {

    public static void sendWelcomeEmail(String recipientEmail) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.ehu.es"); 

            Session session = Session.getInstance(props);

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(recipientEmail)); 
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Bienvenido/a a nuestra aplicación de viajes");

            String body = String.format(
                "Hola %s,\n\nGracias por registrarte en nuestra aplicación. ¡Esperamos que disfrutes del servicio!\n\nSaludos,\nEl equipo de viajes."
            );
            message.setText(body);

            Transport.send(message);

            System.out.println("Correo enviado a: " + recipientEmail);
        } catch (MessagingException e) {
            System.err.println("Error al enviar el correo: " + e.getMessage());
        }
    }
}

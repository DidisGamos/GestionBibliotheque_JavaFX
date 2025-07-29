package com.biblio.gestionbiblio;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;

public class SendMailController {
    public static void main(String[] args) {
        String host = "smtp.gmail.com";
        String port = "465";
        String user = "herllandysamoroschristy@gmail.com";
        String password = "wric trsn rxmp ixph";
        String to = "didisgamos@gmail.com";

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");  // Add this line
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.port", port);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("Test Email");
            message.setText("This is a test email from Java.");

            Transport.send(message);
            System.out.println("Email sent successfully!");
        } catch (MessagingException e) {
            System.out.println("Failed to send email:");
            e.printStackTrace();
        }
    }
}

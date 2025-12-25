package com.example.expensetracker.utils;

import java.util.Properties;
import java.util.Random;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailUtil {

    // Génère un code aléatoire de 6 chiffres
    public static String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    public static void sendEmail(String recipientEmail, String code) throws Exception {
        // --- CONFIGURATION ---
        final String senderEmail = "souhail.moustaghit.ensao@ump.ac.ma";
        final String senderPassword = "hkwu ajhf utmx khwy"; // VOTRE MOT DE PASSE D'APPLICATION (16 lettres)

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Session
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        // Message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject("Code de vérification - Expense Tracker");
        message.setText("Bonjour,\n\nVotre code de vérification est : " + code + "\n\nMerci de l'entrer dans l'application.");

        // Envoi
        Transport.send(message);
    }
}

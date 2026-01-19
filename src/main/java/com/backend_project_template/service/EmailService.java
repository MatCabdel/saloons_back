package com.backend_project_template.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail-from}")
    private String mailFrom;

    @Value("${app.admin-email}")
    private String adminEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmailToAdmin(String subject, String htmlContent, String replyToEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailFrom);
            helper.setTo(adminEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            if (replyToEmail != null && !replyToEmail.isBlank()) {
                helper.setReplyTo(replyToEmail);
            }

            mailSender.send(message);
            logger.info("Email envoyé avec succès à {}", adminEmail);
        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'email: {}", e.getMessage(), e);
            throw new RuntimeException("Impossible d'envoyer l'email. Veuillez réessayer plus tard.");
        }
    }

    public String getAdminEmail() {
        return adminEmail;
    }
}

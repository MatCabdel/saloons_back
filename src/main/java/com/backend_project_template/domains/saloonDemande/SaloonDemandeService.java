package com.backend_project_template.domains.saloonDemande;

import com.backend_project_template.domains.user.User;
import com.backend_project_template.service.EmailService;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SaloonDemandeService {

    private static final Logger logger = LoggerFactory.getLogger(SaloonDemandeService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

    private final SaloonDemandeRepository repository;
    private final EmailService emailService;

    public SaloonDemandeService(SaloonDemandeRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }

    public SaloonDemande createDemande(SaloonDemandeDTO dto, User user) {
        SaloonDemande demande = new SaloonDemande();
        demande.setPlaceName(dto.getPlaceName());
        demande.setPlaceType(dto.getPlaceType());
        demande.setAddress(dto.getAddress());
        demande.setComment(dto.getComment());
        demande.setUser(user);

        SaloonDemande saved = repository.save(demande);
        logger.info("Nouvelle demande de saloon créée: {} (ID: {})", saved.getPlaceName(), saved.getId());

        sendNotificationEmail(saved, user);

        return saved;
    }

    private void sendNotificationEmail(SaloonDemande demande, User user) {
        String subject = String.format("[SALOONS] Saloon à la demande - %s - %s",
                demande.getPlaceType().getLabel(),
                demande.getPlaceName());

        String htmlContent = buildEmailContent(demande, user);
        String replyTo = (user != null) ? user.getEmail() : null;

        emailService.sendEmailToAdmin(subject, htmlContent, replyTo);
    }

    private String buildEmailContent(SaloonDemande demande, User user) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'></head><body>");
        html.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>");

        // Header
        html.append("<div style='background: linear-gradient(to right, #413443, #5a4a5c); ");
        html.append("color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0;'>");
        html.append("<h1 style='margin: 0; font-size: 24px;'>🏠 Nouvelle demande de Saloon</h1>");
        html.append("</div>");

        // Content
        html.append("<div style='background: #f9f9f9; padding: 20px; border: 1px solid #ddd;'>");

        // Lieu info
        html.append("<h2 style='color: #413443; margin-top: 0;'>📍 Informations du lieu</h2>");
        html.append("<table style='width: 100%; border-collapse: collapse;'>");
        addTableRow(html, "Nom du lieu", demande.getPlaceName());
        addTableRow(html, "Type", demande.getPlaceType().getLabel());
        addTableRow(html, "Adresse", demande.getAddress());
        if (demande.getComment() != null && !demande.getComment().isBlank()) {
            addTableRow(html, "Commentaire", demande.getComment());
        }
        html.append("</table>");

        // User info
        html.append("<h2 style='color: #413443; margin-top: 20px;'>👤 Informations utilisateur</h2>");
        html.append("<table style='width: 100%; border-collapse: collapse;'>");
        if (user != null) {
            addTableRow(html, "ID utilisateur", String.valueOf(user.getId()));
            if (user.getUserName() != null) {
                addTableRow(html, "Pseudo", user.getUserName());
            }
            if (user.getEmail() != null) {
                addTableRow(html, "Email", user.getEmail());
            }
        } else {
            addTableRow(html, "Utilisateur", "Non connecté / Anonyme");
        }
        html.append("</table>");

        // Date
        html.append("<p style='color: #666; font-size: 12px; margin-top: 20px; text-align: right;'>");
        html.append("Reçu le ").append(demande.getCreatedAt().format(DATE_FORMATTER));
        html.append("</p>");

        html.append("</div>");

        // Footer
        html.append("<div style='background: #413443; color: white; padding: 15px; ");
        html.append("text-align: center; border-radius: 0 0 8px 8px; font-size: 12px;'>");
        html.append("Saloons - L'app sociale de géolocalisation");
        html.append("</div>");

        html.append("</div></body></html>");

        return html.toString();
    }

    private void addTableRow(StringBuilder html, String label, String value) {
        html.append("<tr>");
        html.append("<td style='padding: 8px; border-bottom: 1px solid #eee; font-weight: bold; width: 35%;'>");
        html.append(label).append("</td>");
        html.append("<td style='padding: 8px; border-bottom: 1px solid #eee;'>");
        html.append(escapeHtml(value)).append("</td>");
        html.append("</tr>");
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

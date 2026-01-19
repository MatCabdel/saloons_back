package com.backend_project_template.domains.contact;

import com.backend_project_template.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ContactService {

    private static final Logger logger = LoggerFactory.getLogger(ContactService.class);

    private final EmailService emailService;

    public ContactService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void sendContactMessage(ContactDTO dto) {
        logger.info("Nouveau message de contact reçu de {} {} ({})",
                dto.getFirstName(), dto.getLastName(), dto.getEmail());

        String subject = String.format("[SALOONS] Contact - %s - %s %s",
                dto.getSubject().getLabel(),
                dto.getFirstName(),
                dto.getLastName());

        String htmlContent = buildEmailContent(dto);

        emailService.sendEmailToAdmin(subject, htmlContent, dto.getEmail());

        logger.info("Email de contact envoyé avec succès pour: {} {}", dto.getFirstName(), dto.getLastName());
    }

    private String buildEmailContent(ContactDTO dto) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'></head><body>");
        html.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>");

        // Header
        html.append("<div style='background: linear-gradient(to right, #413443, #5a4a5c); ");
        html.append("color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0;'>");
        html.append("<h1 style='margin: 0; font-size: 24px;'>📬 Nouveau message de contact</h1>");
        html.append("</div>");

        // Content
        html.append("<div style='background: #f9f9f9; padding: 20px; border: 1px solid #ddd;'>");

        // Contact info
        html.append("<h2 style='color: #413443; margin-top: 0;'>👤 Informations de contact</h2>");
        html.append("<table style='width: 100%; border-collapse: collapse;'>");
        addTableRow(html, "Nom", dto.getLastName());
        addTableRow(html, "Prénom", dto.getFirstName());
        addTableRow(html, "Email", dto.getEmail());
        addTableRow(html, "Sujet", getSubjectIcon(dto.getSubject()) + " " + dto.getSubject().getLabel());
        html.append("</table>");

        // Message
        html.append("<h2 style='color: #413443; margin-top: 20px;'>💬 Message</h2>");
        html.append("<div style='background: white; padding: 15px; border-radius: 8px; ");
        html.append("border-left: 4px solid #ffc122; margin-top: 10px;'>");
        html.append("<p style='margin: 0; white-space: pre-wrap;'>").append(escapeHtml(dto.getMessage()))
                .append("</p>");
        html.append("</div>");

        html.append("</div>");

        // Footer
        html.append("<div style='background: #413443; color: white; padding: 15px; ");
        html.append("text-align: center; border-radius: 0 0 8px 8px; font-size: 12px;'>");
        html.append(
                "<p style='margin: 0;'>💡 Tu peux répondre directement à cet email pour contacter l'utilisateur.</p>");
        html.append("</div>");

        html.append("</div></body></html>");

        return html.toString();
    }

    private void addTableRow(StringBuilder html, String label, String value) {
        html.append("<tr>");
        html.append(
                "<td style='padding: 10px; border-bottom: 1px solid #eee; font-weight: bold; color: #666; width: 30%;'>")
                .append(label).append("</td>");
        html.append("<td style='padding: 10px; border-bottom: 1px solid #eee; color: #333;'>")
                .append(escapeHtml(value)).append("</td>");
        html.append("</tr>");
    }

    private String getSubjectIcon(ContactSubject subject) {
        return switch (subject) {
            case QUESTION -> "❓";
            case SUGGESTION -> "💡";
            case BUG -> "🐛";
            case PARTNERSHIP -> "🤝";
            case REPORT -> "🚨";
            case OTHER -> "📝";
        };
    }

    private String escapeHtml(String text) {
        if (text == null)
            return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

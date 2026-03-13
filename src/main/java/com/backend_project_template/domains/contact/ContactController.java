package com.backend_project_template.domains.contact;

import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/contact")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> submitContact(@Valid @RequestBody ContactDTO dto) {
        contactService.sendContactMessage(dto);

        return ResponseEntity.ok(Map.of(
                "message", "Merci ! Ton message a bien été envoyé. Nous te répondrons dans les plus brefs délais 🙂"));
    }
}

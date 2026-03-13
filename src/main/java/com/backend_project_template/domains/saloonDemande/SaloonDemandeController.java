package com.backend_project_template.domains.saloonDemande;

import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserRepository;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/saloon-demande")
public class SaloonDemandeController {

    private final SaloonDemandeService saloonDemandeService;
    private final UserRepository userRepository;

    public SaloonDemandeController(SaloonDemandeService saloonDemandeService, UserRepository userRepository) {
        this.saloonDemandeService = saloonDemandeService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> submitDemande(
            @Valid @RequestBody SaloonDemandeDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = null;
        if (userDetails != null) {
            user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        }

        saloonDemandeService.createDemande(dto, user);

        return ResponseEntity.ok(Map.of(
                "message", "Merci ! Ta demande a bien été envoyée. Nous allons l'étudier 🙂"));
    }
}

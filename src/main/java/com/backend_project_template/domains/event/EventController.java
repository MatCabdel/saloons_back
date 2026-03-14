package com.backend_project_template.domains.event;

import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserRepository;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/event")
public class EventController {

    private static final int DAYS_IN_WEEK = 7;
    private static final int DAYS_IN_MONTH = 30;
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final EventService eventService;
    private final UserRepository userRepository;

    public EventController(EventService eventService, UserRepository userRepository) {
        this.eventService = eventService;
        this.userRepository = userRepository;
    }

    /**
     * Récupère les événements actifs pour une période donnée avec pagination côté
     * serveur.
     * Périodes supportées : today, week, month, all.
     */
    @GetMapping
    public ResponseEntity<Page<EventDTO>> getEvents(
            @RequestParam(defaultValue = "all") String period,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {
        Long userId = getUserId(principal);
        PageRequest pageRequest = PageRequest.of(page,
                Math.min(size, DEFAULT_PAGE_SIZE),
                Sort.by("startDateTime").ascending());
        LocalDateTime from;
        LocalDateTime to;

        switch (period) {
            case "today":
                from = LocalDate.now().atStartOfDay();
                to = LocalDate.now().atTime(LocalTime.MAX);
                break;
            case "week":
                from = LocalDate.now().atStartOfDay();
                to = LocalDate.now().plusDays(DAYS_IN_WEEK).atTime(LocalTime.MAX);
                break;
            case "month":
                from = LocalDate.now().atStartOfDay();
                to = LocalDate.now().plusDays(DAYS_IN_MONTH).atTime(LocalTime.MAX);
                break;
            default:
                return ResponseEntity.ok(
                        eventService.getAllActiveEventsPaged(userId, pageRequest));
        }

        return ResponseEntity.ok(
                eventService.getEventsByPeriodPaged(from, to, userId, pageRequest));
    }

    /**
     * Récupère un événement par son id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEventById(@PathVariable Long id, Principal principal) {
        Long userId = getUserId(principal);
        EventDTO event = eventService.getEventById(id, userId);
        if (event == null) {
            return ResponseEntity.<EventDTO>notFound().build();
        }
        return ResponseEntity.ok(event);
    }

    /**
     * Toggle l'intérêt de l'utilisateur pour un événement.
     */
    @PostMapping("/{id}/interest")
    public ResponseEntity<Map<String, Object>> toggleInterest(
            @PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean interested = eventService.toggleInterest(id, principal.getName());
        long count = eventService.getInterestedCount(id);
        return ResponseEntity.ok(Map.of("interested", interested, "interestedCount", count));
    }

    private Long getUserId(Principal principal) {
        if (principal == null) {
            return null;
        }
        return userRepository.findByEmail(principal.getName())
                .map(User::getId)
                .orElse(null);
    }
}

package com.backend_project_template.domains.event;

import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventInterestRepository eventInterestRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;

    public EventService(
            EventRepository eventRepository,
            EventInterestRepository eventInterestRepository,
            UserRepository userRepository,
            EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.eventInterestRepository = eventInterestRepository;
        this.userRepository = userRepository;
        this.eventMapper = eventMapper;
    }

    /**
     * Récupère les événements actifs pour une période donnée (exclut les passés).
     */
    public List<EventDTO> getEventsByPeriod(LocalDateTime from, LocalDateTime to, Long userId) {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        List<Event> events = eventRepository.findActiveByPeriod(from, to, startOfToday);
        return events.stream()
                .map(event -> eventMapper.toEventDTO(event,
                        eventInterestRepository.countByEventId(event.getId()),
                        userId != null
                                && eventInterestRepository.existsByUserIdAndEventId(
                                        userId, event.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Récupère tous les événements actifs dont la date n'est pas passée.
     */
    public List<EventDTO> getAllActiveEvents(Long userId) {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        List<Event> events = eventRepository.findAllActiveNotPast(startOfToday);
        return events.stream()
                .map(event -> eventMapper.toEventDTO(event,
                        eventInterestRepository.countByEventId(event.getId()),
                        userId != null
                                && eventInterestRepository.existsByUserIdAndEventId(
                                        userId, event.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Récupère les événements actifs par période avec pagination côté serveur.
     */
    public Page<EventDTO> getEventsByPeriodPaged(
            LocalDateTime from, LocalDateTime to, Long userId, Pageable pageable) {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        Page<Event> events = eventRepository.findActiveByPeriodPaged(
                from, to, startOfToday, pageable);
        return events.map(event -> eventMapper.toEventDTO(event,
                eventInterestRepository.countByEventId(event.getId()),
                userId != null
                        && eventInterestRepository.existsByUserIdAndEventId(
                                userId, event.getId())));
    }

    /**
     * Récupère tous les événements actifs non passés avec pagination côté serveur.
     */
    public Page<EventDTO> getAllActiveEventsPaged(Long userId, Pageable pageable) {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        Page<Event> events = eventRepository.findAllActiveNotPastPaged(
                startOfToday, pageable);
        return events.map(event -> eventMapper.toEventDTO(event,
                eventInterestRepository.countByEventId(event.getId()),
                userId != null
                        && eventInterestRepository.existsByUserIdAndEventId(
                                userId, event.getId())));
    }

    /**
     * Récupère un événement par son id.
     */
    public EventDTO getEventById(Long id, Long userId) {
        Event event = eventRepository.findById(id).orElse(null);
        if (event == null) {
            return null;
        }
        return eventMapper.toEventDTO(event,
                eventInterestRepository.countByEventId(event.getId()),
                userId != null
                        && eventInterestRepository.existsByUserIdAndEventId(userId, event.getId()));
    }

    /**
     * Récupère un événement par son id (sans contexte utilisateur, pour admin).
     */
    public EventDTO getEventById(Long id) {
        Event event = eventRepository.findById(id).orElse(null);
        if (event == null) {
            return null;
        }
        return eventMapper.toEventDTO(event,
                eventInterestRepository.countByEventId(event.getId()), false);
    }

    /**
     * Toggle l'intérêt d'un utilisateur pour un événement.
     * Retourne true si l'utilisateur est maintenant intéressé, false sinon.
     */
    @Transactional
    public boolean toggleInterest(Long eventId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (eventInterestRepository.existsByUserIdAndEventId(user.getId(), eventId)) {
            eventInterestRepository.deleteByUserIdAndEventId(user.getId(), eventId);
            return false;
        } else {
            EventInterest interest = new EventInterest();
            interest.setUser(user);
            interest.setEvent(event);
            eventInterestRepository.save(interest);
            return true;
        }
    }

    /**
     * Récupère le nombre d'intéressés pour un événement.
     */
    public long getInterestedCount(Long eventId) {
        return eventInterestRepository.countByEventId(eventId);
    }
}

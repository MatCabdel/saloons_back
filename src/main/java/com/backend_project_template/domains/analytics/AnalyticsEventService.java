package com.backend_project_template.domains.analytics;

import org.springframework.stereotype.Service;

/**
 * Service pour enregistrer les événements analytiques.
 * À brancher dans les services existants pour alimenter la table analytics_events.
 * Cela permettra en V2 de calculer des funnels et rétentions fines.
 */
@Service
public class AnalyticsEventService {

    private final AnalyticsEventRepository analyticsEventRepository;

    public AnalyticsEventService(AnalyticsEventRepository analyticsEventRepository) {
        this.analyticsEventRepository = analyticsEventRepository;
    }

    public void track(AnalyticsEventType type, Long userId, Long saloonId, String city) {
        AnalyticsEvent event = new AnalyticsEvent(type, userId, saloonId, city);
        analyticsEventRepository.save(event);
    }

    public void track(AnalyticsEventType type, Long userId, Long saloonId, String city, String payload) {
        AnalyticsEvent event = new AnalyticsEvent(type, userId, saloonId, city);
        event.setPayload(payload);
        analyticsEventRepository.save(event);
    }

    public void trackSimple(AnalyticsEventType type, Long userId) {
        track(type, userId, null, null);
    }

    public void trackWithCity(AnalyticsEventType type, Long userId, String city) {
        track(type, userId, null, city);
    }
}

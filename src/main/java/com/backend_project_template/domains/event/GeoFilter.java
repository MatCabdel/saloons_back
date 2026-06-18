package com.backend_project_template.domains.event;

/**
 * Encapsule les paramètres de filtrage géographique (Haversine).
 */
public record GeoFilter(double lat, double lng) {
}

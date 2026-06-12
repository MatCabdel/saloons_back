package com.backend_project_template.domains.saloon;

import java.math.BigDecimal;

public record BboxRequest(
        BigDecimal minLat,
        BigDecimal maxLat,
        BigDecimal minLng,
        BigDecimal maxLng,
        SaloonType type) {
}

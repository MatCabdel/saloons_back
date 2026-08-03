package com.backend_project_template.domains.admin.stats;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/stats/saloons")
public class SaloonDetailStatsController {

  private final SaloonDetailStatsService statsService;

  public SaloonDetailStatsController(SaloonDetailStatsService statsService) {
    this.statsService = statsService;
  }

  @GetMapping("/{saloonId}")
  public ResponseEntity<SaloonDetailStatsDTO> getStats(
      @PathVariable Long saloonId,
      @RequestParam(defaultValue = "DAY") String period,
      @RequestParam(defaultValue = "8") int referenceWeeks,
      @RequestParam(required = false) String weekDay,
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    LocalDate selectedDate = date == null ? LocalDate.now() : date;
    return ResponseEntity.ok(
        statsService.getStats(saloonId, period, selectedDate, referenceWeeks, weekDay));
  }
}

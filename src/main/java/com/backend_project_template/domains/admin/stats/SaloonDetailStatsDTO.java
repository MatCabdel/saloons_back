package com.backend_project_template.domains.admin.stats;

import java.util.List;

public record SaloonDetailStatsDTO(
    Long saloonId,
    String saloonName,
    String city,
    Integer currentPresence,
    boolean presenceAvailable,
    String period,
    String periodStart,
    String periodEnd,
    long totalConnections,
    long uniqueVisitors,
    long allTimeEntries,
    long allTimeUniqueVisitors,
    List<ChartPoint> connections,
    List<ChartPoint> hourlyConnections,
    PeakSlot peakSlot,
    ThirtyDayComparison thirtyDays,
    int referenceWeeks,
    String referencePeriodStart,
    String referencePeriodEnd,
    List<AverageChartPoint> weekDayAverages,
    String selectedWeekDay,
    List<AverageChartPoint> weekDayHourlyAverages,
    AveragePeakSlot averagePeakSlot,
    DayComparison selectedDayComparison,
    WeeklySummary weeklySummary,
    MonthlySummary monthlySummary,
    double averagePerDay,
    double averagePerWeek,
    Double averagePerMonth,
    String averagesPeriod
) {
  public record ChartPoint(String label, long value) {
  }

  public record PeakSlot(String label, long value) {
  }

  public record AverageChartPoint(String label, double value) {
  }

  public record AveragePeakSlot(String label, double value) {
  }

  public record ThirtyDayComparison(
      String currentStart, String currentEnd, long currentEntries, long currentUniqueVisitors,
      String previousStart, String previousEnd, long previousEntries, Double evolutionPercent) {
  }

  public record DayComparison(
      String date, String weekDay, long entries, double usualAverage,
      double difference, Double differencePercent) {
  }

  public record WeeklySummary(
      String currentStart, String currentEnd, long currentEntries,
      double comparablePeriodAverage, double difference, Double differencePercent) {
  }

  public record MonthlySummary(
      long currentMonthEntries, long previousMonthEntries,
      Double lastThreeCompleteMonthsAverage, Double lastSixCompleteMonthsAverage,
      String historyMessage) {
  }
}

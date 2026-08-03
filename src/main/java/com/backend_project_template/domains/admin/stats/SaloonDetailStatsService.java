package com.backend_project_template.domains.admin.stats;

import com.backend_project_template.domains.admin.stats.SaloonDetailStatsDTO.AverageChartPoint;
import com.backend_project_template.domains.admin.stats.SaloonDetailStatsDTO.AveragePeakSlot;
import com.backend_project_template.domains.admin.stats.SaloonDetailStatsDTO.ChartPoint;
import com.backend_project_template.domains.admin.stats.SaloonDetailStatsDTO.DayComparison;
import com.backend_project_template.domains.admin.stats.SaloonDetailStatsDTO.MonthlySummary;
import com.backend_project_template.domains.admin.stats.SaloonDetailStatsDTO.PeakSlot;
import com.backend_project_template.domains.admin.stats.SaloonDetailStatsDTO.ThirtyDayComparison;
import com.backend_project_template.domains.admin.stats.SaloonDetailStatsDTO.WeeklySummary;
import com.backend_project_template.domains.saloon.Saloon;
import com.backend_project_template.domains.saloon.SaloonRepository;
import com.backend_project_template.domains.saloonSession.SaloonSessionRepository;
import com.backend_project_template.domains.session.SessionRedisService;
import com.backend_project_template.exception.ResourceNotFoundException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("checkstyle:MagicNumber")
public class SaloonDetailStatsService {

  private static final int THIRTY_DAYS = 30;
  private static final int DEFAULT_REFERENCE_WEEKS = 8;
  private static final Set<Integer> ALLOWED_REFERENCE_WEEKS = Set.of(4, 8, 12);
  private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("dd/MM");
  private static final DateTimeFormatter WEEK_DAY_LABEL =
      DateTimeFormatter.ofPattern("EEE dd/MM", Locale.FRENCH);
  private static final DateTimeFormatter MONTH_LABEL =
      DateTimeFormatter.ofPattern("MMM", Locale.FRENCH);
  private static final String[] WEEK_DAY_LABELS = {
      "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"
  };

  private final SaloonRepository saloonRepository;
  private final SaloonSessionRepository sessionRepository;
  private final SessionRedisService redisService;

  public SaloonDetailStatsService(SaloonRepository saloonRepository,
      SaloonSessionRepository sessionRepository, SessionRedisService redisService) {
    this.saloonRepository = saloonRepository;
    this.sessionRepository = sessionRepository;
    this.redisService = redisService;
  }

  public SaloonDetailStatsDTO getStats(Long saloonId, String rawPeriod, LocalDate selectedDate,
      int rawReferenceWeeks, String rawWeekDay) {
    Saloon saloon = saloonRepository.findById(saloonId)
        .orElseThrow(() -> new ResourceNotFoundException("Saloon non trouvé"));
    LocalDate today = LocalDate.now();
    if (selectedDate.isAfter(today)) {
      throw new IllegalArgumentException("La date ne peut pas être future");
    }

    Period period = Period.from(rawPeriod);
    int referenceWeeks = validateReferenceWeeks(rawReferenceWeeks);
    DayOfWeek selectedWeekDay = parseWeekDay(rawWeekDay, selectedDate.getDayOfWeek());
    DateRange range = getRange(period, selectedDate, today);
    DateRange referenceRange = completedWeeksRange(today, referenceWeeks);

    List<ChartPoint> connections = getConnections(saloonId, period, range);
    List<ChartPoint> hourly = getHourly(saloonId, selectedDate);
    PeakSlot peak = peak(hourly);
    List<AverageChartPoint> weekDayAverages =
        getWeekDayAverages(saloonId, referenceRange, referenceWeeks);
    List<AverageChartPoint> weekDayHourlyAverages =
        getWeekDayHourlyAverages(saloonId, referenceRange, referenceWeeks, selectedWeekDay);
    AveragePeakSlot averagePeak = averagePeak(weekDayHourlyAverages);

    Integer currentPresence = null;
    boolean presenceAvailable = true;
    try {
      currentPresence = redisService.getActivePresenceCount(saloonId);
    } catch (RuntimeException exception) {
      presenceAvailable = false;
    }

    long periodEntries = count(saloonId, range);
    long periodUnique = unique(saloonId, range);
    ThirtyDayComparison thirtyDays = getThirtyDayComparison(saloonId, today);
    DayComparison dayComparison =
        getDayComparison(saloonId, selectedDate, weekDayAverages);
    WeeklySummary weeklySummary =
        getWeeklySummary(saloonId, today, weekDayAverages);
    MonthlySummary monthlySummary = getMonthlySummary(saloonId, today);
    double dailyAverage = round(thirtyDays.currentEntries() / (double) THIRTY_DAYS);

    return new SaloonDetailStatsDTO(
        saloon.getId(), saloon.getName(), saloon.getCity(), currentPresence, presenceAvailable,
        period.name(), range.from().toString(), range.to().toString(),
        periodEntries, periodUnique,
        sessionRepository.countBySaloonId(saloonId),
        sessionRepository.countUniqueVisitorsForSaloon(saloonId),
        connections, hourly, peak, thirtyDays, referenceWeeks,
        referenceRange.from().toString(), referenceRange.to().toString(),
        weekDayAverages, selectedWeekDay.name(), weekDayHourlyAverages, averagePeak,
        dayComparison, weeklySummary, monthlySummary,
        dailyAverage, weeklySummary.comparablePeriodAverage(),
        monthlySummary.lastSixCompleteMonthsAverage(),
        "30 derniers jours / " + referenceWeeks + " semaines complètes / 6 mois complets");
  }

  private ThirtyDayComparison getThirtyDayComparison(Long saloonId, LocalDate today) {
    DateRange current = new DateRange(today.minusDays(THIRTY_DAYS - 1), today);
    DateRange previous =
        new DateRange(current.from().minusDays(THIRTY_DAYS), current.from().minusDays(1));
    long currentEntries = count(saloonId, current);
    long previousEntries = count(saloonId, previous);
    return new ThirtyDayComparison(
        current.from().toString(), current.to().toString(), currentEntries,
        unique(saloonId, current), previous.from().toString(), previous.to().toString(),
        previousEntries, percentageDifference(currentEntries, previousEntries));
  }

  private DayComparison getDayComparison(Long saloonId, LocalDate selectedDate,
      List<AverageChartPoint> weekDayAverages) {
    long entries = count(saloonId, new DateRange(selectedDate, selectedDate));
    String selectedWeekDayLabel = weekDayLabel(selectedDate.getDayOfWeek());
    double usualAverage = weekDayAverages.stream()
        .filter(point -> point.label().equals(selectedWeekDayLabel))
        .mapToDouble(AverageChartPoint::value)
        .findFirst()
        .orElse(0);
    double difference = round(entries - usualAverage);
    return new DayComparison(selectedDate.toString(), selectedWeekDayLabel,
        entries, usualAverage, difference, percentageDifference(entries, usualAverage));
  }

  private WeeklySummary getWeeklySummary(Long saloonId, LocalDate today,
      List<AverageChartPoint> weekDayAverages) {
    LocalDate currentMonday =
        today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    DateRange currentWeek = new DateRange(currentMonday, today);
    long currentEntries = count(saloonId, currentWeek);
    int elapsedWeekDays = today.getDayOfWeek().getValue();
    double comparableAverage = round(weekDayAverages.stream()
        .limit(elapsedWeekDays)
        .mapToDouble(AverageChartPoint::value)
        .sum());
    double difference = round(currentEntries - comparableAverage);
    return new WeeklySummary(currentWeek.from().toString(), currentWeek.to().toString(),
        currentEntries, comparableAverage, difference,
        percentageDifference(currentEntries, comparableAverage));
  }

  private MonthlySummary getMonthlySummary(Long saloonId, LocalDate today) {
    YearMonth currentMonth = YearMonth.from(today);
    DateRange current = new DateRange(currentMonth.atDay(1), today);
    YearMonth previousMonth = currentMonth.minusMonths(1);
    DateRange previous =
        new DateRange(previousMonth.atDay(1), previousMonth.atEndOfMonth());
    DateRange lastThree = completeMonthsRange(currentMonth, 3);
    DateRange lastSix = completeMonthsRange(currentMonth, 6);
    LocalDateTime firstConnection = sessionRepository.findFirstConnectionDateForSaloon(saloonId);
    boolean hasThreeMonths = hasHistorySince(firstConnection, lastThree.from());
    boolean hasSixMonths = hasHistorySince(firstConnection, lastSix.from());
    String historyMessage = null;
    if (!hasThreeMonths) {
      historyMessage = "Historique insuffisant pour calculer les moyennes sur 3 et 6 mois complets.";
    } else if (!hasSixMonths) {
      historyMessage = "Historique insuffisant pour calculer la moyenne sur 6 mois complets.";
    }
    return new MonthlySummary(count(saloonId, current), count(saloonId, previous),
        hasThreeMonths ? round(count(saloonId, lastThree) / 3.0) : null,
        hasSixMonths ? round(count(saloonId, lastSix) / 6.0) : null,
        historyMessage);
  }

  private List<AverageChartPoint> getWeekDayAverages(Long saloonId, DateRange range,
      int referenceWeeks) {
    Map<Integer, Long> values = numericValues(
        sessionRepository.countEntriesByWeekDayForSaloon(saloonId, start(range), end(range)));
    List<AverageChartPoint> result = new ArrayList<>();
    for (DayOfWeek day : DayOfWeek.values()) {
      result.add(new AverageChartPoint(weekDayLabel(day),
          round(values.getOrDefault(toDatabaseWeekDay(day), 0L) / (double) referenceWeeks)));
    }
    return result;
  }

  private List<AverageChartPoint> getWeekDayHourlyAverages(Long saloonId, DateRange range,
      int referenceWeeks, DayOfWeek selectedWeekDay) {
    Map<Integer, Long> values = new HashMap<>();
    int databaseWeekDay = toDatabaseWeekDay(selectedWeekDay);
    sessionRepository.countEntriesByWeekDayAndHourForSaloon(
        saloonId, start(range), end(range)).stream()
        .filter(row -> ((Number) row[0]).intValue() == databaseWeekDay)
        .forEach(row -> values.put(((Number) row[1]).intValue(), ((Number) row[2]).longValue()));
    List<AverageChartPoint> result = new ArrayList<>();
    for (int hour = 0; hour < 24; hour++) {
      result.add(new AverageChartPoint(hourLabel(hour),
          round(values.getOrDefault(hour, 0L) / (double) referenceWeeks)));
    }
    return result;
  }

  private List<ChartPoint> getConnections(Long saloonId, Period period, DateRange range) {
    LocalDateTime from = start(range);
    LocalDateTime to = end(range);
    if (period == Period.DAY) {
      return fillHours(sessionRepository.countEntriesByHourForSaloon(saloonId, from, to));
    }
    if (period == Period.YEAR) {
      return fillMonths(sessionRepository.countEntriesByMonthForSaloon(saloonId, from, to));
    }
    return fillDays(range, sessionRepository.countEntriesByDayForSaloon(saloonId, from, to),
        period == Period.WEEK);
  }

  private List<ChartPoint> getHourly(Long saloonId, LocalDate date) {
    return fillHours(sessionRepository.countEntriesByHourForSaloon(
        saloonId, date.atStartOfDay(), date.atTime(LocalTime.MAX)));
  }

  private List<ChartPoint> fillHours(List<Object[]> raw) {
    Map<Integer, Long> values = numericValues(raw);
    List<ChartPoint> result = new ArrayList<>();
    for (int hour = 0; hour < 24; hour++) {
      result.add(new ChartPoint(hourLabel(hour), values.getOrDefault(hour, 0L)));
    }
    return result;
  }

  private List<ChartPoint> fillDays(DateRange range, List<Object[]> raw,
      boolean includeWeekDay) {
    Map<String, Long> values = new HashMap<>();
    raw.forEach(row -> values.put(row[0].toString(), ((Number) row[1]).longValue()));
    List<ChartPoint> result = new ArrayList<>();
    DateTimeFormatter labelFormatter = includeWeekDay ? WEEK_DAY_LABEL : DAY_LABEL;
    for (LocalDate date = range.from(); !date.isAfter(range.to()); date = date.plusDays(1)) {
      result.add(new ChartPoint(date.format(labelFormatter),
          values.getOrDefault(date.toString(), 0L)));
    }
    return result;
  }

  private List<ChartPoint> fillMonths(List<Object[]> raw) {
    Map<Integer, Long> values = numericValues(raw);
    List<ChartPoint> result = new ArrayList<>();
    for (int month = 1; month <= 12; month++) {
      result.add(new ChartPoint(YearMonth.of(2000, month).format(MONTH_LABEL),
          values.getOrDefault(month, 0L)));
    }
    return result;
  }

  private PeakSlot peak(List<ChartPoint> points) {
    return points.stream()
        .max((first, second) -> Long.compare(first.value(), second.value()))
        .filter(point -> point.value() > 0)
        .map(point -> new PeakSlot(point.label(), point.value()))
        .orElse(new PeakSlot("Aucun créneau", 0));
  }

  private AveragePeakSlot averagePeak(List<AverageChartPoint> points) {
    return points.stream()
        .max((first, second) -> Double.compare(first.value(), second.value()))
        .filter(point -> point.value() > 0)
        .map(point -> new AveragePeakSlot(point.label(), point.value()))
        .orElse(new AveragePeakSlot("Aucun créneau", 0));
  }

  private Map<Integer, Long> numericValues(List<Object[]> raw) {
    Map<Integer, Long> values = new HashMap<>();
    raw.forEach(row -> values.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue()));
    return values;
  }

  private long count(Long saloonId, DateRange range) {
    return sessionRepository.countBySaloonIdAndConnectedAtBetween(
        saloonId, start(range), end(range));
  }

  private long unique(Long saloonId, DateRange range) {
    return sessionRepository.countUniqueVisitorsForSaloonBetween(
        saloonId, start(range), end(range));
  }

  private Double percentageDifference(double current, double baseline) {
    if (baseline == 0) {
      return current == 0 ? 0.0 : null;
    }
    return round(((current - baseline) / baseline) * 100.0);
  }

  private double round(double value) {
    return Math.round(value * 100.0) / 100.0;
  }

  private int validateReferenceWeeks(int value) {
    if (!ALLOWED_REFERENCE_WEEKS.contains(value)) {
      throw new IllegalArgumentException("La période de référence doit être de 4, 8 ou 12 semaines");
    }
    return value;
  }

  private DayOfWeek parseWeekDay(String value, DayOfWeek defaultValue) {
    if (value == null || value.isBlank()) {
      return defaultValue;
    }
    try {
      return DayOfWeek.valueOf(value.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException exception) {
      throw new IllegalArgumentException("Jour de la semaine invalide");
    }
  }

  private DateRange completedWeeksRange(LocalDate today, int weeks) {
    LocalDate currentMonday =
        today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    LocalDate end = currentMonday.minusDays(1);
    return new DateRange(end.minusWeeks(weeks).plusDays(1), end);
  }

  private DateRange completeMonthsRange(YearMonth currentMonth, int months) {
    YearMonth firstMonth = currentMonth.minusMonths(months);
    YearMonth lastMonth = currentMonth.minusMonths(1);
    return new DateRange(firstMonth.atDay(1), lastMonth.atEndOfMonth());
  }

  private boolean hasHistorySince(LocalDateTime firstConnection, LocalDate expectedStart) {
    return firstConnection != null && !firstConnection.toLocalDate().isAfter(expectedStart);
  }

  private int toDatabaseWeekDay(DayOfWeek day) {
    return day == DayOfWeek.SUNDAY ? 1 : day.getValue() + 1;
  }

  private String weekDayLabel(DayOfWeek day) {
    return WEEK_DAY_LABELS[day.getValue() - 1];
  }

  private String hourLabel(int hour) {
    return String.format("%02d h – %02d h", hour, (hour + 1) % 24);
  }

  private LocalDateTime start(DateRange range) {
    return range.from().atStartOfDay();
  }

  private LocalDateTime end(DateRange range) {
    return range.to().atTime(LocalTime.MAX);
  }

  private DateRange getRange(Period period, LocalDate date, LocalDate today) {
    DateRange range = switch (period) {
      case DAY -> new DateRange(date, date);
      case WEEK -> {
        LocalDate monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        yield new DateRange(monday, monday.plusDays(6));
      }
      case MONTH -> new DateRange(date.withDayOfMonth(1),
          date.withDayOfMonth(date.lengthOfMonth()));
      case YEAR -> new DateRange(date.withDayOfYear(1),
          date.withDayOfYear(date.lengthOfYear()));
    };
    return new DateRange(range.from(), range.to().isAfter(today) ? today : range.to());
  }

  private enum Period {
    DAY, WEEK, MONTH, YEAR;

    static Period from(String value) {
      try {
        return Period.valueOf(value.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException exception) {
        throw new IllegalArgumentException("Période invalide");
      }
    }
  }

  private record DateRange(LocalDate from, LocalDate to) {
  }
}

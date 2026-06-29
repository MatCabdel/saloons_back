package com.backend_project_template.domains.admin.stats;

import com.backend_project_template.domains.conversation.ConversationRepository;
import com.backend_project_template.domains.heartRequest.HeartRequestRepository;
import com.backend_project_template.domains.match.MatchRepository;
import com.backend_project_template.domains.message.MessageRepository;
import com.backend_project_template.domains.report.ReportRepository;
import com.backend_project_template.domains.report.ReportStatus;
import com.backend_project_template.domains.saloon.Saloon;
import com.backend_project_template.domains.saloon.SaloonRepository;
import com.backend_project_template.domains.saloonSession.SaloonSessionRepository;
import com.backend_project_template.domains.session.SessionRedisService;
import com.backend_project_template.domains.subscription.PremiumSubscriptionRepository;
import com.backend_project_template.domains.user.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur pour les endpoints de statistiques avancées du dashboard admin.
 * Tous les endpoints acceptent des paramètres from/to pour le filtrage par
 * période.
 */
@RestController
@RequestMapping("/admin/stats")
@SuppressWarnings("checkstyle:ParameterNumber")
public class AdminStatsController {

    private static final int TOP_LIMIT = 10;
    private static final int DAYS_CHURN_THRESHOLD = 30;
    private static final int MYSQL_SUNDAY = 1;
    private static final int MYSQL_SATURDAY = 7;
    private static final double PERCENT_MULTIPLIER = 10000.0;
    private static final double PERCENT_DIVISOR = 100.0;
    private static final int DAYS_ACTIVE_WEEK = 7;
    private static final int DAYS_ACTIVE_MONTH = 30;
    private static final int DAYS_RETENTION_D1_FROM = 8;
    private static final int DAYS_RETENTION_D1_TO = 2;
    private static final int DAYS_RETENTION_D7_FROM = 37;
    private static final int DAYS_RETENTION_D30_FROM = 60;
    private static final int DAYS_RETENTION_D30_TO = 31;
    private static final int DAYS_ISO_SUNDAY = 7;

    private final UserRepository userRepository;
    private final SaloonRepository saloonRepository;
    private final MatchRepository matchRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final SaloonSessionRepository saloonSessionRepository;
    private final HeartRequestRepository heartRequestRepository;
    private final PremiumSubscriptionRepository premiumSubscriptionRepository;
    private final SessionRedisService sessionRedisService;
    private final ReportRepository reportRepository;

    public AdminStatsController(
            UserRepository userRepository,
            SaloonRepository saloonRepository,
            MatchRepository matchRepository,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            SaloonSessionRepository saloonSessionRepository,
            HeartRequestRepository heartRequestRepository,
            PremiumSubscriptionRepository premiumSubscriptionRepository,
            SessionRedisService sessionRedisService,
            ReportRepository reportRepository) {
        this.userRepository = userRepository;
        this.saloonRepository = saloonRepository;
        this.matchRepository = matchRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.saloonSessionRepository = saloonSessionRepository;
        this.heartRequestRepository = heartRequestRepository;
        this.premiumSubscriptionRepository = premiumSubscriptionRepository;
        this.sessionRedisService = sessionRedisService;
        this.reportRepository = reportRepository;
    }

    // ==================== VUE D'ENSEMBLE ====================

    @GetMapping("/overview")
    public ResponseEntity<OverviewStatsDTO> getOverview() {
        OverviewStatsDTO dto = new OverviewStatsDTO();
        dto.setTotalUsers(userRepository.count());
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(DAYS_ACTIVE_WEEK);
        dto.setActiveUsers(userRepository.countByLastLoginAtAfter(weekAgo));
        dto.setPremiumUsers(userRepository.countByIsPremiumTrue());
        dto.setTotalSaloons(saloonRepository.count());
        dto.setConnectedUsers(sessionRedisService.getTotalConnectedUsers());
        dto.setTotalMatches(matchRepository.count());
        dto.setTotalConversations(conversationRepository.count());
        dto.setTotalMessages(messageRepository.count());
        dto.setProfilesCompleted(userRepository.countProfileComplete());
        dto.setPendingReports(reportRepository.countByStatus(ReportStatus.PENDING));

        // Villes couvertes
        List<Saloon> allSaloons = saloonRepository.findAll();
        Set<String> cities = allSaloons.stream()
                .filter(s -> s.getCity() != null && !s.getCity().isEmpty())
                .map(Saloon::getCity)
                .collect(Collectors.toSet());
        dto.setCitiesCovered(cities.size());

        return ResponseEntity.ok(dto);
    }

    // ==================== CROISSANCE ====================

    @GetMapping("/growth")
    public ResponseEntity<GrowthStatsDTO> getGrowthStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = to.atTime(LocalTime.MAX);
        LocalDateTime now = LocalDateTime.now();

        GrowthStatsDTO dto = new GrowthStatsDTO();

        // Nouveaux inscrits par jour
        List<Object[]> rawNewUsers = userRepository.countNewUsersPerDay(fromDt, toDt);
        dto.setNewUsersPerDay(toTimeSeries(rawNewUsers));
        dto.setTotalNewUsers(userRepository.countByCreatedAtBetween(fromDt, toDt));

        // Rétention : on regarde les users inscrits il y a X jours qui se sont
        // reconnectés
        long totalUsers = userRepository.count();
        if (totalUsers > 0) {
            // Rétention J+1 : inscrits il y a 2-8 jours, connectés au moins 1 jour après
            LocalDateTime regFrom1 = now.minusDays(DAYS_RETENTION_D1_FROM);
            LocalDateTime regTo1 = now.minusDays(DAYS_RETENTION_D1_TO);
            long registered1 = userRepository.countByCreatedAtBetween(regFrom1, regTo1);
            long retained1 = userRepository.countRetainedUsers(regFrom1, regTo1, regFrom1.plusDays(1));
            dto.setRetentionD1(registered1 > 0 ? toPercentage(retained1, registered1) : 0);

            // Rétention J+7
            LocalDateTime regFrom7 = now.minusDays(DAYS_RETENTION_D7_FROM);
            LocalDateTime regTo7 = now.minusDays(DAYS_RETENTION_D1_FROM);
            long registered7 = userRepository.countByCreatedAtBetween(regFrom7, regTo7);
            long retained7 = userRepository.countRetainedUsers(regFrom7, regTo7, regFrom7.plusDays(DAYS_ACTIVE_WEEK));
            dto.setRetentionD7(registered7 > 0 ? toPercentage(retained7, registered7) : 0);

            // Rétention J+30
            LocalDateTime regFrom30 = now.minusDays(DAYS_RETENTION_D30_FROM);
            LocalDateTime regTo30 = now.minusDays(DAYS_RETENTION_D30_TO);
            long registered30 = userRepository.countByCreatedAtBetween(regFrom30, regTo30);
            long retained30 = userRepository.countRetainedUsers(regFrom30, regTo30,
                    regFrom30.plusDays(DAYS_ACTIVE_MONTH));
            dto.setRetentionD30(registered30 > 0 ? toPercentage(retained30, registered30) : 0);
        }

        // Churn
        LocalDateTime churnDate = now.minusDays(DAYS_CHURN_THRESHOLD);
        dto.setChurnedUsers(userRepository.countChurnedUsers(churnDate));
        dto.setChurnRate(totalUsers > 0 ? toPercentage(dto.getChurnedUsers(), totalUsers) : 0);

        // DAU / WAU / MAU
        dto.setDau(userRepository.countByLastLoginAtAfter(now.minusDays(1)));
        dto.setWau(userRepository.countByLastLoginAtAfter(now.minusDays(DAYS_ACTIVE_WEEK)));
        dto.setMau(userRepository.countByLastLoginAtAfter(now.minusDays(DAYS_ACTIVE_MONTH)));

        return ResponseEntity.ok(dto);
    }

    // ==================== SALOONS & ENGAGEMENT ====================

    @GetMapping("/engagement")
    public ResponseEntity<SaloonEngagementStatsDTO> getEngagementStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = to.atTime(LocalTime.MAX);

        SaloonEngagementStatsDTO dto = new SaloonEngagementStatsDTO();

        // Entrées par jour
        List<Object[]> rawEntries = saloonSessionRepository.countEntriesPerDay(fromDt, toDt);
        dto.setEntriesPerDay(toTimeSeries(rawEntries));
        dto.setTotalEntries(saloonSessionRepository.countEntriesBetween(fromDt, toDt));
        dto.setUniqueVisitors(saloonSessionRepository.countUniqueVisitorsBetween(fromDt, toDt));
        dto.setAvgSaloonsPerUser(saloonSessionRepository.avgSaloonsPerUser(fromDt, toDt));

        // Top saloons
        List<Object[]> rawTopSaloons = saloonSessionRepository.topSaloonsByEntries(fromDt, toDt);
        dto.setTopSaloons(rawTopSaloons.stream()
                .limit(TOP_LIMIT)
                .map(row -> new RankedItem((Long) row[0], (String) row[1], (Long) row[2]))
                .collect(Collectors.toList()));

        // Unique visitors per saloon
        List<Object[]> rawUniquePerSaloon = saloonSessionRepository.uniqueVisitorsPerSaloon(fromDt, toDt);
        dto.setUniqueVisitorsPerSaloon(rawUniquePerSaloon.stream()
                .limit(TOP_LIMIT)
                .map(row -> new RankedItem((Long) row[0], (String) row[1], (Long) row[2]))
                .collect(Collectors.toList()));

        // Entrées par ville
        List<Object[]> rawByCity = saloonSessionRepository.countEntriesByCity(fromDt, toDt);
        dto.setEntriesByCity(rawByCity.stream()
                .map(row -> new RankedItem((String) row[0], (Long) row[1]))
                .collect(Collectors.toList()));

        return ResponseEntity.ok(dto);
    }

    // ==================== MATCH & CHAT ====================

    @GetMapping("/match-chat")
    public ResponseEntity<MatchChatStatsDTO> getMatchChatStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = to.atTime(LocalTime.MAX);

        MatchChatStatsDTO dto = new MatchChatStatsDTO();

        // Matchs
        List<Object[]> rawMatches = matchRepository.countMatchesPerDay(fromDt, toDt);
        dto.setMatchesPerDay(toTimeSeries(rawMatches));
        dto.setTotalMatches(matchRepository.countMatchesBetween(fromDt, toDt));

        // Conversations
        dto.setConversationsStarted(conversationRepository.countConversationsStartedBetween(fromDt, toDt));

        // Messages
        dto.setTotalMessages(messageRepository.countMessagesBetween(fromDt, toDt));
        dto.setAvgMessagesPerConversation(messageRepository.avgMessagesPerConversation(fromDt, toDt));

        // Heart requests
        dto.setHeartRequestsSent(heartRequestRepository.countHeartRequestsBetween(fromDt, toDt));
        List<Object[]> rawHeartReqs = heartRequestRepository.countHeartRequestsPerDay(fromDt, toDt);
        dto.setHeartRequestsPerDay(toTimeSeries(rawHeartReqs));

        // Match rate per entry
        long totalEntries = saloonSessionRepository.countEntriesBetween(fromDt, toDt);
        dto.setMatchRatePerEntry(totalEntries > 0
                ? toPercentage(dto.getTotalMatches(), totalEntries)
                : 0);

        return ResponseEntity.ok(dto);
    }

    // ==================== PREMIUM ====================

    @GetMapping("/premium")
    public ResponseEntity<PremiumStatsDTO> getPremiumStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = to.atTime(LocalTime.MAX);

        PremiumStatsDTO dto = new PremiumStatsDTO();

        dto.setTotalPremium(userRepository.countByIsPremiumTrue());
        dto.setNewSubscriptions(premiumSubscriptionRepository.countNewSubscriptionsBetween(fromDt, toDt));

        // Conversion rate
        long totalUsers = userRepository.count();
        dto.setConversionRate(totalUsers > 0
                ? toPercentage(dto.getTotalPremium(), totalUsers)
                : 0);

        // Premium by city
        List<Object[]> rawByCity = userRepository.countPremiumByCity();
        dto.setPremiumByCity(rawByCity.stream()
                .map(row -> new RankedItem((String) row[0], (Long) row[1]))
                .collect(Collectors.toList()));

        // Subscriptions per day
        List<Object[]> rawPerDay = premiumSubscriptionRepository.countNewSubscriptionsPerDay(fromDt, toDt);
        dto.setSubscriptionsPerDay(toTimeSeries(rawPerDay));

        return ResponseEntity.ok(dto);
    }

    // ==================== GEOGRAPHIE / PICS ====================

    @GetMapping("/geography")
    public ResponseEntity<GeographyStatsDTO> getGeographyStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = to.atTime(LocalTime.MAX);

        GeographyStatsDTO dto = new GeographyStatsDTO();

        // Entrées par ville
        List<Object[]> rawByCity = saloonSessionRepository.countEntriesByCity(fromDt, toDt);
        dto.setEntriesByCity(rawByCity.stream()
                .map(row -> new RankedItem((String) row[0], (Long) row[1]))
                .collect(Collectors.toList()));

        // Heatmap jour x heure
        List<Object[]> rawHeatmap = saloonSessionRepository.countEntriesByDayAndHour(fromDt, toDt);
        List<HeatmapPoint> heatmap = new ArrayList<>();
        for (Object[] row : rawHeatmap) {
            // MySQL DAYOFWEEK : 1=dimanche, 2=lundi... On convertit en 1=lundi..7=dimanche
            int mysqlDow = ((Number) row[0]).intValue();
            int isoDow = mysqlDow == MYSQL_SUNDAY ? DAYS_ISO_SUNDAY : mysqlDow - 1;
            int hour = ((Number) row[1]).intValue();
            long count = ((Number) row[2]).longValue();
            heatmap.add(new HeatmapPoint(isoDow, hour, count));
        }
        dto.setActivityHeatmap(heatmap);

        // Villes couvertes
        List<Saloon> allSaloons = saloonRepository.findAll();
        Set<String> cities = allSaloons.stream()
                .filter(s -> s.getCity() != null && !s.getCity().isEmpty())
                .map(Saloon::getCity)
                .collect(Collectors.toSet());
        dto.setTotalCities(cities.size());

        return ResponseEntity.ok(dto);
    }

    // ==================== FUNNEL ====================

    @GetMapping("/funnel")
    public ResponseEntity<FunnelStatsDTO> getFunnelStats() {
        FunnelStatsDTO dto = new FunnelStatsDTO();

        long totalRegistered = userRepository.count();
        dto.setTotalRegistered(totalRegistered);
        dto.setProfileCompleted(userRepository.countProfileComplete());

        // Utilisateurs qui ont au moins une session saloon
        long neverInSaloon = userRepository.countUsersNeverInSaloon();
        long enteredSaloon = totalRegistered - neverInSaloon;
        dto.setEnteredSaloon(enteredSaloon);

        // Utilisateurs qui ont au moins un match
        long matched = matchRepository.count();
        dto.setMatched(matched > 0 ? matched : 0);

        // Conversations
        dto.setConversationStarted(conversationRepository.count());

        // Heart requests
        dto.setHeartRequestSent(heartRequestRepository.count());

        // % never entered saloon
        dto.setPctNeverEnteredSaloon(totalRegistered > 0
                ? toPercentage(neverInSaloon, totalRegistered)
                : 0);

        // % entered but never matched (approximation)
        dto.setPctEnteredNoMatch(enteredSaloon > 0
                ? toPercentage(enteredSaloon - Math.min(matched, enteredSaloon), enteredSaloon)
                : 0);

        return ResponseEntity.ok(dto);
    }

    // ==================== HELPERS ====================

    private static double toPercentage(long numerator, long denominator) {
        return Math.round((double) numerator / denominator * PERCENT_MULTIPLIER) / PERCENT_DIVISOR;
    }

    private List<TimeSeriesPoint> toTimeSeries(List<Object[]> raw) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return raw.stream()
                .map(row -> {
                    String date;
                    if (row[0] instanceof java.sql.Date) {
                        date = ((java.sql.Date) row[0]).toLocalDate().format(fmt);
                    } else if (row[0] instanceof LocalDate) {
                        date = ((LocalDate) row[0]).format(fmt);
                    } else {
                        date = row[0].toString();
                    }
                    long value = ((Number) row[1]).longValue();
                    return new TimeSeriesPoint(date, value);
                })
                .collect(Collectors.toList());
    }
}

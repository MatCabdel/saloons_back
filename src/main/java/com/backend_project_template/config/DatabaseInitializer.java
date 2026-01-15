package com.backend_project_template.config;

import com.backend_project_template.domains.saloon.Saloon;
import com.backend_project_template.domains.saloon.SaloonRepository;
import com.backend_project_template.domains.subscription.PremiumSubscription;
import com.backend_project_template.domains.subscription.PremiumSubscriptionRepository;
import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.init-data", havingValue = "true", matchIfMissing = true)
public class DatabaseInitializer {

  // Coordonnées précises des bars
  private static final BigDecimal LAT_MAGNUS = new BigDecimal("44.83842840285206");
  private static final BigDecimal LNG_MAGNUS = new BigDecimal("-0.575610564976734");
  private static final BigDecimal LAT_ENGRENAGE = new BigDecimal("44.84115720658105");
  private static final BigDecimal LNG_ENGRENAGE = new BigDecimal("-0.581747002585448");
  private static final BigDecimal LAT_MINOUCHE = new BigDecimal("44.838817483569436");
  private static final BigDecimal LNG_MINOUCHE = new BigDecimal("-0.5789271928216408");
  private static final BigDecimal LAT_SHERLOCK = new BigDecimal("44.841514754939915");
  private static final BigDecimal LNG_SHERLOCK = new BigDecimal("-0.5818113755995328");
  private static final BigDecimal LAT_VINTAGE = new BigDecimal("44.83909795210986");
  private static final BigDecimal LNG_VINTAGE = new BigDecimal("-0.5683563973081616");

  private static final int YEAR_U1 = 1990;
  private static final int MONTH_U1 = 5;
  private static final int DAY_U1 = 13;
  private static final int YEAR_U2 = 1992;
  private static final int MONTH_U2 = 5;
  private static final int DAY_U2 = 13;
  private static final int YEAR_U3 = 1993;
  private static final int MONTH_U3 = 5;
  private static final int DAY_U3 = 13;
  private static final int MIN_USERS = 0;
  private static final int MIN_SALOONS = 0;
  private static final int INDEX_0 = 0;
  private static final int INDEX_1 = 1;
  private static final int INDEX_2 = 2;
  private static final int PREMIUM_MONTHS_U1 = 3;
  private static final int PREMIUM_MONTHS_U2 = 1;
  private static final int INACTIVE_WEEKS = 4;

  private final UserRepository userRepository;
  private final SaloonRepository saloonRepository;
  private final PremiumSubscriptionRepository premiumSubscriptionRepository;

  @Value("${app.base-url:http://localhost:8080}")
  private String baseUrl;

  public DatabaseInitializer(UserRepository userRepository, SaloonRepository saloonRepository,
      PremiumSubscriptionRepository premiumSubscriptionRepository) {
    this.userRepository = userRepository;
    this.saloonRepository = saloonRepository;
    this.premiumSubscriptionRepository = premiumSubscriptionRepository;
  }

  @Bean
  CommandLineRunner init() {
    return args -> {
      if (userRepository.count() == MIN_USERS) {
        User u1 = new User();
        u1.setUserName("Pilou");
        u1.setEmail("test1@gmail.com");
        u1.setPassword("Motdepasse1");
        u1.setImgUrl(baseUrl + "/images/piloubond.jpg");
        u1.setBirthDate(LocalDate.of(YEAR_U1, MONTH_U1, DAY_U1));
        u1.setIsPremium(true);
        u1.setPremiumStartDate(LocalDateTime.now().minusMonths(PREMIUM_MONTHS_U1));
        u1.setLastLoginAt(LocalDateTime.now());

        User u2 = new User();
        u2.setUserName("JamesBond Girl");
        u2.setEmail("admin1@gmail.com");
        u2.setPassword("Motdepasse1");
        u2.setImgUrl(baseUrl + "/images/NicoBondgirl.jpg");
        u2.setBirthDate(LocalDate.of(YEAR_U2, MONTH_U2, DAY_U2));
        u2.setIsPremium(true);
        u2.setPremiumStartDate(LocalDateTime.now().minusMonths(PREMIUM_MONTHS_U2));
        u2.setLastLoginAt(LocalDateTime.now());

        User u3 = new User();
        u3.setUserName("Julien");
        u3.setEmail("admin1@gmail.com");
        u3.setPassword("Motdepasse1");
        u3.setImgUrl(baseUrl + "/images/Julien.jpg");
        u3.setBirthDate(LocalDate.of(YEAR_U3, MONTH_U3, DAY_U3));
        u3.setLastLoginAt(LocalDateTime.now().minusWeeks(INACTIVE_WEEKS)); // Inactif (> 3 semaines)

        userRepository.saveAll(List.of(u1, u2, u3));

        // Créer les abonnements premium pour le graphique
        PremiumSubscription sub1 = new PremiumSubscription();
        sub1.setUser(u1);
        sub1.setStartDate(LocalDateTime.now().minusMonths(PREMIUM_MONTHS_U1));
        sub1.setIsActive(true);
        sub1.setSubscriptionType("monthly");

        PremiumSubscription sub2 = new PremiumSubscription();
        sub2.setUser(u2);
        sub2.setStartDate(LocalDateTime.now().minusMonths(PREMIUM_MONTHS_U2));
        sub2.setIsActive(true);
        sub2.setSubscriptionType("monthly");

        premiumSubscriptionRepository.saveAll(List.of(sub1, sub2));
      }

      // Créer des abonnements si la table est vide (pour les bases existantes)
      if (premiumSubscriptionRepository.count() == 0 && userRepository.count() > 0) {
        List<User> existingUsers = userRepository.findAll();
        if (existingUsers.size() >= 2) {
          User u1 = existingUsers.get(INDEX_0);
          User u2 = existingUsers.get(INDEX_1);

          // Mettre à jour les users existants
          u1.setIsPremium(true);
          u1.setPremiumStartDate(LocalDateTime.now().minusMonths(PREMIUM_MONTHS_U1));
          u1.setLastLoginAt(LocalDateTime.now());
          userRepository.save(u1);

          u2.setIsPremium(true);
          u2.setPremiumStartDate(LocalDateTime.now().minusMonths(PREMIUM_MONTHS_U2));
          u2.setLastLoginAt(LocalDateTime.now());
          userRepository.save(u2);

          // Créer les abonnements
          PremiumSubscription sub1 = new PremiumSubscription();
          sub1.setUser(u1);
          sub1.setStartDate(LocalDateTime.now().minusMonths(PREMIUM_MONTHS_U1));
          sub1.setIsActive(true);
          sub1.setSubscriptionType("monthly");

          PremiumSubscription sub2 = new PremiumSubscription();
          sub2.setUser(u2);
          sub2.setStartDate(LocalDateTime.now().minusMonths(PREMIUM_MONTHS_U2));
          sub2.setIsActive(true);
          sub2.setSubscriptionType("monthly");

          premiumSubscriptionRepository.saveAll(List.of(sub1, sub2));
        }
      }

      // Initialisation des salons
      if (saloonRepository.count() == MIN_SALOONS) {
        Saloon magnus = new Saloon();
        magnus.setName("Le Magnus");
        magnus.setImgUrl(baseUrl + "/images/magnus.jpg");
        magnus.setLatitude(LAT_MAGNUS);
        magnus.setLongitude(LNG_MAGNUS);
        magnus.setAddress("32 Rue de Cheverus, Bordeaux");
        magnus.setCity("Bordeaux");
        magnus.setCreatedAt(LocalDateTime.now());

        Saloon engrenage = new Saloon();
        engrenage.setName("L'Engrenage");
        engrenage.setImgUrl(baseUrl + "/images/engrenage.webp");
        engrenage.setLatitude(LAT_ENGRENAGE);
        engrenage.setLongitude(LNG_ENGRENAGE);
        engrenage.setAddress("14 Rue du Parlement Sainte-Catherine, Bordeaux");
        engrenage.setCity("Bordeaux");
        engrenage.setCreatedAt(LocalDateTime.now());

        Saloon minouche = new Saloon();
        minouche.setName("Le Minouche");
        minouche.setImgUrl(baseUrl + "/images/minouche.jpg");
        minouche.setLatitude(LAT_MINOUCHE);
        minouche.setLongitude(LNG_MINOUCHE);
        minouche.setAddress("64 Rue des Remparts, Bordeaux");
        minouche.setCity("Bordeaux");
        minouche.setCreatedAt(LocalDateTime.now());

        Saloon sherlock = new Saloon();
        sherlock.setName("Le Sherlock");
        sherlock.setImgUrl(baseUrl + "/images/sherlock.jpg");
        sherlock.setLatitude(LAT_SHERLOCK);
        sherlock.setLongitude(LNG_SHERLOCK);
        sherlock.setAddress("100 Rue des Remparts, Bordeaux");
        sherlock.setCity("Bordeaux");
        sherlock.setCreatedAt(LocalDateTime.now());

        Saloon vintage = new Saloon();
        vintage.setName("Le Vintage Café");
        vintage.setImgUrl(baseUrl + "/images/vintage.webp");
        vintage.setLatitude(LAT_VINTAGE);
        vintage.setLongitude(LNG_VINTAGE);
        vintage.setAddress("137 Rue des Remparts, Bordeaux");
        vintage.setCity("Bordeaux");
        vintage.setCreatedAt(LocalDateTime.now());

        saloonRepository.saveAll(List.of(magnus, engrenage, minouche, sherlock, vintage));
      }

      Saloon engrenage = saloonRepository.findAll().stream().filter(s -> "L'Engrenage".equals(s.getName())).findFirst()
          .orElse(null);
      Saloon sherlock = saloonRepository.findAll().stream().filter(s -> "Le Sherlock".equals(s.getName())).findFirst()
          .orElse(null);

      List<User> users = userRepository.findAll();
      if (users.size() >= MIN_USERS && engrenage != null && sherlock != null) {
        users.get(INDEX_0).setCurrentSaloon(engrenage);
        users.get(INDEX_1).setCurrentSaloon(engrenage);
        users.get(INDEX_2).setCurrentSaloon(sherlock);
        userRepository.saveAll(users);
      }
    };
  }
}

package com.backend_project_template.config;

import com.backend_project_template.domains.saloon.Saloon;
import com.backend_project_template.domains.saloon.SaloonRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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

  private static final int MIN_SALOONS = 0;

  private final SaloonRepository saloonRepository;

  @Value("${app.base-url:http://localhost:8080}")
  private String baseUrl;

  public DatabaseInitializer(SaloonRepository saloonRepository) {
    this.saloonRepository = saloonRepository;
  }

  @Bean
  CommandLineRunner init() {
    return args -> {
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

        saloonRepository.saveAll(java.util.List.of(magnus, engrenage, minouche, sherlock, vintage));
      }
    };
  }
}

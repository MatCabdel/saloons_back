package com.backend_project_template.config;

import com.backend_project_template.Entity.User;
import com.backend_project_template.demo.DemoEntity;
import com.backend_project_template.demo.DemoRepository;
import com.backend_project_template.domains.saloon.Saloon;
import com.backend_project_template.domains.saloon.SaloonRepository;
import com.backend_project_template.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseInitializer {

  private final DemoRepository demoRepository;
  private final UserRepository userRepository;
  private final SaloonRepository saloonRepository;

  public DatabaseInitializer(DemoRepository demoRepository, UserRepository userRepository, SaloonRepository saloonRepository) {
    this.demoRepository = demoRepository;
    this.userRepository = userRepository;
    this.saloonRepository = saloonRepository;
  }

  @Bean
  CommandLineRunner init() {
    return args -> {
      List.of(new DemoEntity("Hello"), new DemoEntity("Bonjour"), new DemoEntity("Sabaidi"), new DemoEntity("Ia ora na")).forEach(
        demoRepository::save
      );

      if (userRepository.count() == 0) {
        User u1 = new User();
        u1.setUserName("Mat");
        u1.setEmail("test1@gmail.com");
        u1.setPassword("Motdepasse1");

        User u2 = new User();
        u2.setUserName("Admin");
        u2.setEmail("admin1@gmail.com");
        u2.setPassword("Motdepasse1");

        userRepository.saveAll(List.of(u1, u2));
      }

      if (saloonRepository.count() == 0) {
        com.backend_project_template.domains.saloon.Saloon s1 = new Saloon();
        s1.setName("L'engrenage");
        s1.setImgUrl("/images/saloons/engrenage.webp");
        s1.setLatitude(new BigDecimal("48.8566"));
        s1.setLongitude(new BigDecimal("2.3522"));
        s1.setAddress("123 rue des remparts, Bordeaux");
        s1.setCreatedAt(LocalDateTime.now());

        saloonRepository.save(s1);
      }

      if (saloonRepository.count() == 0) {
        com.backend_project_template.domains.saloon.Saloon s1 = new Saloon();
        s1.setName("Le Sherlock");
        s1.setImgUrl("/images/saloons/sherlock.jpg");
        s1.setLatitude(new BigDecimal("48.8566"));
        s1.setLongitude(new BigDecimal("2.3522"));
        s1.setAddress("100 rue des remparts, Bordeaux");
        s1.setCreatedAt(LocalDateTime.now());

        saloonRepository.save(s1);
      }

      if (saloonRepository.count() == 0) {
        com.backend_project_template.domains.saloon.Saloon s1 = new Saloon();
        s1.setName("Le vintage Café");
        s1.setImgUrl("/images/saloons/vintage.webp");
        s1.setLatitude(new BigDecimal("48.8566"));
        s1.setLongitude(new BigDecimal("2.3522"));
        s1.setAddress("137 rue des remparts, Bordeaux");
        s1.setCreatedAt(LocalDateTime.now());

        saloonRepository.save(s1);
      }
    };
  }
}

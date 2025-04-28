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

  // Constantes pour remplacer les magic numbers
  private static final BigDecimal LAT_ENGRENAGE = BigDecimal.valueOf(44.841162);
  private static final BigDecimal LNG_ENGRENAGE = BigDecimal.valueOf(-0.58192);
  private static final BigDecimal LAT_SHERLOCK = BigDecimal.valueOf(44.838357);
  private static final BigDecimal LNG_SHERLOCK = BigDecimal.valueOf(-0.575559);
  private static final BigDecimal LAT_VINTAGE_CAFE = BigDecimal.valueOf(44.838929);
  private static final BigDecimal LNG_VINTAGE_CAFE = BigDecimal.valueOf(-0.568325);

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
      // Insertion de données de démonstration
      List.of(new DemoEntity("Hello"), new DemoEntity("Bonjour"), new DemoEntity("Sabaidi"), new DemoEntity("Ia ora na")).forEach(
              demoRepository::save
      );

      // Initialisation des utilisateurs
      if (userRepository.count() == 0) {
        User u1 = new User();
        u1.setUserName("Pilou");
        u1.setEmail("test1@gmail.com");
        u1.setPassword("Motdepasse1");
      //  u1.setImgUrl("http://localhost:8080/images/piloubond.jpg");

        User u2 = new User();
        u2.setUserName("JamesBond Girl");
        u2.setEmail("admin1@gmail.com");
        u2.setPassword("Motdepasse1");
      // u2.setImgUrl("http://localhost:8080/images/NicoBondgirl.jpg");

        User u3 = new User();
        u3.setUserName("Julien");
        u3.setEmail("admin1@gmail.com");
        u3.setPassword("Motdepasse1");
      //  u3.setImgUrl("http://localhost:8080/images/Julien.jpg");

        userRepository.saveAll(List.of(u1, u2, u3));
      }

      // Initialisation des salons
      if (saloonRepository.count() == 0) {
        Saloon s1 = new Saloon();
        s1.setName("L'engrenage");
        s1.setImgUrl("http://localhost:8080/images/engrenage.webp");
        s1.setLatitude(LAT_ENGRENAGE);
        s1.setLongitude(LNG_ENGRENAGE);
        s1.setAddress("123 rue des remparts, Bordeaux");
        s1.setCreatedAt(LocalDateTime.now());

        Saloon s2 = new Saloon();
        s2.setName("Le Sherlock");
        s2.setImgUrl("http://localhost:8080/images/sherlock.jpg");
        s2.setLatitude(LAT_SHERLOCK);
        s2.setLongitude(LNG_SHERLOCK);
        s2.setAddress("100 rue des remparts, Bordeaux");
        s2.setCreatedAt(LocalDateTime.now());

        Saloon s3 = new Saloon();
        s3.setName("Le Vintage Café");
        s3.setImgUrl("http://localhost:8080/images/vintage.webp");
        s3.setLatitude(LAT_VINTAGE_CAFE);
        s3.setLongitude(LNG_VINTAGE_CAFE);
        s3.setAddress("137 rue des remparts, Bordeaux");
        s3.setCreatedAt(LocalDateTime.now());

        saloonRepository.saveAll(List.of(s1, s2, s3));
      }

      // Assigner tous les utilisateurs au salon "L'engrenage"
      // Saloon engrenage = saloonRepository.findByName("L'engrenage").orElseThrow();
     //  List<User> users = userRepository.findAll();
     // for (User user : users) {
      //  user.setCurrentSaloon(engrenage);
     // }
    //  userRepository.saveAll(users);
    };
  }
}
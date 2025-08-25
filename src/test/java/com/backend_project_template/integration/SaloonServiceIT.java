package com.backend_project_template.integration;

import com.backend_project_template.domains.saloon.Saloon;
import com.backend_project_template.domains.saloon.SaloonRepository;
import com.backend_project_template.domains.saloon.SaloonService;
import com.backend_project_template.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class SaloonServiceIT extends AbstractIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SaloonService saloonService;

    @Autowired
    private SaloonRepository saloonRepository;

    @Test
    void shouldReturnAllSaloons() {
        userRepository.deleteAll();
        saloonRepository.deleteAll();
        Saloon s1 = new Saloon();
        s1.setName("Magnus");
        s1.setCreatedAt(java.time.LocalDateTime.now());
        s1.setImgUrl("https://example.com/magnus.jpg");
        s1.setAddress("1 rue du Magnus, Bordeaux");
        s1.setLatitude(BigDecimal.valueOf(44.84));
        s1.setLongitude(BigDecimal.valueOf(-0.57));
        s1.setVisitorNumber(10);
        saloonRepository.save(s1);

        Saloon s2 = new Saloon();
        s2.setName("Connemara");
        s2.setCreatedAt(java.time.LocalDateTime.now());
        s2.setImgUrl("https://example.com/connemara.jpg");
        s2.setAddress("2 rue du Connemara, Bordeaux");
        s2.setLatitude(BigDecimal.valueOf(44.85));
        s2.setLongitude(BigDecimal.valueOf(-0.58));
        s2.setVisitorNumber(15);
        saloonRepository.save(s2);

        List<Saloon> saloons = saloonService.getAllSaloons();

        assertEquals(2, saloons.size());
        assertTrue(saloons.stream().anyMatch(s -> "Magnus".equals(s.getName())));
        assertTrue(saloons.stream().anyMatch(s -> "Connemara".equals(s.getName())));
    }
}

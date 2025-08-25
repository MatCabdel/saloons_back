package com.backend_project_template.e2e;

import com.backend_project_template.domains.saloon.Saloon;
import com.backend_project_template.domains.saloon.SaloonRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SaloonE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SaloonRepository saloonRepository;

    @BeforeEach
    void setupTestData() {
        // Créer des données de test
        Saloon testSaloon = new Saloon();
        testSaloon.setName("Test Saloon");
        testSaloon.setAddress("123 Test Street");
        testSaloon.setLatitude(BigDecimal.valueOf(44.841162));
        testSaloon.setLongitude(BigDecimal.valueOf(-0.58192));
        testSaloon.setImgUrl("http://test.com/image.jpg");
        testSaloon.setVisitorNumber(10);
        testSaloon.setCreatedAt(LocalDateTime.now()); // ← AJOUTÉ !
        
        saloonRepository.save(testSaloon);
    }

    @Test
    @DisplayName("E2E - Test du repository")
    void shouldSaveAndRetrieveSaloons() {
        // Test direct du repository (sans passer par l'API)
        long count = saloonRepository.count();
        
        assertThat(count).isGreaterThan(0);
    }
}
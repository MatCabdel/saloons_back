package com.backend_project_template.e2e;

import com.backend_project_template.domains.saloon.SaloonDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SaloonE2ETest {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void shouldRetrieveSaloonById() {
        ResponseEntity<SaloonDTO> response = restTemplate.getForEntity("/saloon/1", SaloonDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        SaloonDTO saloon = response.getBody();
        assertNotNull(saloon);
        assertEquals(1L, saloon.getId());
        assertNotNull(saloon.getName());
        assertNotNull(saloon.getImgUrl());
    }
}

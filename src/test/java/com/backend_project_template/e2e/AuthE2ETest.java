package com.backend_project_template.e2e;

import com.backend_project_template.dto.UserRegistrationResponseDTO;
import com.backend_project_template.integration.AbstractIT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthE2ETest extends AbstractIT {
    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void shouldRegisterUserWithImage() {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("email", "e2euser@example.com");
        body.add("password", "e2epassword");
        body.add("username", "E2EUser");

        ByteArrayResource imageResource = new ByteArrayResource("fake-image-content".getBytes()) {
            @Override
            public String getFilename() {
                return "profile.jpg";
            }
        };
        body.add("image", imageResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<UserRegistrationResponseDTO> response = restTemplate.postForEntity(
                "/auth/register", requestEntity, UserRegistrationResponseDTO.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        UserRegistrationResponseDTO user = response.getBody();
        assertNotNull(user);
        assertEquals("e2euser@example.com", user.getEmail());
        assertEquals("E2EUser", user.getUsername());
        assertNotNull(user.getId());
    }
}

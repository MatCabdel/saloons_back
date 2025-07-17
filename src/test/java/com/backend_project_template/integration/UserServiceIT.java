package com.backend_project_template.integration;

import com.backend_project_template.Entity.User;
import com.backend_project_template.dto.UserRegistrationDTO;
import com.backend_project_template.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
public class UserServiceIT extends AbstractIT {

    @Autowired
    UserService userService;

    @Test
    void shouldCreateAndRetrieveUser() {
        UserRegistrationDTO dto = new UserRegistrationDTO();
        dto.setEmail("alice@example.com");
        dto.setPassword("password");
        dto.setUsername("Alice");
        Set<String> roles = Set.of("USER");

    userService.registerUser(dto, roles);

        User result = userService.findByEmail(dto.getEmail());

        assertEquals(dto.getEmail(), result.getEmail());
        assertTrue(result.getRoles().contains("USER"));
        assertEquals(dto.getUsername(), result.getUserName());
    }
}


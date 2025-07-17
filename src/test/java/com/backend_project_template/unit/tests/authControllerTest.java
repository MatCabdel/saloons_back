package com.backend_project_template.unit.tests;

import com.backend_project_template.Entity.User;
import com.backend_project_template.controllers.AuthController;
import com.backend_project_template.dto.UserRegistrationDTO;
import com.backend_project_template.dto.UserRegistrationResponseDTO;
import com.backend_project_template.security.AuthenticationService;
import com.backend_project_template.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class authControllerTest {

    UserService userService = mock(UserService.class);
    AuthenticationService authenticationService = mock(AuthenticationService.class);
    AuthController controller = new AuthController(userService, authenticationService);
    UserRegistrationDTO dto = new UserRegistrationDTO();

    @Test
    void shouldRegisterUserAndReturnCreated() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@mail.com");
        user.setUserName("testuser");
        when(userService.registerUserWithImage(dto, Set.of("ROLE_USER"))).thenReturn(user);

        ResponseEntity<UserRegistrationResponseDTO> response = controller.register(dto);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals(user.getId(), response.getBody().getId());
        assertEquals(user.getEmail(), response.getBody().getEmail());
        assertEquals(user.getUserName(), response.getBody().getUsername());
        verify(userService).registerUserWithImage(dto, Set.of("ROLE_USER"));
    }
}

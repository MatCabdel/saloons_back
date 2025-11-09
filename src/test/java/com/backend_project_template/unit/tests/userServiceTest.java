package com.backend_project_template.unit.tests;

import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserService;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class userServiceTest {
    @Test
    void shouldReturnUserWhenEmailExists() {
        UserService userService = mock(UserService.class);
        User user = new User();
        user.setEmail("test@mail.com");
        when(userService.findByEmail("test@mail.com")).thenReturn(user);

        User result = userService.findByEmail("test@mail.com");

        assertNotNull(result);
        assertEquals("test@mail.com", result.getEmail());
        verify(userService).findByEmail("test@mail.com");
    }
}

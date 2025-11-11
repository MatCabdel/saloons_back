package com.backend_project_template.unit.tests;

import com.backend_project_template.domains.user.User;
import com.backend_project_template.domains.user.UserService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

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
    private final UserService userService = new UserService();
    @Test
    void calculateAge_shouldReturnCorrectYears() {
        LocalDate birthDate = LocalDate.of(1990, 6, 15);
        int age = userService.calculateAge(birthDate);
        int expected = LocalDate.now().getYear() - 1990;
        // On vérifie l'approximation par années (la date précise dépend du jour/mois)
        assertTrue(age == expected || age == expected - 1, "Age should match expected years");
    }

    @Test
    void calculateAge_withNull_shouldReturnZero() {
        assertEquals(0, userService.calculateAge(null));
    }
}

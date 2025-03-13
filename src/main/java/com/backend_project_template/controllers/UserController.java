package com.backend_project_template.controllers;

import com.backend_project_template.Entity.User;
import com.backend_project_template.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/profile")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{email}")
    public ResponseEntity<User> getUserProfile(@PathVariable String email, @AuthenticationPrincipal UserDetails userDetails) {
        if(!Objects.equals(userDetails.getUsername(), email)) {
            throw new AccessDeniedException("Access denied");
        }
        System.out.println(userDetails.getUsername());
        User user = userService.findByEmail(email);
        return ResponseEntity.ok(user);
    }
}

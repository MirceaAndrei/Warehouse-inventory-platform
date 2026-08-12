package com.inventory.system.controller;

import com.inventory.system.dto.*;
import com.inventory.system.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            System.out.println("[OK] User logged in: " + request.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("[ERROR] Login failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            String username = authentication.getName();
            UserResponse user = authService.getCurrentUser(username);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            System.out.println("[ERROR] Get current user failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request, 
                                           Authentication authentication) {
        try {
            String username = authentication.getName();
            authService.changePassword(username, request);
            System.out.println("[OK] Password changed for user: " + username);
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (Exception e) {
            System.out.println("[ERROR] Change password failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody CreateUserRequest request) {
        try {
            
            if (request.getRole() == null) {
                request.setRole(com.inventory.system.domain.Role.EMPLOYEE);
            }
            
            
            if (request.getRole() != com.inventory.system.domain.Role.EMPLOYEE) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Doar conturi EMPLOYEE pot fi create prin register"));
            }
            
            UserResponse user = authService.createUser(request);
            System.out.println("[OK] New user registered: " + user.getUsername());
            return ResponseEntity.ok(Map.of(
                "message", "Cont creat cu succes! Te poți autentifica acum.",
                "user", user
            ));
        } catch (Exception e) {
            System.out.println("[ERROR] Registration failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

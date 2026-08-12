package com.inventory.system.controller;

import com.inventory.system.dto.*;
import com.inventory.system.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')") 
public class UserController {
    
    @Autowired
    private AuthService authService;
    
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = authService.getAllUsers();
        System.out.println("[OK] Retrieved " + users.size() + " users");
        return ResponseEntity.ok(users);
    }
    
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        try {
            UserResponse user = authService.createUser(request);
            System.out.println("[OK] User created: " + user.getUsername());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            System.out.println("[ERROR] Create user failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, 
                                       @RequestBody CreateUserRequest request) {
        try {
            UserResponse user = authService.updateUser(id, request);
            System.out.println("[OK] User updated: " + user.getUsername());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            System.out.println("[ERROR] Update user failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            authService.deleteUser(id);
            System.out.println("[OK] User deleted with ID: " + id);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (Exception e) {
            System.out.println("[ERROR] Delete user failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id) {
        try {
            authService.resetPassword(id);
            System.out.println("[OK] Password reset for user ID: " + id);
            return ResponseEntity.ok(Map.of(
                "message", "Password reset to 'parola123@'. User must change on next login."
            ));
        } catch (Exception e) {
            System.out.println("[ERROR] Reset password failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

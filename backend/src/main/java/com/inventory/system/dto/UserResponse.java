package com.inventory.system.dto;

import com.inventory.system.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private Boolean enabled;
    private Boolean mustChangePassword;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}

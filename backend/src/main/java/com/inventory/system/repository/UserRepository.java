package com.inventory.system.repository;

import com.inventory.system.domain.User;
import com.inventory.system.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    List<User> findByRole(Role role);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}

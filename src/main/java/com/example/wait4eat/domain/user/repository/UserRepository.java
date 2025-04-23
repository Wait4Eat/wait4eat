package com.example.wait4eat.domain.user.repository;

import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.user.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Long countByLoginDate(LocalDate yesterday);

    Long countByRole(UserRole userRole);

    Long countByLoginDateAndRole(LocalDate yesterday, UserRole userRole);
}

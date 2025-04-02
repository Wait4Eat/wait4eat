package com.example.wait4eat.domain.user.repository;

import com.example.wait4eat.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}

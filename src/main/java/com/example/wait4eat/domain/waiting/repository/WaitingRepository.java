package com.example.wait4eat.domain.waiting.repository;

import com.example.wait4eat.domain.waiting.entity.Waiting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WaitingRepository extends JpaRepository<Waiting, Long>, WaitingQueryRepository {

}

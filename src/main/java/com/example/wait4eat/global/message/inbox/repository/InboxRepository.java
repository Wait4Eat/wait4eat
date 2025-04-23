package com.example.wait4eat.global.message.inbox.repository;

import com.example.wait4eat.global.message.inbox.entity.InboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InboxRepository extends JpaRepository<InboxMessage, String> {
}

package com.example.wait4eat.global.message.dedup;

import com.example.wait4eat.global.message.inbox.entity.InboxMessage;
import com.example.wait4eat.global.message.inbox.repository.InboxRepository;
import com.example.wait4eat.global.message.outbox.repository.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DbMessageDeduplicationHandler implements MessageDeduplicationHandler {

    private final OutboxMessageRepository outboxRepository;
    private final InboxRepository inboxRepository;

    @Override
    public boolean isDuplicated(String messageKey) {
        if (inboxRepository.findById(messageKey).orElse(null) == null) {
            return false;
        }
        return true;
//        return outboxRepository.existsByIdAndIsProcessed(messageKey, true);
    }


    @Transactional
    @Override
    public void markAsProcessed(String messageKey) {
        inboxRepository.save(new InboxMessage(messageKey));
//        OutboxMessage outboxMessage = outboxRepository.findById(messageKey)
//                .orElseThrow(() -> new IllegalArgumentException("outboxId: " + messageKey + " not found"));
//        outboxMessage.markAsProcessed();
    }
}
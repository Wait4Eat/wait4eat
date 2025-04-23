package com.example.wait4eat.global.message.outbox.event.handler;

import com.example.wait4eat.global.message.outbox.event.OutboxSavedEvent;
import com.example.wait4eat.global.message.outbox.service.OutboxProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxSavedEventHandler {

    private final OutboxProcessor outboxProcessor;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OutboxSavedEvent event) {
        outboxProcessor.process(event.getMessages());
    }
}

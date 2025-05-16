package com.example.wait4eat.global.util;

import java.util.UUID;

public class IdGenerator {

    private IdGenerator() { }

    public static String generateMessageId() {
        return UUID.randomUUID().toString();
    }

    public static String generateNotificationId() {
        return UUID.randomUUID().toString();
    }

}

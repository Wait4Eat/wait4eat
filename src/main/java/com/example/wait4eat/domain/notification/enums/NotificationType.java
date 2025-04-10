package com.example.wait4eat.domain.notification.enums;

public enum NotificationType {

    WAITING_CALLED("웨이팅이 호출되었습니다. 가게로 입장해주세요.");

    private final String message;

    NotificationType(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

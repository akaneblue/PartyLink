package com.hangw.model.DTO;

import java.time.LocalDateTime;

public record NotificationDto(Long id, String message, LocalDateTime createdAt, boolean isRead,  Long crewId, String crewName) {

}

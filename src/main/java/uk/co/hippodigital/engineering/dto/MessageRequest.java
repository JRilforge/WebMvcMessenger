package uk.co.hippodigital.engineering.dto;

public record MessageRequest(String fromUserId, String toUserId, String content) {
}

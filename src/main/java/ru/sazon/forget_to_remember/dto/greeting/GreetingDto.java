package ru.sazon.forget_to_remember.dto.greeting;

public record GreetingDto(Long id, String text, String mediaUrl, boolean isPublic, int likesCount, Long ownerId) {
}

package ru.sazon.forget_to_remember.dto.greeting;

import jakarta.validation.constraints.Size;

public record GreetingUpdateDto(
        @Size(min = 1, max = 1000, message = "Текст должен быть от 1 до 1000 символов")
        String text,

        @Size(max = 500, message = "URL медиа не более 500 символов")
        String mediaUrl,

        Boolean isPublic
) {}

package ru.sazon.forget_to_remember.dto.birthday;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record BirthdayUpdateDto(
        @Size(min = 1, max = 255, message = "Имя от 1 до 255 символов")
        String name,

        LocalDate date,

        @Size(max = 255, message = "Контакт не более 255 символов")
        String contact
) {}

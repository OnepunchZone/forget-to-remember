package ru.sazon.forget_to_remember.dto.birthday;

import java.time.LocalDate;

public record BirthdayDto(Long id, String name, LocalDate date, String contact, Long userId) {
}

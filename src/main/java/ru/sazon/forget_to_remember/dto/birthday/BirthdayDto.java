package ru.sazon.forget_to_remember.dto.birthday;

import ru.sazon.forget_to_remember.dto.greeting.GreetingDto;

import java.time.LocalDate;

public record BirthdayDto(
        Long id,
        String name,
        LocalDate date,
        String contact,
        Long userId,
        GreetingDto greeting
) {}

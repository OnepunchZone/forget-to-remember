package ru.sazon.forget_to_remember.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRegistrationDto(
        @NotBlank(message = "Имя необходимое поле")
        String username,
        @NotBlank(message = "Пароль необходимое поле")
        String password,
        @NotBlank(message = "Email необходимое поле")
        @Email
        String email,
        String role,
        @NotBlank(message = "Contact необходимое поле")
        String contact
) {
}

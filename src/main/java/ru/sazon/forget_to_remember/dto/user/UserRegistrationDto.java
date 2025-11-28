package ru.sazon.forget_to_remember.dto.user;

public record UserRegistrationDto(String username, String password, String email, String role, String contact) {
}

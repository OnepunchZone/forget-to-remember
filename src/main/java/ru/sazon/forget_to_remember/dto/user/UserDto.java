package ru.sazon.forget_to_remember.dto.user;

import java.util.Set;

public record UserDto(Long id, String username, String email, Set<String> roles, String contact) {
}

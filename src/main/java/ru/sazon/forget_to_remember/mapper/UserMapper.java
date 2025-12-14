package ru.sazon.forget_to_remember.mapper;

import ru.sazon.forget_to_remember.dto.user.UserDto;
import ru.sazon.forget_to_remember.model.User;

public interface UserMapper {
    UserDto toDto(User user);
}

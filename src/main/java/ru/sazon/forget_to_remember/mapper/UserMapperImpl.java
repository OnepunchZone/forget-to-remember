package ru.sazon.forget_to_remember.mapper;

import org.springframework.stereotype.Component;
import ru.sazon.forget_to_remember.dto.user.UserDto;
import ru.sazon.forget_to_remember.model.User;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDto toDto(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return new UserDto(user.getId(), user.getUsername(), user.getEmail(), roles, user.getContact());
    }
}

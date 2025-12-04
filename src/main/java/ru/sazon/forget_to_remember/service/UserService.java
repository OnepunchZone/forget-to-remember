package ru.sazon.forget_to_remember.service;

import ru.sazon.forget_to_remember.dto.user.UserDto;
import ru.sazon.forget_to_remember.dto.user.UserRegistrationDto;

import java.util.List;

public interface UserService {
    UserDto getById(Long id);

    List<UserDto> findAllUsers();

    UserDto createUser(UserRegistrationDto registrationDto);

    UserDto updateUser(Long id, UserRegistrationDto updateDto);


    public void deleteUser(Long id);
}

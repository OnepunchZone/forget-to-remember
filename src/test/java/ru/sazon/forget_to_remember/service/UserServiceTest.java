package ru.sazon.forget_to_remember.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.sazon.forget_to_remember.dto.user.UserDto;
import ru.sazon.forget_to_remember.dto.user.UserRegistrationDto;
import ru.sazon.forget_to_remember.exeption.BusinessLogicException;
import ru.sazon.forget_to_remember.mapper.UserMapperImpl;
import ru.sazon.forget_to_remember.model.Role;
import ru.sazon.forget_to_remember.model.User;
import ru.sazon.forget_to_remember.repository.UserRepository;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapperImpl userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRegistrationDto registrationDto;

    @BeforeEach
    void setUp() {
        registrationDto = new UserRegistrationDto("test", "pass123", "test@email.com", "USER", "123456789");
    }

    @Test
    void createUser_Success() {
        when(userRepository.findByUsername("test")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass123")).thenReturn("encodedPass");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("test");
        savedUser.setEmail("test@email.com");
        savedUser.setRoles(Set.of(Role.USER));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDto userDto = new UserDto(1L, "test", "test@email.com", Set.of("USER"), "123456");
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        UserDto result = userService.createUser(registrationDto);

        assertNotNull(result);
        assertEquals("test", result.username());
        assertEquals("test@email.com", result.email());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("pass123");
        verify(userMapper).toDto(savedUser);
    }

    @Test
    void createUser_UsernameExists_ThrowsException() {
        when(userRepository.findByUsername("test")).thenReturn(Optional.of(new User()));

        BusinessLogicException exception = assertThrows(BusinessLogicException.class, () -> userService.createUser(registrationDto));
        assertEquals("Пользователь с таким именем уже существует", exception.getMessage());
    }
}

package ru.sazon.forget_to_remember.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.sazon.forget_to_remember.dto.user.UserDto;
import ru.sazon.forget_to_remember.dto.user.UserRegistrationDto;
import ru.sazon.forget_to_remember.mapper.UserMapper;
import ru.sazon.forget_to_remember.model.Role;
import ru.sazon.forget_to_remember.model.User;
import ru.sazon.forget_to_remember.repository.UserRepository;


import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Сервис для работы с пользователями")
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    private UserDto testUserDto;

    private UserRegistrationDto testRegistrationDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setPassword("encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setContact("+123456789");
        testUser.setRoles(new HashSet<>(Set.of(Role.USER)));

        testUserDto = new UserDto(
                1L,
                "testUser",
                "test@example.com",
                Set.of(Role.USER.toString()),
                "+123456789"
        );

        testRegistrationDto = new UserRegistrationDto(
                "newUser",
                "password123",
                "new@example.com",
                "USER",
                "+987654321"

        );
    }

    @DisplayName("должен получать пользователя по id")
    @Test
    void shouldGetUserById() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        UserDto result = userService.getById(userId);

        assertThat(result).isNotNull();
        assertThat(result).usingRecursiveComparison().isEqualTo(testUserDto);
        verify(userRepository).findById(userId);
    }

    @DisplayName("должен создавать нового пользователя")
    @Test
    void shouldCreateUser() {
        when(userRepository.findByUsername(testRegistrationDto.username())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(testRegistrationDto.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        UserDto result = userService.createUser(testRegistrationDto);

        assertThat(result).isNotNull();
        assertThat(result).usingRecursiveComparison().isEqualTo(testUserDto);
        verify(userRepository).findByUsername(testRegistrationDto.username());
        verify(passwordEncoder).encode(testRegistrationDto.password());
        verify(userRepository).save(any(User.class));
    }

    @DisplayName("должен создавать пользователя с ролью ADMIN")
    @Test
    void shouldCreateUserWithAdminRole() {
        UserRegistrationDto adminRegistrationDto = new UserRegistrationDto(
                "adminUser",
                "adminPass",
                "admin@example.com",
                "ADMIN",
                "+111111111"

        );

        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("adminUser");
        adminUser.setPassword("encodedAdminPass");
        adminUser.setEmail("admin@example.com");
        adminUser.setContact("+111111111");
        adminUser.setRoles(new HashSet<>(Set.of(Role.USER, Role.ADMIN)));

        UserDto adminUserDto = new UserDto(
                2L,
                "adminUser",
                "admin@example.com",
                Set.of(Role.USER.toString(), Role.ADMIN.toString()),
                "+111111111"

        );

        when(userRepository.findByUsername("adminUser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("adminPass")).thenReturn("encodedAdminPass");
        when(userRepository.save(any(User.class))).thenReturn(adminUser);
        when(userMapper.toDto(adminUser)).thenReturn(adminUserDto);

        UserDto result = userService.createUser(adminRegistrationDto);

        assertThat(result).isNotNull();
        assertThat(result.roles()).contains(Role.USER.toString(), Role.ADMIN.toString());
    }
}

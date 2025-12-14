package ru.sazon.forget_to_remember.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sazon.forget_to_remember.config.SecurityConfig;
import ru.sazon.forget_to_remember.dto.user.UserDto;
import ru.sazon.forget_to_remember.dto.user.UserRegistrationDto;
import ru.sazon.forget_to_remember.exeption.BusinessLogicException;
import ru.sazon.forget_to_remember.model.Role;
import ru.sazon.forget_to_remember.model.User;
import ru.sazon.forget_to_remember.service.UserService;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationManager authManager;

    private User testUser;

    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setEmail("test@user.com");
        testUser.setContact("@user_tg");

        testUserDto = new UserDto(
                1L,
                "testuser",
                "test@user.com",
                Set.of(Role.USER.toString()),
                "@user_tg"

        );
    }

    @Test
    @DisplayName("должен регистрировать нового пользователя")
    void shouldRegisterNewUser() throws Exception {
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                "newuser",
                "password123",
                "new@user.com",
                "USER",
                "@new_tg"

        );

        UserDto createdDto = new UserDto(
                2L,
                "newuser",
                "new@user.com",
                Set.of(Role.USER.toString()),
                "@new_tg"
        );

        given(userService.createUser(any(UserRegistrationDto.class)))
                .willReturn(createdDto);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("new@user.com"))
                .andExpect(jsonPath("$.contact").value("@new_tg"));
    }

    @Test
    @DisplayName("должен регистрировать пользователя с ролью ADMIN")
    void shouldRegisterUserWithAdminRole() throws Exception {
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                "adminuser",
                "adminpass",
                "admin@user.com",
                "ADMIN",
                "@admin_tg"
        );

        UserDto createdDto = new UserDto(
                2L,
                "adminuser",
                "admin@user.com",
                Set.of(Role.USER.toString(), Role.ADMIN.toString()),
                "@admin_tg"
        );

        given(userService.createUser(any(UserRegistrationDto.class)))
                .willReturn(createdDto);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles.length()").value(2));
    }

    @Test
    @DisplayName("должен возвращать ошибку при регистрации с существующим именем пользователя")
    void shouldReturnErrorWhenRegisteringWithExistingUsername() throws Exception {
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                "existinguser",
                "password123",
                "existing@example.com",
                "USER",
                "@existing_tg"
        );

        given(userService.createUser(any(UserRegistrationDto.class)))
                .willThrow(new BusinessLogicException("Пользователь с таким именем уже существует"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Пользователь с таким именем уже существует"));
    }

    @Test
    @DisplayName("должен аутентифицировать пользователя")
    void shouldAuthenticateUser() throws Exception {
        User loginRequest = new User();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "testuser", "password", null
        );

        given(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(auth);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "Вошел в систему: testuser"
                )));
    }
}

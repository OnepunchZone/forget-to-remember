package ru.sazon.forget_to_remember.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sazon.forget_to_remember.config.MyUserDetails;
import ru.sazon.forget_to_remember.config.SecurityConfig;
import ru.sazon.forget_to_remember.dto.birthday.BirthdayDto;
import ru.sazon.forget_to_remember.dto.greeting.GreetingDto;
import ru.sazon.forget_to_remember.dto.user.UserDto;
import ru.sazon.forget_to_remember.dto.user.UserRegistrationDto;
import ru.sazon.forget_to_remember.exeption.BusinessLogicException;
import ru.sazon.forget_to_remember.model.Role;
import ru.sazon.forget_to_remember.model.User;
import ru.sazon.forget_to_remember.service.BirthdayService;
import ru.sazon.forget_to_remember.service.GreetingService;
import ru.sazon.forget_to_remember.service.UserService;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(WebController.class)
@Import(SecurityConfig.class)
class WebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BirthdayService birthdayService;

    @MockitoBean
    private GreetingService greetingService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private User testUser;

    private Authentication userAuth;

    private Authentication adminAuth;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(Role.USER);
        testUser.setRoles(userRoles);

        MyUserDetails userDetails = new MyUserDetails(testUser);
        userAuth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(Role.USER);
        adminRoles.add(Role.ADMIN);
        adminUser.setRoles(adminRoles);

        MyUserDetails adminDetails = new MyUserDetails(adminUser);
        adminAuth = new UsernamePasswordAuthenticationToken(
                adminDetails, null, adminDetails.getAuthorities()
        );
    }

    @Test
    @DisplayName("должен отображать домашнюю страницу")
    void shouldDisplayHomePage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("showHeader", true));
    }

    @Test
    @DisplayName("должен отображать страницу входа")
    void shouldDisplayLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("должен отображать страницу регистрации")
    void shouldDisplayRegisterPage() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/register"));
    }

    @Test
    @DisplayName("должен отображать панель управления для аутентифицированного пользователя")
    void shouldDisplayDashboardForAuthenticatedUser() throws Exception {
        List<BirthdayDto> birthdays = List.of(
                new BirthdayDto(1L, "John", LocalDate.now(), "@john", 1L, null)
        );

        Page<GreetingDto> greetingPage = new PageImpl<>(List.of(
                new GreetingDto(1L, "Greeting 1", null, true, 5, 1L)
        ));

        given(birthdayService.findByUser(testUser)).willReturn(birthdays);
        given(greetingService.findByOwner(eq(testUser), any(Pageable.class)))
                .willReturn(greetingPage);

        mockMvc.perform(get("/dashboard")
                        .with(authentication(userAuth)))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("currentUser"))
                .andExpect(model().attributeExists("birthdays"))
                .andExpect(model().attributeExists("greetingPage"))
                .andExpect(model().attribute("username", "testuser"));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("не должен отображать панель управления для анонимного пользователя")
    void shouldNotDisplayDashboardForAnonymous() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @DisplayName("должен отображать дни рождения пользователя")
    void shouldDisplayUserBirthdays() throws Exception {
        List<BirthdayDto> birthdays = List.of(
                new BirthdayDto(1L, "John", LocalDate.now(), "@john", 1L, null)
        );

        Page<GreetingDto> myGreetings = new PageImpl<>(List.of(
                new GreetingDto(1L, "My Greeting", null, false, 0, 1L)
        ));

        Page<GreetingDto> publicGreetings = new PageImpl<>(List.of(
                new GreetingDto(2L, "Public Greeting", null, true, 10, 2L)
        ));

        given(birthdayService.findByUser(testUser)).willReturn(birthdays);
        given(greetingService.findByOwner(eq(testUser), any(Pageable.class)))
                .willReturn(myGreetings);
        given(greetingService.findPublic(any(Pageable.class)))
                .willReturn(publicGreetings);

        mockMvc.perform(get("/birthdays")
                        .with(authentication(userAuth)))
                .andExpect(status().isOk())
                .andExpect(view().name("birthday/list"))
                .andExpect(model().attributeExists("birthdays"))
                .andExpect(model().attributeExists("newBirthday"))
                .andExpect(model().attributeExists("myGreetings"))
                .andExpect(model().attributeExists("publicGreetings"))
                .andExpect(model().attribute("username", "testuser"));
    }

    @Test
    @DisplayName("должен отображать публичные поздравления")
    void shouldDisplayPublicGreetings() throws Exception {
        Page<GreetingDto> greetingsPage = new PageImpl<>(List.of(
                new GreetingDto(1L, "Public Greeting 1", null, true, 10, 1L),
                new GreetingDto(2L, "Public Greeting 2", null, true, 5, 2L)
        ));

        given(greetingService.findPublic(any(Pageable.class)))
                .willReturn(greetingsPage);

        mockMvc.perform(get("/greetings")
                        .with(authentication(userAuth))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("greeting/public-list"))
                .andExpect(model().attributeExists("greetings"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attributeExists("totalPages"))
                .andExpect(model().attributeExists("newGreeting"))
                .andExpect(model().attribute("username", "testuser"));
    }

    @Test
    @DisplayName("должен отображать личные поздравления пользователя")
    void shouldDisplayUserGreetings() throws Exception {
        Page<GreetingDto> greetingsPage = new PageImpl<>(List.of(
                new GreetingDto(1L, "My Greeting 1", null, false, 0, 1L),
                new GreetingDto(2L, "My Greeting 2", null, true, 3, 1L)
        ));

        given(greetingService.findByOwner(eq(testUser), any(Pageable.class)))
                .willReturn(greetingsPage);

        mockMvc.perform(get("/greetings-my")
                        .with(authentication(userAuth))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("greeting/user-list"))
                .andExpect(model().attributeExists("greetings"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attributeExists("totalPages"))
                .andExpect(model().attributeExists("newGreeting"))
                .andExpect(model().attribute("username", "testuser"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("должен отображать список пользователей для администратора")
    void shouldDisplayUsersListForAdmin() throws Exception {
        List<UserDto> users = List.of(
                new UserDto(2L, "user1", "user1@example.com", Set.of(Role.USER.toString()), "@user1"),
                new UserDto(1L, "admin", "admin@example.com", Set.of(Role.USER.toString(), Role.ADMIN.toString()), "@admin")
        );

        given(userService.findAllUsers()).willReturn(users);

        mockMvc.perform(get("/admin/users")
                        .with(authentication(adminAuth)))
                .andExpect(status().isOk())
                .andExpect(view().name("user/admin-users"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("currentUserId"))
                .andExpect(model().attributeExists("updateDto"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("не должен отображать список пользователей для обычного пользователя")
    void shouldNotDisplayUsersListForRegularUser() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .with(authentication(userAuth)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("должен обновлять пользователя как администратор")
    void shouldUpdateUserAsAdmin() throws Exception {
        Long userId = 2L;
        UserRegistrationDto updateDto = new UserRegistrationDto(
                "updateduser",
                null,
                "updated@example.com",
                "@updated",
                "USER"
        );

        UserDto updatedUser = new UserDto(
                userId,
                "updateduser",
                "updated@example.com",
                Set.of(Role.USER.toString()),
                "@updated"
        );

        given(userService.updateUser(eq(userId), any(UserRegistrationDto.class)))
                .willReturn(updatedUser);

        mockMvc.perform(post("/admin/users/{id}/update", userId)
                        .with(authentication(adminAuth))
                        .flashAttr("updateDto", updateDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("должен удалять пользователя как администратор")
    void shouldDeleteUserAsAdmin() throws Exception {
        Long userId = 2L;

        mockMvc.perform(post("/admin/users/{id}/delete", userId)
                        .with(authentication(adminAuth)))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Нельзя удалить самого себя"))
                .andExpect(view().name("user/admin-users"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("не должен позволять администратору удалять самого себя")
    void shouldNotAllowAdminToDeleteSelf() throws Exception {
        Long adminId = 2L;

        doThrow(new BusinessLogicException("Нельзя удалить самого себя"))
                .when(userService).deleteUser(adminId);

        mockMvc.perform(post("/admin/users/{id}/delete", adminId)
                        .with(authentication(adminAuth)))
                .andExpect(status().isOk())
                .andExpect(view().name("user/admin-users"))
                .andExpect(model().attributeExists("error"));
    }
}

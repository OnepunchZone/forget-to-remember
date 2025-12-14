package ru.sazon.forget_to_remember.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sazon.forget_to_remember.bot.BotSender;
import ru.sazon.forget_to_remember.config.MyUserDetails;
import ru.sazon.forget_to_remember.config.SecurityConfig;
import ru.sazon.forget_to_remember.dto.greeting.GreetingDto;
import ru.sazon.forget_to_remember.mapper.GreetingMapper;
import ru.sazon.forget_to_remember.model.Greeting;
import ru.sazon.forget_to_remember.model.Role;
import ru.sazon.forget_to_remember.model.User;
import ru.sazon.forget_to_remember.service.GreetingService;
import ru.sazon.forget_to_remember.service.SendService;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SendController.class)
@Import(SecurityConfig.class)
class SendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SendService sendService;

    @MockitoBean
    private GreetingService greetingService;

    @MockitoBean
    private GreetingMapper greetingMapper;

    @MockitoBean
    private BotSender bot;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private GreetingDto testGreetingDto;

    private Greeting testGreeting;

    private Authentication userAuth;

    private Authentication otherUserAuth;

    private Authentication adminAuth;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(Role.USER);
        testUser.setRoles(userRoles);

        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");
        Set<Role> otherUserRoles = new HashSet<>();
        otherUserRoles.add(Role.USER);
        otherUser.setRoles(otherUserRoles);

        User adminUser = new User();
        adminUser.setId(3L);
        adminUser.setUsername("admin");
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(Role.USER);
        adminRoles.add(Role.ADMIN);
        adminUser.setRoles(adminRoles);

        testGreetingDto = new GreetingDto(
                1L,
                "Test greeting text",
                null,
                true,
                5,
                testUser.getId()
        );

        testGreeting = new Greeting();
        testGreeting.setId(1L);
        testGreeting.setText("Test greeting text");
        testGreeting.setOwner(testUser);

        MyUserDetails userDetails = new MyUserDetails(testUser);
        userAuth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        MyUserDetails otherUserDetails = new MyUserDetails(otherUser);
        otherUserAuth = new UsernamePasswordAuthenticationToken(
                otherUserDetails, null, otherUserDetails.getAuthorities()
        );

        MyUserDetails adminDetails = new MyUserDetails(adminUser);
        adminAuth = new UsernamePasswordAuthenticationToken(
                adminDetails, null, adminDetails.getAuthorities()
        );
    }

    @Test
    @DisplayName("должен отправлять поздравление владельцем")
    void shouldSendGreetingByOwner() throws Exception {
        Long greetingId = 1L;
        String chatId = "@recipient";

        given(greetingService.getById(greetingId)).willReturn(testGreetingDto);
        given(greetingMapper.toEntity(testGreetingDto)).willReturn(testGreeting);

        mockMvc.perform(post("/api/v1/send/{greetingId}", greetingId)
                        .param("chatId", chatId)
                        .with(authentication(userAuth)))
                .andExpect(status().isOk())
                .andExpect(content().string("Sent to " + chatId));
    }

    @Test
    @DisplayName("должен отправлять поздравление администратором")
    void shouldSendGreetingByAdmin() throws Exception {
        Long greetingId = 1L;
        String chatId = "@recipient";

        given(greetingService.getById(greetingId)).willReturn(testGreetingDto);
        given(greetingMapper.toEntity(testGreetingDto)).willReturn(testGreeting);

        mockMvc.perform(post("/api/v1/send/{greetingId}", greetingId)
                        .param("chatId", chatId)
                        .with(authentication(adminAuth)))
                .andExpect(status().isOk())
                .andExpect(content().string("Sent to " + chatId));
    }

    @Test
    @DisplayName("не должен отправлять чужое поздравление обычным пользователем")
    void shouldNotSendOtherUsersGreeting() throws Exception {
        Long greetingId = 1L;
        String chatId = "@recipient";

        given(greetingService.getById(greetingId)).willReturn(testGreetingDto);
        given(greetingMapper.toEntity(testGreetingDto)).willReturn(testGreeting);

        mockMvc.perform(post("/api/v1/send/{greetingId}", greetingId)
                        .param("chatId", chatId)
                        .with(authentication(otherUserAuth)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Not authorized"));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("не должен отправлять поздравление для анонимного пользователя")
    void shouldNotSendGreetingForAnonymous() throws Exception {
        mockMvc.perform(post("/api/v1/send/1")
                        .param("chatId", "@recipient"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("должен возвращать ошибку при отсутствии chatId")
    void shouldReturnErrorWhenChatIdMissing() throws Exception {
        Long greetingId = 1L;

        mockMvc.perform(post("/api/v1/send/{greetingId}", greetingId)
                        .with(authentication(userAuth)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("должен возвращать ошибку при несуществующем поздравлении")
    void shouldReturnErrorForNonExistingGreeting() throws Exception {
        Long nonExistingId = 999L;
        String chatId = "@recipient";

        given(greetingService.getById(nonExistingId))
                .willThrow(new ru.sazon.forget_to_remember.exeption.EntityNotFoundException(
                        "Greeting not found with id: " + nonExistingId));

        mockMvc.perform(post("/api/v1/send/{greetingId}", nonExistingId)
                        .param("chatId", chatId)
                        .with(authentication(userAuth)))
                .andExpect(status().isNotFound());
    }
}

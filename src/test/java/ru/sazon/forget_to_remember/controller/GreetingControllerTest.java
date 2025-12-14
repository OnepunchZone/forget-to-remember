package ru.sazon.forget_to_remember.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sazon.forget_to_remember.config.MyUserDetails;
import ru.sazon.forget_to_remember.config.SecurityConfig;
import ru.sazon.forget_to_remember.dto.greeting.GreetingCreateDto;
import ru.sazon.forget_to_remember.dto.greeting.GreetingDto;
import ru.sazon.forget_to_remember.dto.greeting.GreetingUpdateDto;
import ru.sazon.forget_to_remember.model.Role;
import ru.sazon.forget_to_remember.model.User;
import ru.sazon.forget_to_remember.service.GreetingService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(GreetingController.class)
@Import(SecurityConfig.class)
class GreetingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GreetingService greetingService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private User testUser;

    private User adminUser;

    private GreetingDto testGreetingDto;

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

        adminUser = new User();
        adminUser.setId(2L);
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

        MyUserDetails userDetails = new MyUserDetails(testUser);
        userAuth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        MyUserDetails adminDetails = new MyUserDetails(adminUser);
        adminAuth = new UsernamePasswordAuthenticationToken(
                adminDetails, null, adminDetails.getAuthorities()
        );
    }

    @Test
    @DisplayName("должен создавать новое поздравление для аутентифицированного пользователя")
    void shouldCreateGreetingForAuthenticatedUser() throws Exception {
        GreetingCreateDto createDto = new GreetingCreateDto(
                "New greeting",
                null,
                true
        );

        GreetingDto createdDto = new GreetingDto(
                1L,
                "New greeting",
                null,
                true,
                0,
                testUser.getId()
        );

        given(greetingService.createGreeting(any(GreetingCreateDto.class), eq(testUser)))
                .willReturn(createdDto);

        mockMvc.perform(post("/api/v1/greeting")
                        .with(authentication(userAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("New greeting"))
                .andExpect(jsonPath("$.isPublic").value(true))
                .andExpect(jsonPath("$.ownerId").value(testUser.getId()));
    }

    @Test
    @DisplayName("должен обновлять поздравление владельцем")
    void shouldUpdateGreetingByOwner() throws Exception {
        Long greetingId = 1L;
        GreetingUpdateDto updateDto = new GreetingUpdateDto(
                "Updated text",
                null,
                false
        );

        GreetingDto updatedDto = new GreetingDto(
                1L,
                "Updated text",
                null,
                false,
                5,
                testUser.getId()
        );

        given(greetingService.getById(greetingId)).willReturn(testGreetingDto);
        given(greetingService.updateGreeting(eq(greetingId), any(GreetingUpdateDto.class)))
                .willReturn(updatedDto);

        mockMvc.perform(put("/api/v1/greeting/{id}", greetingId)
                        .with(authentication(userAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Updated text"))
                .andExpect(jsonPath("$.isPublic").value(false));
    }

    @Test
    @DisplayName("должен обновлять поздравление админа")
    void shouldUpdateGreetingByAdmin() throws Exception {
        Long greetingId = 1L;
        GreetingUpdateDto updateDto = new GreetingUpdateDto(
                "Updated by admin",
                null,
                false
        );

        GreetingDto updatedDto = new GreetingDto(
                greetingId,
                "Updated by admin",
                null,
                true,
                5,
                testUser.getId()
        );

        given(greetingService.getById(greetingId)).willReturn(testGreetingDto);
        given(greetingService.updateGreeting(eq(greetingId), any(GreetingUpdateDto.class)))
                .willReturn(updatedDto);

        mockMvc.perform(put("/api/v1/greeting/{id}", greetingId)
                        .with(authentication(adminAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Updated by admin"));
    }

    @Test
    @DisplayName("должен удалять поздравление владельцем")
    void shouldDeleteGreetingByOwner() throws Exception {
        Long greetingId = 1L;

        given(greetingService.getById(greetingId)).willReturn(testGreetingDto);

        mockMvc.perform(delete("/api/v1/greeting/{id}", greetingId)
                        .with(authentication(userAuth)))
                .andExpect(status().isOk())
                .andExpect(content().string("Greeting deleted"));

        verify(greetingService).deleteGreeting(greetingId);
    }

    @Test
    @DisplayName("должен получать поздравления пользователя")
    void shouldGetMyGreetings() throws Exception {
        Page<GreetingDto> greetingPage = new PageImpl<>(List.of(testGreetingDto));

        given(greetingService.findByOwner(eq(testUser), any(Pageable.class)))
                .willReturn(greetingPage);

        mockMvc.perform(get("/api/v1/greeting/my")
                        .with(authentication(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].text").value("Test greeting text"))
                .andExpect(jsonPath("$.content[0].ownerId").value(testUser.getId()));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("не должен получать поздравления пользователя для анонимна")
    void shouldNotGetMyGreetingsForAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/greeting/my"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("должен получать публичные поздравления")
    void shouldGetPublicGreetings() throws Exception {
        Page<GreetingDto> greetingPage = new PageImpl<>(List.of(testGreetingDto));

        given(greetingService.findPublic(any(Pageable.class)))
                .willReturn(greetingPage);

        mockMvc.perform(get("/api/v1/greeting/public")
                        .with(authentication(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].isPublic").value(true));
    }

    @Test
    @DisplayName("должен добавлять лайк")
    void shouldAddLikeToGreeting() throws Exception {
        Long greetingId = 1L;
        GreetingDto likedDto = new GreetingDto(
                greetingId,
                "Test greeting text",
                null,
                true,
                6,
                testUser.getId()
        );

        given(greetingService.addLike(greetingId)).willReturn(likedDto);

        mockMvc.perform(post("/api/v1/greeting/{id}/like", greetingId)
                        .with(authentication(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likesCount").value(6));
    }

    @Test
    @DisplayName("должен удалять лайк с поздравления")
    void shouldRemoveLikeFromGreeting() throws Exception {
        Long greetingId = 1L;
        GreetingDto unlikedDto = new GreetingDto(
                greetingId,
                "Test greeting text",
                null,
                true,
                4,
                testUser.getId()
        );

        given(greetingService.removeLike(greetingId)).willReturn(unlikedDto);

        mockMvc.perform(delete("/api/v1/greeting/{id}/like", greetingId)
                        .with(authentication(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likesCount").value(4));
    }
}

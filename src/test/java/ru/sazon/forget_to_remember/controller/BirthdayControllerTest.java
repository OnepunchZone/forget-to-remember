package ru.sazon.forget_to_remember.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sazon.forget_to_remember.config.MyUserDetails;
import ru.sazon.forget_to_remember.config.SecurityConfig;
import ru.sazon.forget_to_remember.dto.birthday.BirthdayCreateDto;
import ru.sazon.forget_to_remember.dto.birthday.BirthdayDto;
import ru.sazon.forget_to_remember.dto.birthday.BirthdayUpdateDto;
import ru.sazon.forget_to_remember.mapper.GreetingMapper;
import ru.sazon.forget_to_remember.model.Greeting;
import ru.sazon.forget_to_remember.model.Role;
import ru.sazon.forget_to_remember.model.User;
import ru.sazon.forget_to_remember.service.BirthdayService;

import java.time.LocalDate;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BirthdayController.class)
@Import(SecurityConfig.class)
class BirthdayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BirthdayService birthdayService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private User testUser;

    private BirthdayDto testBirthdayDto;

    private Greeting testGreeting;

    private Greeting testGreeting2;

    @MockitoBean
    private GreetingMapper greetingMapper;

    private Authentication userAuth;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(Role.USER);
        testUser.setRoles(userRoles);

        testGreeting = new Greeting();
        testGreeting.setId(1L);
        testGreeting.setText("Test greeting");
        testGreeting.setOwner(testUser);

        testGreeting2 = new Greeting();
        testGreeting2.setId(2L);
        testGreeting2.setText("Second greeting");
        testGreeting2.setOwner(testUser);

        testBirthdayDto = new BirthdayDto(
                1L,
                "Rick Sanchez",
                LocalDate.of(1990, 5, 15),
                "+123456789",
                testUser.getId(),
                greetingMapper.toDto(testGreeting)
        );

        MyUserDetails userDetails = new MyUserDetails(testUser);
        userAuth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
    }

    @Test
    @DisplayName("должен создавать новый др")
    void shouldCreateBirthday() throws Exception {
        BirthdayCreateDto createDto = new BirthdayCreateDto(
                "Cartman",
                LocalDate.of(1995, 8, 20),
                "@cartman_tg",
                1L
        );

        BirthdayDto createdDto = new BirthdayDto(
                2L,
                "Cartman",
                LocalDate.of(1995, 8, 20),
                "@cartman_tg",
                testUser.getId(),
                greetingMapper.toDto(testGreeting)
        );

        given(birthdayService.createBirthday(any(BirthdayCreateDto.class), eq(testUser)))
                .willReturn(createdDto);

        mockMvc.perform(post("/api/v1/birthday")
                        .with(authentication(userAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("Cartman"))
                .andExpect(jsonPath("$.contact").value("@cartman_tg"))
                .andExpect(jsonPath("$.userId").value(testUser.getId()));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("не должен создавать день рождения для анонимного пользователя")
    void shouldNotCreateBirthdayForAnonymousUser() throws Exception {
        BirthdayCreateDto createDto = new BirthdayCreateDto(
                "Cartman",
                LocalDate.of(1995, 8, 20),
                "@cartman_tg",
                1L
        );

        mockMvc.perform(post("/api/v1/birthday")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("должен получать др пользователя")
    void shouldGetMyBirthdays() throws Exception {
        List<BirthdayDto> birthdays = List.of(testBirthdayDto);

        given(birthdayService.findByUser(testUser)).willReturn(birthdays);

        mockMvc.perform(get("/api/v1/birthday")
                        .with(authentication(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Rick Sanchez"))
                .andExpect(jsonPath("$[0].userId").value(testUser.getId()));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("не должен получать др для анонима")
    void shouldNotGetMyBirthdaysForAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/birthday"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("должен получать др на сегодня")
    void shouldGetTodayBirthdays() throws Exception {
        List<BirthdayDto> birthdays = List.of(testBirthdayDto);

        given(birthdayService.findByDate(any(LocalDate.class))).willReturn(birthdays);

        mockMvc.perform(get("/api/v1/birthday/today")
                        .with(authentication(userAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Rick Sanchez"));
    }

    @Test
    @DisplayName("должен обновлять др")
    void shouldUpdateBirthday() throws Exception {
        Long birthdayId = 1L;
        BirthdayUpdateDto updateDto = new BirthdayUpdateDto(
                "Updated Name",
                LocalDate.of(2000, 1, 1),
                "@updated_tg",
                2L
        );

        BirthdayDto updatedDto = new BirthdayDto(
                birthdayId,
                "Updated Name",
                LocalDate.of(2000, 1, 1),
                "@updated_tg",
                testUser.getId(),
                greetingMapper.toDto(testGreeting2)
        );

        given(birthdayService.updateBirthday(eq(birthdayId), any(BirthdayUpdateDto.class)))
                .willReturn(updatedDto);

        mockMvc.perform(put("/api/v1/birthday/{id}", birthdayId)
                        .with(authentication(userAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.contact").value("@updated_tg"));
    }

    @Test
    @DisplayName("должен удалять день рождения")
    void shouldDeleteBirthday() throws Exception {
        Long birthdayId = 1L;

        mockMvc.perform(delete("/api/v1/birthday/{id}", birthdayId)
                        .with(authentication(userAuth)))
                .andExpect(status().isOk())
                .andExpect(content().string("Birthday deleted"));

        verify(birthdayService).deleteBirthday(birthdayId);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("не должен удалять день рождения для анонимного пользователя")
    void shouldNotDeleteBirthdayForAnonymous() throws Exception {
        mockMvc.perform(delete("/api/v1/birthday/1"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}

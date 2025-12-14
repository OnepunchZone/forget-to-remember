package ru.sazon.forget_to_remember.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.sazon.forget_to_remember.dto.greeting.GreetingCreateDto;
import ru.sazon.forget_to_remember.dto.greeting.GreetingDto;
import ru.sazon.forget_to_remember.dto.greeting.GreetingUpdateDto;
import ru.sazon.forget_to_remember.mapper.GreetingMapper;
import ru.sazon.forget_to_remember.model.Greeting;
import ru.sazon.forget_to_remember.model.User;
import ru.sazon.forget_to_remember.repository.GreetingRepository;
import ru.sazon.forget_to_remember.repository.UserRepository;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Сервис для работы с поздравлениями")
@ExtendWith(MockitoExtension.class)
public class GreetingServiceImplTest {
    @Mock
    private GreetingRepository greetingRepository;

    @Mock
    private GreetingMapper greetingMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private GreetingServiceImpl greetingService;

    private User testUser;

    private Greeting testGreeting;

    private GreetingDto testGreetingDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        testGreeting = new Greeting();
        testGreeting.setId(1L);
        testGreeting.setText("Test greeting text");
        testGreeting.setPublic(true);
        testGreeting.setOwner(testUser);
        testGreeting.setLikedBy(new HashSet<>());

        testGreetingDto = new GreetingDto(
                1L,
                "Test greeting text",
                null,
                true,
                0,
                testUser.getId()
        );
    }

    @DisplayName("должен создавать новое поздравление")
    @Test
    void shouldCreateGreeting() {
        GreetingCreateDto createDto = new GreetingCreateDto(
                "New greeting text",
                null,
                true
        );

        Greeting newGreeting = new Greeting();
        newGreeting.setId(2L);
        newGreeting.setText(createDto.text());
        newGreeting.setPublic(createDto.isPublic());
        newGreeting.setOwner(testUser);

        GreetingDto expectedDto = new GreetingDto(
                2L,
                "New greeting text",
                null,
                true,
                0,
                testUser.getId()
        );

        when(greetingRepository.save(any(Greeting.class))).thenReturn(newGreeting);
        when(greetingMapper.toDto(newGreeting)).thenReturn(expectedDto);

        GreetingDto result = greetingService.createGreeting(createDto, testUser);

        assertThat(result).isNotNull();
        assertThat(result).usingRecursiveComparison().isEqualTo(expectedDto);
        verify(greetingRepository).save(any(Greeting.class));
    }

    @DisplayName("должен обновлять существующее поздравление")
    @Test
    void shouldUpdateGreeting() {
        Long greetingId = 1L;
        GreetingUpdateDto updateDto = new GreetingUpdateDto(
                "Updated text",
                null,
                false
        );

        Greeting updatedGreeting = new Greeting();
        updatedGreeting.setId(greetingId);
        updatedGreeting.setText(updateDto.text());
        updatedGreeting.setPublic(updateDto.isPublic());
        updatedGreeting.setOwner(testUser);

        GreetingDto expectedDto = new GreetingDto(
                greetingId,
                "Updated text",
                null,
                true,
                0,
                testUser.getId()
        );

        when(greetingRepository.findById(greetingId)).thenReturn(Optional.of(testGreeting));
        when(greetingRepository.save(any(Greeting.class))).thenReturn(updatedGreeting);
        when(greetingMapper.toDto(updatedGreeting)).thenReturn(expectedDto);

        GreetingDto result = greetingService.updateGreeting(greetingId, updateDto);

        assertThat(result).isNotNull();
        assertThat(result).usingRecursiveComparison().isEqualTo(expectedDto);
        verify(greetingRepository).findById(greetingId);
        verify(greetingRepository).save(any(Greeting.class));
    }

    @DisplayName("должен добавлять лайк к поздравлению")
    @Test
    void shouldAddLikeToGreeting() {
        Long greetingId = 1L;
        String username = "testUser";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(greetingRepository.findById(greetingId)).thenReturn(Optional.of(testGreeting));
        when(greetingRepository.save(testGreeting)).thenReturn(testGreeting);
        when(greetingMapper.toDto(testGreeting)).thenReturn(testGreetingDto);

        GreetingDto result = greetingService.addLike(greetingId);

        assertThat(result).isNotNull();
        assertThat(result).usingRecursiveComparison().isEqualTo(testGreetingDto);
        verify(greetingRepository).save(testGreeting);
    }

    @DisplayName("должен получать поздравление по id")
    @Test
    void shouldGetGreetingById() {
        Long greetingId = 1L;

        when(greetingRepository.findById(greetingId)).thenReturn(Optional.of(testGreeting));
        when(greetingMapper.toDto(testGreeting)).thenReturn(testGreetingDto);

        GreetingDto result = greetingService.getById(greetingId);

        assertThat(result).isNotNull();
        assertThat(result).usingRecursiveComparison().isEqualTo(testGreetingDto);
        verify(greetingRepository).findById(greetingId);
    }
}

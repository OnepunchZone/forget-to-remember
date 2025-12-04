package ru.sazon.forget_to_remember.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.sazon.forget_to_remember.dto.greeting.GreetingDto;
import ru.sazon.forget_to_remember.exeption.EntityNotFoundException;
import ru.sazon.forget_to_remember.mapper.GreetingMapperImpl;
import ru.sazon.forget_to_remember.model.Greeting;
import ru.sazon.forget_to_remember.model.User;
import ru.sazon.forget_to_remember.repository.GreetingRepository;
import ru.sazon.forget_to_remember.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GreetingServiceTest {

    @Mock
    private GreetingRepository greetingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GreetingMapperImpl greetingMapper;

    @InjectMocks
    private GreetingServiceImpl greetingService;

    private MockedStatic<SecurityContextHolder> securityContextMock;  // Mock для SecurityContext

    @BeforeEach
    void setUp() {
        SecurityContext context = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test");
        when(context.getAuthentication()).thenReturn(auth);
        securityContextMock = mockStatic(SecurityContextHolder.class);
        securityContextMock.when(SecurityContextHolder::getContext).thenReturn(context);
    }

    @AfterEach
    void tearDown() {
        securityContextMock.close();  // Закрой mock после теста
    }

    @Test
    void addLike_Success_IncreasesLikesCount() {
        Long greetingId = 1L;
        User user = new User();
        user.setId(1L);
        Greeting greeting = new Greeting();
        greeting.setId(greetingId);
        greeting.setLikesCount(0);

        when(userRepository.findByUsername("test")).thenReturn(Optional.of(user));
        when(greetingRepository.findById(greetingId)).thenReturn(Optional.of(greeting));
        when(greetingRepository.save(greeting)).thenReturn(greeting);

        GreetingDto greetingDto = new GreetingDto(1L, "test", null, true, 1, 1L);
        when(greetingMapper.toDto(greeting)).thenReturn(greetingDto);

        GreetingDto result = greetingService.addLike(greetingId);

        assertEquals(1, greeting.getLikesCount());
        verify(greetingRepository).save(greeting);
        assertNotNull(result);
        assertEquals(1, result.likesCount());  // Проверяем DTO
    }

    @Test
    void addLike_GreetingNotFound_ThrowsException() {
        Long greetingId = 999L;
        User user = new User();
        when(userRepository.findByUsername("test")).thenReturn(Optional.of(user));
        when(greetingRepository.findById(greetingId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> greetingService.addLike(greetingId));
        assertEquals("Greeting not found: 999", exception.getMessage());
    }
}

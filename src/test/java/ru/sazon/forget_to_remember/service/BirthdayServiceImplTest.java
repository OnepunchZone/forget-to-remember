package ru.sazon.forget_to_remember.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sazon.forget_to_remember.dto.birthday.BirthdayCreateDto;
import ru.sazon.forget_to_remember.dto.birthday.BirthdayDto;
import ru.sazon.forget_to_remember.dto.birthday.BirthdayUpdateDto;
import ru.sazon.forget_to_remember.mapper.BirthdayMapper;
import ru.sazon.forget_to_remember.mapper.GreetingMapper;
import ru.sazon.forget_to_remember.model.Birthday;
import ru.sazon.forget_to_remember.model.Greeting;
import ru.sazon.forget_to_remember.model.User;
import ru.sazon.forget_to_remember.repository.BirthdayRepository;
import ru.sazon.forget_to_remember.repository.GreetingRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Сервис для работы с днями рождения")
@ExtendWith(MockitoExtension.class)
class BirthdayServiceImplTest {

    @Mock
    private BirthdayRepository birthdayRepository;

    @Mock
    private BirthdayMapper birthdayMapper;

    @Mock
    private GreetingMapper greetingMapper;

    @Mock
    private GreetingRepository greetingRepository;

    @InjectMocks
    private BirthdayServiceImpl birthdayService;

    private User testUser;

    private Greeting testGreeting;

    private Birthday testBirthday;

    private BirthdayDto testBirthdayDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        testGreeting = new Greeting();
        testGreeting.setId(1L);
        testGreeting.setText("Test greeting");
        testGreeting.setOwner(testUser);

        testBirthday = new Birthday();
        testBirthday.setId(1L);
        testBirthday.setName("Rick Sanchez");
        testBirthday.setDate(LocalDate.of(1990, 5, 15));
        testBirthday.setContact("+123456789");
        testBirthday.setUser(testUser);
        testBirthday.setGreeting(testGreeting);

        testBirthdayDto = new BirthdayDto(
                1L,
                "Rick Sanchez",
                LocalDate.of(1990, 5, 15),
                "+123456789",
                testUser.getId(),
                greetingMapper.toDto(testGreeting)
        );
    }

    @DisplayName("должен находить дни рождения по пользователю")
    @Test
    void shouldFindByUser() {
        List<Birthday> birthdays = List.of(testBirthday);

        when(birthdayRepository.findByUser(testUser)).thenReturn(birthdays);
        when(birthdayMapper.toDto(testBirthday)).thenReturn(testBirthdayDto);

        List<BirthdayDto> result = birthdayService.findByUser(testUser);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).usingRecursiveComparison().isEqualTo(testBirthdayDto);
        verify(birthdayRepository).findByUser(testUser);
    }

    @DisplayName("должен создавать новый день рождения с приветствием")
    @Test
    void shouldCreateBirthdayWithGreeting() {
        BirthdayCreateDto createDto = new BirthdayCreateDto(
                "Cartman",
                LocalDate.of(1995, 8, 20),
                "+987654321",
                1L
        );

        Birthday newBirthday = new Birthday();
        newBirthday.setId(2L);
        newBirthday.setName(createDto.name());
        newBirthday.setDate(createDto.date());
        newBirthday.setContact(createDto.contact());
        newBirthday.setUser(testUser);
        newBirthday.setGreeting(testGreeting);

        BirthdayDto expectedDto = new BirthdayDto(
                2L,
                "Cartman",
                LocalDate.of(1995, 8, 20),
                "+987654321",
                testUser.getId(),
                greetingMapper.toDto(testGreeting)
        );

        when(greetingRepository.findById(createDto.greetingId())).thenReturn(Optional.of(testGreeting));
        when(birthdayRepository.save(any(Birthday.class))).thenReturn(newBirthday);
        when(birthdayMapper.toDto(newBirthday)).thenReturn(expectedDto);

        BirthdayDto result = birthdayService.createBirthday(createDto, testUser);

        assertThat(result).isNotNull();
        assertThat(result).usingRecursiveComparison().isEqualTo(expectedDto);
        verify(greetingRepository).findById(createDto.greetingId());
        verify(birthdayRepository).save(any(Birthday.class));
    }

    @DisplayName("должен обновлять день рождения")
    @Test
    void shouldUpdateBirthday() {
        Long birthdayId = 1L;
        BirthdayUpdateDto updateDto = new BirthdayUpdateDto(
                "Updated Name",
                LocalDate.of(2000, 1, 1),
                "+111111111",
                2L
        );

        Greeting newGreeting = new Greeting();
        newGreeting.setId(2L);
        newGreeting.setText("New greeting");
        newGreeting.setOwner(testUser);

        Birthday updatedBirthday = new Birthday();
        updatedBirthday.setId(birthdayId);
        updatedBirthday.setName(updateDto.name());
        updatedBirthday.setDate(updateDto.date());
        updatedBirthday.setContact(updateDto.contact());
        updatedBirthday.setUser(testUser);
        updatedBirthday.setGreeting(newGreeting);

        BirthdayDto expectedDto = new BirthdayDto(
                birthdayId,
                "Updated Name",
                LocalDate.of(2000, 1, 1),
                "+111111111",
                testUser.getId(),
                greetingMapper.toDto(newGreeting)

        );

        when(birthdayRepository.findById(birthdayId)).thenReturn(Optional.of(testBirthday));
        when(greetingRepository.findById(updateDto.greetingId())).thenReturn(Optional.of(newGreeting));
        when(birthdayRepository.save(any(Birthday.class))).thenReturn(updatedBirthday);
        when(birthdayMapper.toDto(updatedBirthday)).thenReturn(expectedDto);

        BirthdayDto result = birthdayService.updateBirthday(birthdayId, updateDto);

        assertThat(result).isNotNull();
        assertThat(result).usingRecursiveComparison().isEqualTo(expectedDto);
        verify(birthdayRepository).findById(birthdayId);
        verify(greetingRepository).findById(updateDto.greetingId());
        verify(birthdayRepository).save(any(Birthday.class));
    }
}

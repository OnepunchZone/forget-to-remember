package ru.sazon.forget_to_remember.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.sazon.forget_to_remember.model.Birthday;
import ru.sazon.forget_to_remember.model.Greeting;
import ru.sazon.forget_to_remember.model.Role;
import ru.sazon.forget_to_remember.model.User;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий для работы с др")
@DataJpaTest
class BirthdayRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BirthdayRepository birthdayRepository;

    private User testUser;
    private Birthday savedBirthday;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setEmail("test@example.com");

        Set<Role> roles = new HashSet<>();
        roles.add(Role.USER);
        testUser.setRoles(roles);

        em.persistAndFlush(testUser);

        testDate = LocalDate.of(1960, 5, 15);

        Birthday birthday = new Birthday();
        birthday.setName("Rick Sanchez");
        birthday.setDate(testDate);
        birthday.setContact("@rs_tg");
        birthday.setUser(testUser);

        savedBirthday = em.persistAndFlush(birthday);
    }

    @DisplayName("должен находить др по дате с загрузкой пользователя и поздравления")
    @Test
    void shouldFindByDateWithUserAndGreeting() {
        List<Birthday> birthdays = birthdayRepository.findByDate(testDate);

        assertThat(birthdays).hasSize(1);

        Birthday foundBirthday = birthdays.get(0);
        assertThat(foundBirthday.getName()).isEqualTo("Rick Sanchez");
        assertThat(foundBirthday.getUser()).isNotNull();
        assertThat(foundBirthday.getUser().getUsername()).isEqualTo("testuser");
    }

    @DisplayName("должен находить дни рождения пользователя")
    @Test
    void shouldFindByUser() {
        List<Birthday> birthdays = birthdayRepository.findByUser(testUser);

        assertThat(birthdays).hasSize(1);

        Birthday foundBirthday = birthdays.get(0);
        assertThat(foundBirthday.getName()).isEqualTo("Rick Sanchez");
        assertThat(foundBirthday.getDate()).isEqualTo(testDate);
    }

    @DisplayName("должен возвращать пустой список если у пользователя нет др")
    @Test
    void shouldReturnEmptyListWhenUserHasNoBirthdays() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password");

        Set<Role> roles = new HashSet<>();
        roles.add(Role.USER);
        newUser.setRoles(roles);

        em.persistAndFlush(newUser);

        List<Birthday> birthdays = birthdayRepository.findByUser(newUser);

        assertThat(birthdays).isEmpty();
    }

    @DisplayName("должен сохранять новый др")
    @Test
    void shouldSaveNewBirthday() {
        Birthday newBirthday = new Birthday();
        newBirthday.setName("Morty");
        newBirthday.setDate(LocalDate.of(2012, 8, 20));
        newBirthday.setContact("@morty_tg");
        newBirthday.setUser(testUser);

        Birthday saved = birthdayRepository.save(newBirthday);
        em.flush();

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Morty");

        Birthday foundBirthday = em.find(Birthday.class, saved.getId());
        assertThat(foundBirthday).isNotNull();
        assertThat(foundBirthday.getName()).isEqualTo("Morty");
    }

    @DisplayName("должен сохранять др с приветствием")
    @Test
    void shouldSaveBirthdayWithGreeting() {
        Greeting greeting = new Greeting();
        greeting.setText("Happy Birthday!");
        greeting.setPublic(true);
        greeting.setOwner(testUser);

        em.persistAndFlush(greeting);

        Birthday birthdayWithGreeting = new Birthday();
        birthdayWithGreeting.setName("Erik");
        birthdayWithGreeting.setDate(LocalDate.of(2006, 3, 10));
        birthdayWithGreeting.setContact("@erik_tg");
        birthdayWithGreeting.setUser(testUser);
        birthdayWithGreeting.setGreeting(greeting);

        Birthday saved = birthdayRepository.save(birthdayWithGreeting);
        em.flush();

        Birthday foundBirthday = em.find(Birthday.class, saved.getId());
        assertThat(foundBirthday.getGreeting()).isNotNull();
        assertThat(foundBirthday.getGreeting().getText()).isEqualTo("Happy Birthday!");
    }

    @DisplayName("должен обновлять существующий др")
    @Test
    void shouldUpdateBirthday() {
        Birthday existingBirthday = birthdayRepository.findById(savedBirthday.getId()).orElseThrow();
        existingBirthday.setName("Updated Name");
        existingBirthday.setContact("@updated_tg");

        birthdayRepository.save(existingBirthday);
        em.flush();

        Birthday foundBirthday = em.find(Birthday.class, savedBirthday.getId());

        assertThat(foundBirthday.getName()).isEqualTo("Updated Name");
        assertThat(foundBirthday.getContact()).isEqualTo("@updated_tg");
    }

    @DisplayName("должен удалять др")
    @Test
    void shouldDeleteBirthday() {
        assertThat(birthdayRepository.findById(savedBirthday.getId())).isPresent();

        birthdayRepository.deleteById(savedBirthday.getId());
        em.flush();

        assertThat(birthdayRepository.findById(savedBirthday.getId())).isEmpty();

        Birthday deletedBirthday = em.find(Birthday.class, savedBirthday.getId());
        assertThat(deletedBirthday).isNull();
    }

    @DisplayName("должен находить др по нескольким датам")
    @Test
    void shouldFindBirthdaysByMultipleDates() {
        LocalDate anotherDate = LocalDate.of(1992, 10, 5);

        Birthday anotherBirthday = new Birthday();
        anotherBirthday.setName("Sazon");
        anotherBirthday.setDate(anotherDate);
        anotherBirthday.setContact("@sz_tg");
        anotherBirthday.setUser(testUser);

        em.persistAndFlush(anotherBirthday);

        List<Birthday> birthdays1 = birthdayRepository.findByDate(testDate);
        assertThat(birthdays1).hasSize(1);
        assertThat(birthdays1.get(0).getName()).isEqualTo("Rick Sanchez");

        List<Birthday> birthdays2 = birthdayRepository.findByDate(anotherDate);
        assertThat(birthdays2).hasSize(1);
        assertThat(birthdays2.get(0).getName()).isEqualTo("Sazon");
    }
}

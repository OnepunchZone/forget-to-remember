package ru.sazon.forget_to_remember.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.sazon.forget_to_remember.model.Role;
import ru.sazon.forget_to_remember.model.User;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий для работы с пользователями")
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setEmail("test@example.com");
        user.setContact("@telegram_user");

        Set<Role> roles = new HashSet<>();
        roles.add(Role.USER);
        user.setRoles(roles);

        savedUser = em.persistAndFlush(user);
    }

    @DisplayName("должен находить пользователя по username")
    @Test
    void shouldFindByUsername() {
        Optional<User> foundUser = userRepository.findByUsername("testuser");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get()).usingRecursiveComparison()
                .ignoringFields("birthdays", "greetings", "likedGreetings")
                .isEqualTo(savedUser);
    }

    @DisplayName("должен возвращать пустой Optional при поиске несуществующего username")
    @Test
    void shouldReturnEmptyOptionalWhenUsernameNotFound() {
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");

        assertThat(foundUser).isEmpty();
    }

    @DisplayName("должен находить всех пользователей с загрузкой ролей")
    @Test
    void shouldFindAllWithRoles() {
        User anotherUser = new User();
        anotherUser.setUsername("user2");
        anotherUser.setPassword("password");
        anotherUser.setEmail("user2@another.com");

        Set<Role> roles = new HashSet<>();
        roles.add(Role.USER);
        roles.add(Role.ADMIN);
        anotherUser.setRoles(roles);

        em.persistAndFlush(anotherUser);

        List<User> allUsers = userRepository.findAll();

        assertThat(allUsers).hasSize(2);

        User firstUser = allUsers.stream()
                .filter(u -> u.getUsername().equals("testuser"))
                .findFirst()
                .orElseThrow();

        assertThat(firstUser.getRoles())
                .isNotNull()
                .hasSize(1)
                .contains(Role.USER);

        User secondUser = allUsers.stream()
                .filter(u -> u.getUsername().equals("user2"))
                .findFirst()
                .orElseThrow();

        assertThat(secondUser.getRoles())
                .isNotNull()
                .hasSize(2)
                .contains(Role.USER, Role.ADMIN);
    }

    @DisplayName("должен находить пользователя по id с загрузкой ролей")
    @Test
    void shouldFindByIdWithRoles() {
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getRoles())
                .isNotNull()
                .isNotEmpty()
                .contains(Role.USER);
    }

    @DisplayName("должен находить пользователя по contact")
    @Test
    void shouldFindByContact() {
        Optional<User> foundUser = userRepository.findByContact("@telegram_user");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get()).usingRecursiveComparison()
                .ignoringFields("birthdays", "greetings", "likedGreetings")
                .isEqualTo(savedUser);
    }

    @DisplayName("должен сохранять нового пользователя")
    @Test
    void shouldSaveNewUser() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("newPassword");
        newUser.setEmail("new@user.com");
        newUser.setContact("@new_tg");

        Set<Role> roles = new HashSet<>();
        roles.add(Role.USER);
        newUser.setRoles(roles);

        User saved = userRepository.save(newUser);
        em.flush();

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("newuser");

        User foundUser = em.find(User.class, saved.getId());
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo("newuser");
    }

    @DisplayName("должен обновлять существующего пользователя")
    @Test
    void shouldUpdateUser() {
        User existingUser = userRepository.findById(savedUser.getId()).orElseThrow();
        existingUser.setEmail("updated@user.com");
        existingUser.setContact("@updated_tg");

        userRepository.save(existingUser);
        em.flush();

        User foundUser = em.find(User.class, savedUser.getId());

        assertThat(foundUser.getEmail()).isEqualTo("updated@user.com");
        assertThat(foundUser.getContact()).isEqualTo("@updated_tg");
    }

    @DisplayName("должен удалять пользователя")
    @Test
    void shouldDeleteUser() {
        assertThat(userRepository.findById(savedUser.getId())).isPresent();

        userRepository.deleteById(savedUser.getId());
        em.flush();

        assertThat(userRepository.findById(savedUser.getId())).isEmpty();

        User deletedUser = em.find(User.class, savedUser.getId());
        assertThat(deletedUser).isNull();
    }
}

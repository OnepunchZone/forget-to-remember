package ru.sazon.forget_to_remember.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.sazon.forget_to_remember.model.Greeting;
import ru.sazon.forget_to_remember.model.Role;
import ru.sazon.forget_to_remember.model.User;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий для работы с поздравлениями")
@DataJpaTest
class GreetingRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private GreetingRepository greetingRepository;

    private User testUser;
    private Greeting savedGreeting;

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

        Greeting greeting = new Greeting();
        greeting.setText("Happy Birthday!");
        greeting.setPublic(true);
        greeting.setOwner(testUser);
        greeting.setLikesCount(5);

        savedGreeting = em.persistAndFlush(greeting);
    }

    @DisplayName("должен находить публичные поздравления по лайкам")
    @Test
    void shouldFindAllPublicOrderByLikesDesc() {
        Greeting greeting1 = new Greeting();
        greeting1.setText("Greeting with 10 likes");
        greeting1.setPublic(true);
        greeting1.setOwner(testUser);
        greeting1.setLikesCount(10);
        em.persistAndFlush(greeting1);

        Greeting greeting2 = new Greeting();
        greeting2.setText("Greeting with 2 likes");
        greeting2.setPublic(true);
        greeting2.setOwner(testUser);
        greeting2.setLikesCount(2);
        em.persistAndFlush(greeting2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Greeting> publicGreetings = greetingRepository.findAllPublicOrderByLikesDesc(pageable);

        assertThat(publicGreetings.getContent()).hasSize(3);

        assertThat(publicGreetings.getContent().get(0).getLikesCount()).isEqualTo(10);
        assertThat(publicGreetings.getContent().get(1).getLikesCount()).isEqualTo(5);
        assertThat(publicGreetings.getContent().get(2).getLikesCount()).isEqualTo(2);
    }

    @DisplayName("должен находить поздравления пользователя с загрузкой владельца")
    @Test
    void shouldFindByOwnerWithOwnerGraph() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Greeting> userGreetings = greetingRepository.findByOwner(testUser, pageable);

        assertThat(userGreetings.getContent()).hasSize(1);

        Greeting foundGreeting = userGreetings.getContent().get(0);
        assertThat(foundGreeting.getText()).isEqualTo("Happy Birthday!");
        assertThat(foundGreeting.getOwner()).isNotNull();
        assertThat(foundGreeting.getOwner().getUsername()).isEqualTo("testuser");
    }

    @DisplayName("должен находить поздравления пользователя по публичности")
    @Test
    void shouldFindByOwnerAndIsPublic() {
        Greeting privateGreeting = new Greeting();
        privateGreeting.setText("Private greeting");
        privateGreeting.setPublic(false);
        privateGreeting.setOwner(testUser);
        em.persistAndFlush(privateGreeting);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Greeting> publicGreetings = greetingRepository.findByOwnerAndIsPublic(testUser, true, pageable);
        assertThat(publicGreetings.getContent()).hasSize(1);
        assertThat(publicGreetings.getContent().get(0).getText()).isEqualTo("Happy Birthday!");

        Page<Greeting> privateGreetings = greetingRepository.findByOwnerAndIsPublic(testUser, false, pageable);
        assertThat(privateGreetings.getContent()).hasSize(1);
        assertThat(privateGreetings.getContent().get(0).getText()).isEqualTo("Private greeting");
    }

    @DisplayName("должен сохранять новое поздравление")
    @Test
    void shouldSaveNewGreeting() {
        Greeting newGreeting = new Greeting();
        newGreeting.setText("New Greeting");
        newGreeting.setPublic(false);
        newGreeting.setOwner(testUser);

        Greeting saved = greetingRepository.save(newGreeting);
        em.flush();

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getText()).isEqualTo("New Greeting");

        Greeting foundGreeting = em.find(Greeting.class, saved.getId());
        assertThat(foundGreeting).isNotNull();
        assertThat(foundGreeting.getText()).isEqualTo("New Greeting");
    }

    @DisplayName("должен обновлять существующее поздравление")
    @Test
    void shouldUpdateGreeting() {
        Greeting existingGreeting = greetingRepository.findById(savedGreeting.getId()).orElseThrow();
        existingGreeting.setText("Updated Greeting");
        existingGreeting.setPublic(false);

        Greeting updatedGreeting = greetingRepository.save(existingGreeting);
        em.flush();

        Greeting foundGreeting = em.find(Greeting.class, savedGreeting.getId());

        assertThat(foundGreeting.getText()).isEqualTo("Updated Greeting");
        assertThat(foundGreeting.isPublic()).isFalse();
    }

    @DisplayName("должен удалять поздравление")
    @Test
    void shouldDeleteGreeting() {
        assertThat(greetingRepository.findById(savedGreeting.getId())).isPresent();

        greetingRepository.deleteById(savedGreeting.getId());
        em.flush();

        assertThat(greetingRepository.findById(savedGreeting.getId())).isEmpty();

        Greeting deletedGreeting = em.find(Greeting.class, savedGreeting.getId());
        assertThat(deletedGreeting).isNull();
    }

    @DisplayName("должен поддерживать пагинацию")
    @Test
    void shouldSupportPagination() {
        for (int i = 0; i < 15; i++) {
            Greeting greeting = new Greeting();
            greeting.setText("Greeting " + i);
            greeting.setPublic(true);
            greeting.setOwner(testUser);
            greeting.setLikesCount(i);
            em.persist(greeting);
        }
        em.flush();

        Pageable firstPage = PageRequest.of(0, 10);
        Page<Greeting> firstPageResult = greetingRepository.findAllPublicOrderByLikesDesc(firstPage);

        assertThat(firstPageResult.getContent()).hasSize(10);
        assertThat(firstPageResult.getTotalPages()).isEqualTo(2);
        assertThat(firstPageResult.getTotalElements()).isEqualTo(16); // 15 новых + 1 из BeforeEach

        Pageable secondPage = PageRequest.of(1, 10);
        Page<Greeting> secondPageResult = greetingRepository.findAllPublicOrderByLikesDesc(secondPage);

        assertThat(secondPageResult.getContent()).hasSize(6);
    }

    @DisplayName("не должен включать приватные поздравления в публичные")
    @Test
    void shouldNotIncludePrivateGreetingsInPublicSearch() {
        Greeting privateGreeting = new Greeting();
        privateGreeting.setText("Private greeting with many likes");
        privateGreeting.setPublic(false);
        privateGreeting.setOwner(testUser);
        privateGreeting.setLikesCount(100);
        em.persistAndFlush(privateGreeting);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Greeting> publicGreetings = greetingRepository.findAllPublicOrderByLikesDesc(pageable);

        assertThat(publicGreetings.getContent())
                .hasSize(1)
                .allMatch(Greeting::isPublic);
    }
}

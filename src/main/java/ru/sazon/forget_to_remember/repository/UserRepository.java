package ru.sazon.forget_to_remember.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sazon.forget_to_remember.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @NotNull
    @EntityGraph(attributePaths = {"roles"})
    List<User> findAll();

    @NotNull
    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findById(@NotNull Long id);
}

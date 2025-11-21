package ru.sazon.forget_to_remember.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sazon.forget_to_remember.model.Birthday;
import ru.sazon.forget_to_remember.model.User;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BirthdayRepository extends JpaRepository<Birthday, Long> {
    List<Birthday> findByDate(LocalDate date);

    @EntityGraph(value = "birthday-user-graph", type = EntityGraph.EntityGraphType.FETCH)
    List<Birthday> findByUser(User user);
}

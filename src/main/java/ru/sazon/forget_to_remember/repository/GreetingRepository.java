package ru.sazon.forget_to_remember.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sazon.forget_to_remember.model.Greeting;

@Repository
public interface GreetingRepository extends JpaRepository<Greeting, Long> {
    @Query("SELECT g FROM Greeting g WHERE g.isPublic = true ORDER BY g.likesCount DESC")
    Page<Greeting> findAllPublicOrderByLikesDesc(Pageable pageable);
}

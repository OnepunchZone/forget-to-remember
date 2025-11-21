package ru.sazon.forget_to_remember.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.sazon.forget_to_remember.dto.GreetingDto;
import ru.sazon.forget_to_remember.model.User;

import java.util.List;

public interface GreetingService {
    GreetingDto createGreeting(GreetingDto dto, User currentUser);

    List<GreetingDto> findByOwner(User owner);

    Page<GreetingDto> findPublic(Pageable pageable);
}

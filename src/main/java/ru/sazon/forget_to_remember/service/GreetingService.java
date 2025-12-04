package ru.sazon.forget_to_remember.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import ru.sazon.forget_to_remember.dto.greeting.GreetingCreateDto;
import ru.sazon.forget_to_remember.dto.greeting.GreetingDto;
import ru.sazon.forget_to_remember.dto.greeting.GreetingUpdateDto;
import ru.sazon.forget_to_remember.model.User;

import java.util.List;

public interface GreetingService {
    GreetingDto getById(Long Id);

    GreetingDto createGreeting(GreetingCreateDto dto, User currentUser);

    Page<GreetingDto> findByOwner(User owner, Pageable pageable);

    Page<GreetingDto> findPublic(Pageable pageable);

    GreetingDto createGreetingWithMedia(MultipartFile mediaFile, String text, boolean isPublic, User currentUser);

    GreetingDto addLike(Long greetingId);

    GreetingDto removeLike(Long greetingId);

    GreetingDto updateGreeting(Long id, GreetingUpdateDto dto);

    void deleteGreeting(Long id);
}

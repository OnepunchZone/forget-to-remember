package ru.sazon.forget_to_remember.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.sazon.forget_to_remember.dto.greeting.GreetingCreateDto;
import ru.sazon.forget_to_remember.dto.greeting.GreetingDto;
import ru.sazon.forget_to_remember.dto.greeting.GreetingUpdateDto;
import ru.sazon.forget_to_remember.exeption.EntityNotFoundException;
import ru.sazon.forget_to_remember.mapper.GreetingMapper;
import ru.sazon.forget_to_remember.model.Greeting;
import ru.sazon.forget_to_remember.model.User;
import ru.sazon.forget_to_remember.repository.GreetingRepository;
import ru.sazon.forget_to_remember.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GreetingServiceImpl implements GreetingService {
    private final GreetingRepository greetingRepository;

    private final GreetingMapper greetingMapper;

    private final UserRepository userRepository;

    @Transactional
    @Override
    public GreetingDto createGreeting(GreetingCreateDto dto, User currentUser) {
        Greeting greeting = new Greeting();
        greeting.setText(dto.text());
        greeting.setMediaUrl(dto.mediaUrl());
        greeting.setPublic(dto.isPublic());
        greeting.setOwner(currentUser);
        Greeting saved = greetingRepository.save(greeting);

        return greetingMapper.toDto(saved);
    }

    @Transactional
    @Override
    public GreetingDto updateGreeting(Long id, GreetingUpdateDto dto) {
        Greeting greeting = greetingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Greeting not found with id: " + id));

        greeting.setText(dto.text());
        greeting.setMediaUrl(dto.mediaUrl());
        greeting.setPublic(dto.isPublic());

        Greeting saved = greetingRepository.save(greeting);

        return greetingMapper.toDto(saved);
    }

    @Transactional
    @Override
    public void deleteGreeting(Long id) {
        if (!greetingRepository.existsById(id)) {
            throw new EntityNotFoundException("Greeting not found with id: " + id);
        }
        greetingRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @EntityGraph(value = "greeting-owner-graph", type = EntityGraph.EntityGraphType.FETCH)
    @Override
    public Page<GreetingDto> findByOwner(User owner, Pageable pageable) {
        Page<Greeting> greetings = greetingRepository.findByOwner(owner, pageable);

        return greetings.map(greetingMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<GreetingDto> findPublic(Pageable pageable) {
        Page<Greeting> greetings = greetingRepository.findAllPublicOrderByLikesDesc(pageable);

        return greetings.map(greetingMapper::toDto);
    }

    @Transactional
    @Override
    public GreetingDto createGreetingWithMedia(
            MultipartFile mediaFile, String text, boolean isPublic, User currentUser
    )
    {
        Greeting greeting = new Greeting();
        greeting.setText(text);
        greeting.setPublic(isPublic);
        greeting.setOwner(currentUser);

        if (mediaFile != null && !mediaFile.isEmpty()) {
            try {
                String uploadDir = "uploads/";
                Files.createDirectories(Paths.get(uploadDir));
                String fileName = UUID.randomUUID() + "_" + mediaFile.getOriginalFilename();
                Path filePath = Paths.get(uploadDir + fileName);
                mediaFile.transferTo(filePath);
                greeting.setMediaUrl(uploadDir + fileName);
            } catch (IOException e) {
                throw new RuntimeException("File upload failed: " + e.getMessage());
            }
        }

        Greeting saved = greetingRepository.save(greeting);

        return greetingMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    @EntityGraph(value = "greeting-owner-graph", type = EntityGraph.EntityGraphType.FETCH)
    public GreetingDto getById(Long id) {
        Greeting greeting = greetingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Greeting not found with id: " + id));

        return greetingMapper.toDto(greeting);
    }

    @Transactional
    @Override
    public GreetingDto addLike(Long greetingId) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + currentUsername));

        Greeting greeting = greetingRepository.findById(greetingId)
                .orElseThrow(() -> new EntityNotFoundException("Greeting not found: " + greetingId));

        greeting.addLike(currentUser);
        Greeting saved = greetingRepository.save(greeting);

        return greetingMapper.toDto(saved);
    }

    @Transactional
    @Override
    public GreetingDto removeLike(Long greetingId) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + currentUsername));

        Greeting greeting = greetingRepository.findById(greetingId)
                .orElseThrow(() -> new EntityNotFoundException("Greeting not found: " + greetingId));

        greeting.removeLike(currentUser);
        Greeting saved = greetingRepository.save(greeting);

        return greetingMapper.toDto(saved);
    }
}

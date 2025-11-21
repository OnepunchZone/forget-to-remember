package ru.sazon.forget_to_remember.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sazon.forget_to_remember.dto.GreetingDto;
import ru.sazon.forget_to_remember.mapper.GreetingMapper;
import ru.sazon.forget_to_remember.model.Greeting;
import ru.sazon.forget_to_remember.model.User;
import ru.sazon.forget_to_remember.repository.GreetingRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GreetingServiceImpl implements GreetingService {
    private final GreetingRepository greetingRepository;

    private final GreetingMapper greetingMapper;

    @Transactional
    @Override
    public GreetingDto createGreeting(GreetingDto dto, User currentUser) {
        Greeting greeting = new Greeting();
        greeting.setText(dto.text());
        greeting.setMediaUrl(dto.mediaUrl());
        greeting.setPublic(dto.isPublic());
        greeting.setOwner(currentUser);
        Greeting saved = greetingRepository.save(greeting);
        return greetingMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    @EntityGraph(value = "greeting-owner-graph", type = EntityGraph.EntityGraphType.FETCH)
    @Override
    public List<GreetingDto> findByOwner(User owner) {
        List<Greeting> greetings = greetingRepository.findByOwner(owner);
        return greetings.stream().map(greetingMapper::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public Page<GreetingDto> findPublic(Pageable pageable) {
        Page<Greeting> greetings = greetingRepository.findAllPublicOrderByLikesDesc(pageable);
        return greetings.map(greetingMapper::toDto);
    }
}

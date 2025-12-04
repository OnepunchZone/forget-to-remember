package ru.sazon.forget_to_remember.mapper;

import org.springframework.stereotype.Component;
import ru.sazon.forget_to_remember.dto.greeting.GreetingDto;
import ru.sazon.forget_to_remember.model.Greeting;

@Component
public class GreetingMapperImpl implements GreetingMapper {

    @Override
    public GreetingDto toDto(Greeting greeting) {
        return new GreetingDto(
                greeting.getId(),
                greeting.getText(),
                greeting.getMediaUrl(),
                greeting.isPublic(),
                greeting.getLikesCount(),
                greeting.getOwner().getId());
    }

    @Override
    public Greeting toEntity(GreetingDto greetingDto) {
        Greeting greeting = new Greeting();
        greeting.setId(greetingDto.id());
        greeting.setText(greetingDto.text());
        greeting.setMediaUrl(greetingDto.mediaUrl());
        greeting.setPublic(greetingDto.isPublic());
        greeting.setLikesCount(greetingDto.likesCount());

        /*if (greetingDto.ownerId() != null) {
            User owner = userRepository.findById(greetingDto.ownerId())
                    .orElseThrow(() -> new EntityNotFoundException("Owner not found with id: " + greetingDto.ownerId()));
            greeting.setOwner(owner);
        }*/

        return greeting;
    }
}

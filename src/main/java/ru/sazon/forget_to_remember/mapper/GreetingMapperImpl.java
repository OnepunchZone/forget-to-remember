package ru.sazon.forget_to_remember.mapper;

import org.springframework.stereotype.Component;
import ru.sazon.forget_to_remember.dto.GreetingDto;
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
}

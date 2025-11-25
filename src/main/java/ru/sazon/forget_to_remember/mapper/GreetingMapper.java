package ru.sazon.forget_to_remember.mapper;

import ru.sazon.forget_to_remember.dto.GreetingDto;
import ru.sazon.forget_to_remember.model.Greeting;

public interface GreetingMapper {
    GreetingDto toDto(Greeting greeting);

    Greeting toEntity(GreetingDto greetingDto);
}

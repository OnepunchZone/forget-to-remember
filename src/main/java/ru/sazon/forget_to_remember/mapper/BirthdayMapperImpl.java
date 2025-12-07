package ru.sazon.forget_to_remember.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sazon.forget_to_remember.dto.birthday.BirthdayDto;
import ru.sazon.forget_to_remember.dto.greeting.GreetingDto;
import ru.sazon.forget_to_remember.model.Birthday;

@Component
@RequiredArgsConstructor
public class BirthdayMapperImpl implements BirthdayMapper {
    private final GreetingMapper greetingMapper;

    @Override
    public BirthdayDto toDto(Birthday birthday) {
        if (birthday == null) {
            return null;
        }

        GreetingDto greetingDto = birthday.getGreeting() != null
                ? greetingMapper.toDto(birthday.getGreeting()) : null;

        return new BirthdayDto(
                birthday.getId(),
                birthday.getName(),
                birthday.getDate(),
                birthday.getContact(),
                birthday.getUser().getId(),
                greetingDto
        );
    }
}

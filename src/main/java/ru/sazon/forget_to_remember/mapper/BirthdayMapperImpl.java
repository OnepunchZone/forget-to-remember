package ru.sazon.forget_to_remember.mapper;

import org.springframework.stereotype.Component;
import ru.sazon.forget_to_remember.dto.birthday.BirthdayDto;
import ru.sazon.forget_to_remember.model.Birthday;

@Component
public class BirthdayMapperImpl implements BirthdayMapper {

    @Override
    public BirthdayDto toDto(Birthday birthday) {
        return new BirthdayDto(
                birthday.getId(),
                birthday.getName(),
                birthday.getDate(),
                birthday.getContact(),
                birthday.getUser().getId());
    }
}

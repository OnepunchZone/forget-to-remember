package ru.sazon.forget_to_remember.mapper;

import ru.sazon.forget_to_remember.dto.birthday.BirthdayDto;
import ru.sazon.forget_to_remember.model.Birthday;

public interface BirthdayMapper {
    BirthdayDto toDto(Birthday birthday);
}

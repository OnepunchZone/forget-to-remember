package ru.sazon.forget_to_remember.service;

import ru.sazon.forget_to_remember.dto.BirthdayDto;
import ru.sazon.forget_to_remember.model.User;

import java.time.LocalDate;
import java.util.List;

public interface BirthdayService {
    BirthdayDto createBirthday(BirthdayDto dto, User currentUser);

    List<BirthdayDto> findByUser(User user);

    List<BirthdayDto> findByDate(LocalDate date);

    BirthdayDto updateBirthday(Long id, BirthdayDto dto);
}

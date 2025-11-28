package ru.sazon.forget_to_remember.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sazon.forget_to_remember.dto.BirthdayDto;
import ru.sazon.forget_to_remember.exeption.EntityNotFoundException;
import ru.sazon.forget_to_remember.mapper.BirthdayMapper;
import ru.sazon.forget_to_remember.model.Birthday;
import ru.sazon.forget_to_remember.model.User;
import ru.sazon.forget_to_remember.repository.BirthdayRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BirthdayServiceImpl implements BirthdayService {
    private final BirthdayRepository birthdayRepository;

    private final BirthdayMapper birthdayMapper;

    private final NotificationService notificationService;

    @Transactional
    @Override
    public BirthdayDto createBirthday(BirthdayDto dto, User currentUser) {
        Birthday birthday = new Birthday();
        birthday.setName(dto.name());
        birthday.setDate(dto.date());
        birthday.setContact(dto.contact());
        birthday.setUser(currentUser);
        Birthday saved = birthdayRepository.save(birthday);
        return birthdayMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BirthdayDto> findByUser(User user) {
        List<Birthday> birthdays = birthdayRepository.findByUser(user);
        return birthdays.stream().map(birthdayMapper::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<BirthdayDto> findByDate(LocalDate date) {
        List<Birthday> birthdays = birthdayRepository.findByDate(date);
        return birthdays.stream().map(birthdayMapper::toDto).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public BirthdayDto updateBirthday(Long id, BirthdayDto dto) {
        Birthday birthday = birthdayRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Birthday not found: " + id));
        birthday.setName(dto.name());
        birthday.setDate(dto.date());
        birthday.setContact(dto.contact());
        Birthday saved = birthdayRepository.save(birthday);
        return birthdayMapper.toDto(saved);
    }

    @Scheduled(cron = "0 * * * * ?") // запуск каждую минуту (cron = "0 * * * * ?"), в 9:00 - (cron = "0 0 9 * * ?")
    @Transactional
    public void checkBirthdays() {
        LocalDate today = LocalDate.now();
        List<Birthday> birthdaysToday = birthdayRepository.findByDate(today);

        if (birthdaysToday.isEmpty()) return;

        Map<User, List<Birthday>> birthdaysByUser = birthdaysToday.stream()
                .collect(Collectors.groupingBy(Birthday::getUser));

        birthdaysByUser.forEach(notificationService::sendBirthdayNotification);
    }
}

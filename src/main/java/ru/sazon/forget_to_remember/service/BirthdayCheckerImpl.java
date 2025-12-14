package ru.sazon.forget_to_remember.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sazon.forget_to_remember.model.Birthday;
import ru.sazon.forget_to_remember.model.Greeting;
import ru.sazon.forget_to_remember.model.User;
import ru.sazon.forget_to_remember.repository.BirthdayRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BirthdayCheckerImpl implements BirthdayChecker {
    private final BirthdayRepository birthdayRepository;

    private final NotificationService notificationService;

    @Override
    @Scheduled(cron = "0 */10 * * * ?") // запуск каждую минуту (cron = "0 * * * * ?"), в 9:00 - (cron = "0 0 9 * * ?")
    @Transactional
    public void checkBirthdays() {
        LocalDate today = LocalDate.now();
        List<Birthday> birthdaysToday = birthdayRepository.findByDate(today);

        if (birthdaysToday.isEmpty()) return;

        Map<User, List<Birthday>> birthdaysByUser = birthdaysToday.stream()
                .collect(Collectors.groupingBy(Birthday::getUser));

        birthdaysByUser.forEach((user, birthdays) -> {
            Map<Birthday, Greeting> birthdaysWithGreetings = birthdays.stream()
                    .collect(Collectors.toMap(b -> b, Birthday::getGreeting));

            if (!birthdaysWithGreetings.isEmpty()) {
                notificationService.sendBirthdayNotification(user, birthdaysWithGreetings, birthdaysToday);
            }
        });
    }
}

package ru.sazon.forget_to_remember.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sazon.forget_to_remember.model.Birthday;
import ru.sazon.forget_to_remember.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService{
    private final TelegramBotService telegramBotService;

    @Override
    public void sendBirthdayNotification(User user, List<Birthday> birthdays) {
        String chatId = user.getContact();

        if (chatId == null || chatId.isEmpty()) {
            log.warn("No contact found for user: {}", user.getUsername());
            return;
        }

        List<TelegramBotService.BirthdayInfo> birthdayInfos = birthdays.stream()
                .map(birthday -> new TelegramBotService.BirthdayInfo(
                        birthday.getName(),
                        birthday.getContact()
                ))
                .collect(Collectors.toList());

        telegramBotService.sendBirthdayNotification(chatId, birthdayInfos);

        log.info("Birthday notification processed for user: {}", user.getUsername());
    }

    public void sendWelcomeNotification(User user) {
        String chatId = user.getContact();
        if (chatId != null && !chatId.isEmpty()) {
            String welcomeMessage = "👋 Добро пожаловать в *Forget To Remember*!\n\n" +
                    "Я буду напоминать тебе о днях рождениях твоих друзей и близких. " +
                    "Ты можешь создавать шаблоны поздравлений и быстро отправлять их!";

            telegramBotService.sendMessageWithKeyboard(chatId, welcomeMessage,
                    telegramBotService.createMainMenuKeyboard());
        }
    }

    public void sendReminderNotification(User user, List<Birthday> upcomingBirthdays) {
        String chatId = user.getContact();
        if (chatId != null && !chatId.isEmpty() && !upcomingBirthdays.isEmpty()) {
            StringBuilder reminderMessage = new StringBuilder("🔔 *Напоминание!*\n\n" +
                    "Скоро дни рождения у " + upcomingBirthdays.size() + " человек:\n\n");

            for (int i = 0; i < Math.min(upcomingBirthdays.size(), 5); i++) {
                Birthday birthday = upcomingBirthdays.get(i);
                reminderMessage.append("• ").append(birthday.getName()).append("\n");
            }

            if (upcomingBirthdays.size() > 5) {
                reminderMessage.append("\n... и еще ").append(upcomingBirthdays.size() - 5).append(" человек");
            }

            reminderMessage.append("\nНе забудь подготовить поздравления! 🎁");

            telegramBotService.sendMessageWithKeyboard(chatId, reminderMessage.toString(),
                    telegramBotService.createGreetingsKeyboard());
        }
    }
}

package ru.sazon.forget_to_remember.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.sazon.forget_to_remember.bot.BotSender;
import ru.sazon.forget_to_remember.model.Birthday;
import ru.sazon.forget_to_remember.model.Greeting;
import ru.sazon.forget_to_remember.model.User;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Сервис уведомлений")
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private BotSender botSender;

    @Mock
    private KeyboardService keyboardService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User testUser;
    private Birthday testBirthday;
    private Greeting testGreeting;
    private InlineKeyboardMarkup testKeyboard;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setContact("123456789");

        testBirthday = new Birthday();
        testBirthday.setId(1L);
        testBirthday.setName("John Doe");
        testBirthday.setDate(LocalDate.now());
        testBirthday.setContact("@john_telegram");
        testBirthday.setUser(testUser);

        testGreeting = new Greeting();
        testGreeting.setId(1L);
        testGreeting.setText("Happy Birthday!");

        testKeyboard = new InlineKeyboardMarkup();
    }

    @DisplayName("должен отправлять уведомление о дне рождения")
    @Test
    void shouldSendBirthdayNotification() {
        Map<Birthday, Greeting> birthdaysWithGreetings = new HashMap<>();
        birthdaysWithGreetings.put(testBirthday, testGreeting);

        List<Birthday> birthdays = List.of(testBirthday);

        when(keyboardService.createBirthdayNotificationKeyboard()).thenReturn(testKeyboard);

        notificationService.sendBirthdayNotification(testUser, birthdaysWithGreetings, birthdays);

        verify(botSender, times(1)).sendMessage(
                eq(testUser.getContact()),
                contains("Сегодня дни рождения: 1 человек")
        );

        verify(botSender, times(1)).sendMessageWithKeyboard(
                eq(testUser.getContact()),
                contains("С днем рождения, " + testBirthday.getName()),
                eq(testKeyboard)
        );
    }

    @DisplayName("должен отправлять напоминание о предстоящих днях рождения")
    @Test
    void shouldSendReminderNotification() {
        Birthday birthday1 = new Birthday();
        birthday1.setId(1L);
        birthday1.setName("John");

        Birthday birthday2 = new Birthday();
        birthday2.setId(2L);
        birthday2.setName("Jane");

        Birthday birthday3 = new Birthday();
        birthday3.setId(3L);
        birthday3.setName("Bob");

        List<Birthday> upcomingBirthdays = List.of(birthday1, birthday2, birthday3);

        when(keyboardService.createBirthdayNotificationKeyboard()).thenReturn(testKeyboard);

        notificationService.sendReminderNotification(testUser, upcomingBirthdays);

        verify(botSender, times(1)).sendMessageWithKeyboard(
                eq(testUser.getContact()),
                argThat(message ->
                        message.contains("Напоминание!") &&
                                message.contains("Скоро дни рождения у 3 человек") &&
                                message.contains("John") &&
                                message.contains("Jane") &&
                                message.contains("Bob")
                ),
                eq(testKeyboard)
        );
    }
}

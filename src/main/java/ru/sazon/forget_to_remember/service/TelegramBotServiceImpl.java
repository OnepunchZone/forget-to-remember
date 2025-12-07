package ru.sazon.forget_to_remember.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramBotServiceImpl implements TelegramBotService {

    private final AbsSender bot;

    private final KeyboardService keyboardService;

    @Override
    public void sendMessage(String chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        executeMessage(message);
    }

    @Override
    public void sendMessageWithKeyboard(String chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard)
                .build();
        executeMessage(message);
    }

    @Override
    public void sendBirthdayNotification(String chatId, List<BirthdayInfo> birthdays) {
        if (birthdays.isEmpty()) {
            sendMessage(chatId, "Сегодня нет дней рождения! 🎉");

            return;
        }

        StringBuilder messageText = new StringBuilder();
        messageText.append("🎂 *Сегодня дни рождения:* ").append(birthdays.size()).append(" человек!\n\n");

        for (int i = 0; i < birthdays.size(); i++) {
            BirthdayInfo birthday = birthdays.get(i);
            messageText.append(i + 1).append(". *").append(birthday.name()).append("*");

            if (birthday.contact() != null && !birthday.contact().isEmpty()) {
                messageText.append(" - ").append(birthday.contact());
            }
            messageText.append("\n");
        }

        messageText.append("\nВыбери действие:");

        sendMessageWithKeyboard(
                chatId,
                messageText.toString(),
                keyboardService.createBirthdayNotificationKeyboard()
        );
    }

    private void executeMessage(SendMessage message) {
        try {
            bot.execute(message);
            log.debug("Message sent successfully to: {}", message.getChatId());
        } catch (TelegramApiException e) {
            log.error("Failed to send message to {}: {}", message.getChatId(), e.getMessage());
        }
    }
}

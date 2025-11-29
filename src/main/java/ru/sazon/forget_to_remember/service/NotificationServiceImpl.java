package ru.sazon.forget_to_remember.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.sazon.forget_to_remember.model.Birthday;
import ru.sazon.forget_to_remember.model.User;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService{
    private final AbsSender bot;

    @Value("${bot.name}")
    private String botName;

    @Override
    public void sendBirthdayNotification(User user, List<Birthday> birthdays) {
        String chatId = user.getContact();

        if (chatId == null || chatId.isEmpty()) return;

        String message = "Сегодня дни рождения: " + birthdays.size() + " человек!\n";
        for (Birthday b : birthdays) {
            message += "• " + b.getName() + " (" + b.getContact() + ")\n";
        }
        message += "Выбери шаблон и отправь!";

        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .replyMarkup(createInlineKeyboard())
                .build();

        try {
            bot.execute(sendMessage);
        } catch (Exception e) {
            System.err.println("Notification send failed for " + chatId + ": " + e.getMessage());
        }
    }

    private InlineKeyboardMarkup createInlineKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder().text("Поиск шаблонов").callbackData("search_greetings").build());
        row.add(InlineKeyboardButton.builder().text("Создать новое").callbackData("create_greeting").build());
        rows.add(row);
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }
}

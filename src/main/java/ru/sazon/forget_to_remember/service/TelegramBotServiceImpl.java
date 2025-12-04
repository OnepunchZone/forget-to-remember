package ru.sazon.forget_to_remember.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramBotServiceImpl implements TelegramBotService {

    private final AbsSender bot;;

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

        sendMessageWithKeyboard(chatId, messageText.toString(), createGreetingsKeyboard());
    }

    @Override
    public InlineKeyboardMarkup createGreetingsKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> firstRow = new ArrayList<>();
        firstRow.add(InlineKeyboardButton.builder()
                .text("🔍 Поиск шаблонов")
                .callbackData("search_greetings")
                .build());
        firstRow.add(InlineKeyboardButton.builder()
                .text("📝 Создать новое")
                .callbackData("create_greeting")
                .build());

        List<InlineKeyboardButton> secondRow = new ArrayList<>();
        secondRow.add(InlineKeyboardButton.builder()
                .text("⭐ Популярные")
                .callbackData("popular_greetings")
                .build());
        secondRow.add(InlineKeyboardButton.builder()
                .text("🕐 Недавние")
                .callbackData("recent_greetings")
                .build());

        keyboard.add(firstRow);
        keyboard.add(secondRow);

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    @Override
    public InlineKeyboardMarkup createMainMenuKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text("📅 Мои дни рождения")
                .callbackData("my_birthdays")
                .build());
        row1.add(InlineKeyboardButton.builder()
                .text("💬 Мои поздравления")
                .callbackData("my_greetings")
                .build());

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder()
                .text("⚙️ Настройки")
                .callbackData("settings")
                .build());
        row2.add(InlineKeyboardButton.builder()
                .text("ℹ️ Помощь")
                .callbackData("help")
                .build());

        keyboard.add(row1);
        keyboard.add(row2);

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    @Override
    public InlineKeyboardMarkup createPaginationKeyboard(int currentPage, int totalPages, String callbackPrefix) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> paginationRow = new ArrayList<>();

        if (currentPage > 1) {
            paginationRow.add(InlineKeyboardButton.builder()
                    .text("◀️ Назад")
                    .callbackData(callbackPrefix + "_page_" + (currentPage - 1))
                    .build());
        }

        paginationRow.add(InlineKeyboardButton.builder()
                .text(currentPage + "/" + totalPages)
                .callbackData("current_page")
                .build());

        if (currentPage < totalPages) {
            paginationRow.add(InlineKeyboardButton.builder()
                    .text("Вперед ▶️")
                    .callbackData(callbackPrefix + "_page_" + (currentPage + 1))
                    .build());
        }

        List<InlineKeyboardButton> backRow = new ArrayList<>();
        backRow.add(InlineKeyboardButton.builder()
                .text("🏠 Главное меню")
                .callbackData("main_menu")
                .build());

        keyboard.add(paginationRow);
        keyboard.add(backRow);

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
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

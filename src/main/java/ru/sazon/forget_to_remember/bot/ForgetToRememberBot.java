package ru.sazon.forget_to_remember.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ForgetToRememberBot extends TelegramLongPollingBot {
    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.name}")
    private String botUsername;

    public ForgetToRememberBot() {
        super();
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleTextMessage(update);
        }

        if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
        }
    }

    private void handleTextMessage(Update update) {
        String messageText = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        switch (messageText) {
            case "/start":
                sendMessageWithKeyboard(chatId.toString(),
                        "Привет! Я бот Forget To Remember. Выбери действие:",
                        createMainMenuKeyboard());
                break;
            case "/help":
                sendMessage(chatId.toString(),
                        "Помощь по боту:\n\n" +
                                "/start - Главное меню\n" +
                                "/help - Справка\n\n" +
                                "Я помогу не забыть о днях рождениях! 🎂");
                break;
            default:
                sendMessage(chatId.toString(),
                        "Не понимаю команду. Используй /start или /help");
        }
    }

    private void handleCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String messageText;

        switch (callbackData) {
            case "search_greetings":
                messageText = "🔍 Поиск шаблонов поздравлений...";
                break;
            case "create_greeting":
                messageText = "📝 Создание нового поздравления...";
                break;
            case "main_menu":
                messageText = "🏠 Главное меню:";
                sendMessageWithKeyboard(chatId.toString(), messageText, createMainMenuKeyboard());
                return;
            default:
                messageText = "Команда в разработке: " + callbackData;
        }

        sendMessage(chatId.toString(), messageText);
    }

    // Вспомогательные методы для отправки сообщений
    private void sendMessage(String chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        executeMessage(message);
    }

    private void sendMessageWithKeyboard(String chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard)
                .build();
        executeMessage(message);
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
            log.debug("Message sent successfully to: {}", message.getChatId());
        } catch (TelegramApiException e) {
            log.error("Failed to send message to {}: {}", message.getChatId(), e.getMessage());
        }
    }

    // Методы создания клавиатур (дублируются из TelegramBotService)
    private InlineKeyboardMarkup createMainMenuKeyboard() {
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
}

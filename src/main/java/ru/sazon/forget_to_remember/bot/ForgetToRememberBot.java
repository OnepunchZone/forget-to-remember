package ru.sazon.forget_to_remember.bot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class ForgetToRememberBot extends TelegramLongPollingBot {
    private final String botUsername;
    private final String botToken;

    public ForgetToRememberBot(String botUsername, String botToken) {
        this.botUsername = botUsername;
        this.botToken = botToken;
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
        // Обработка callback (e.g., "send" — отправить шаблон)
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            if ("send".equals(callbackData)) {
                // Логика отправки
            }
        }
    }
}

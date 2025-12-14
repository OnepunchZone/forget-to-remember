package ru.sazon.forget_to_remember.bot;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.Serializable;

@Slf4j
@Getter
@Setter
@Component
public class ForgetToRememberBot extends TelegramLongPollingBot implements BotSender {
    private String botToken;

    private String botUsername;

    private BotCommandHandler botCommandHandler;

    public ForgetToRememberBot(@Value("${bot.token}") String botToken,
                               @Value("${bot.name}") String botUsername,
                               BotCommandHandler botCommandHandler) {
        super(botToken);
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.botCommandHandler = botCommandHandler;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.debug("Update received: {}", update);

        try {
            log.info("=== NEW UPDATE ===");

            if (update.hasMessage() && update.getMessage().hasText()) {
                String text = update.getMessage().getText();
                Long chatId = update.getMessage().getChatId();
                log.info("Text message from chat {}: {}", chatId, text);
                botCommandHandler.handleTextMessage(update, this);
            } else if (update.hasCallbackQuery()) {
                String callbackData = update.getCallbackQuery().getData();
                Long chatId = update.getCallbackQuery().getMessage().getChatId();
                log.info("Callback from chat {}: {}", chatId, callbackData);
                botCommandHandler.handleCallbackQuery(update, this);
            } else {
                log.warn("Unhandled update type");
            }
        } catch (Exception e) {
            log.error("Error processing update: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendMessage(String chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        executeWithLogging(message, "Message sent to: " + chatId);
    }

    @Override
    public void sendMessageWithKeyboard(String chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard)
                .build();
        executeWithLogging(message, "Message with keyboard sent to: " + chatId);
    }

    @Override
    public void answerCallbackQuery(String callbackQueryId) {
        AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .build();
        executeWithLogging(answer, "Answered callback query: " + callbackQueryId);
    }

    @Override
    public <T extends Serializable, M extends BotApiMethod<T>> T execute(M method) {
        try {
            T result = super.execute(method);
            log.debug("Executed {}: {}", method.getClass().getSimpleName(), method);
            return result;
        } catch (TelegramApiException e) {
            log.error("Failed to execute {}: {}", method.getClass().getSimpleName(), e.getMessage());
            throw new RuntimeException("Telegram API error", e);
        }
    }

    private void executeWithLogging(BotApiMethod<?> method, String successMessage) {
        try {
            super.execute(method);
            log.debug(successMessage);
        } catch (TelegramApiException e) {
            log.error("Error executing {}: {}", method.getClass().getSimpleName(), e.getMessage());
            throw new RuntimeException("Telegram API error", e);
        }
    }

    @PostConstruct
    public void init() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
            log.info("Bot registered successfully: {}", botUsername);
        } catch (TelegramApiException e) {
            log.error("Failed to register bot: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}

package ru.sazon.forget_to_remember.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.sazon.forget_to_remember.bot.ForgetToRememberBot;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BotConfig {
    public ForgetToRememberBot forgetToRememberBot(TelegramBotsApi telegramBotsApi, ForgetToRememberBot bot) throws TelegramApiException {
        try {
            telegramBotsApi.registerBot(bot);
            log.info("Bot successfully registered: {}", bot.getBotUsername());
        } catch (TelegramApiException e) {
            if (e.getMessage().contains("404")) {
                log.info("No old webhook found (404) — switching to polling. Bot: {}", bot.getBotUsername());
            } else {
                log.error("Failed to register bot: {}", e.getMessage());
                throw e;
            }
        }
        return bot;
    }
}

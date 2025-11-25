package ru.sazon.forget_to_remember.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.sazon.forget_to_remember.bot.ForgetToRememberBot;

@Configuration
public class BotConfig {
    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String botToken;

    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(DefaultBotSession.class);
    }

    @Bean
    public ForgetToRememberBot forgetToRememberBot(TelegramBotsApi telegramBotsApi) throws TelegramApiException {
        ForgetToRememberBot bot = new ForgetToRememberBot(botName, botToken);
        telegramBotsApi.registerBot(bot);
        return bot;
    }
}

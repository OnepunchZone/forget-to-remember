package ru.sazon.forget_to_remember.service;

import ru.sazon.forget_to_remember.bot.BotSender;
import ru.sazon.forget_to_remember.model.Greeting;

public interface SendService {
    void sendGreeting(Greeting greeting, String chatId, boolean preview, BotSender bot);
}

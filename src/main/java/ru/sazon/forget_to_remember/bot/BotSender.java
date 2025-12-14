package ru.sazon.forget_to_remember.bot;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.io.Serializable;

public interface BotSender {
    void sendMessage(String chatId, String text);

    void sendMessageWithKeyboard(String chatId, String text, InlineKeyboardMarkup keyboard);

    void answerCallbackQuery(String callbackQueryId);

    <T extends Serializable, M extends BotApiMethod<T>> T execute(M method);
}

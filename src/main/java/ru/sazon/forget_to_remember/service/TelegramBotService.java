package ru.sazon.forget_to_remember.service;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

public interface TelegramBotService {
    void sendMessage(String chatId, String text);
    void sendMessageWithKeyboard(String chatId, String text, InlineKeyboardMarkup keyboard);
    void sendBirthdayNotification(String chatId, List<BirthdayInfo> birthdays);

    // Вспомогательные методы для создания клавиатур
    InlineKeyboardMarkup createGreetingsKeyboard();
    InlineKeyboardMarkup createMainMenuKeyboard();
    InlineKeyboardMarkup createPaginationKeyboard(int currentPage, int totalPages, String callbackPrefix);

    record BirthdayInfo(String name, String contact) {}
}

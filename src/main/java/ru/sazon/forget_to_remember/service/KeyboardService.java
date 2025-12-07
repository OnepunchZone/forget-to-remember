package ru.sazon.forget_to_remember.service;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public interface KeyboardService {
    InlineKeyboardMarkup createBirthdayNotificationKeyboard();

    InlineKeyboardMarkup createMainMenuKeyboard();

    InlineKeyboardMarkup createPaginationKeyboard(int currentPage, int totalPages, String callbackPrefix);

    InlineKeyboardMarkup createMyGreetingsKeyboard();

    InlineKeyboardMarkup createSingleButton(String buttonText, String callbackData);
}

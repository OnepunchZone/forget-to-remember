package ru.sazon.forget_to_remember.service;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.sazon.forget_to_remember.dto.birthday.BirthdayDto;
import ru.sazon.forget_to_remember.dto.greeting.GreetingDto;

import java.util.List;

public interface KeyboardService {
    InlineKeyboardMarkup createBirthdayNotificationKeyboard();

    InlineKeyboardMarkup createMainMenuKeyboard();

    InlineKeyboardMarkup createPaginationKeyboard(int currentPage, int totalPages, String callbackPrefix);

    InlineKeyboardMarkup createMyGreetingsKeyboard();

    InlineKeyboardMarkup createSingleButton(String buttonText, String callbackData);

    InlineKeyboardMarkup createEditSelectionKeyboard(List<GreetingDto> userGreetings);

    InlineKeyboardMarkup createSendSelectionKeyboard(
            List<GreetingDto> userGreetings, List<GreetingDto> publicGreetings
    );

    InlineKeyboardMarkup createContactSelectionKeyboard(List<BirthdayDto> birthdays);

    InlineKeyboardMarkup createShare(String url);
}

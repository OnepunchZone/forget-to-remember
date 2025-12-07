package ru.sazon.forget_to_remember.service;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
public class KeyboardServiceImpl implements KeyboardService {
    @Override
    public InlineKeyboardMarkup createBirthdayNotificationKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> firstRow = new ArrayList<>();
        firstRow.add(InlineKeyboardButton.builder()
                .text("🔍 Поиск шаблонов")
                .callbackData("search_popular_greetings")
                .build());
        firstRow.add(InlineKeyboardButton.builder()
                .text("📝 Создать новое")
                .callbackData("create_greeting")
                .build());

        List<InlineKeyboardButton> secondRow = new ArrayList<>();
        secondRow.add(InlineKeyboardButton.builder()
                .text("💬 Мои шаблоны")
                .callbackData("my_private_greetings")
                .build());

        keyboard.add(firstRow);
        keyboard.add(secondRow);

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    @Override
    public InlineKeyboardMarkup createMainMenuKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text("📅 Мои дни рождения")
                .callbackData("my_birthdays")
                .build());
        row1.add(InlineKeyboardButton.builder()
                .text("💬 Мои поздравления")
                .callbackData("my_private_greetings")
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

    @Override
    public InlineKeyboardMarkup createPaginationKeyboard(int currentPage, int totalPages, String callbackPrefix) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> paginationRow = new ArrayList<>();

        if (currentPage > 1) {
            paginationRow.add(InlineKeyboardButton.builder()
                    .text("◀️ Назад")
                    .callbackData(callbackPrefix + "_page_" + (currentPage - 1))
                    .build());
        }

        paginationRow.add(InlineKeyboardButton.builder()
                .text(currentPage + "/" + totalPages)
                .callbackData("current_page")
                .build());

        if (currentPage < totalPages) {
            paginationRow.add(InlineKeyboardButton.builder()
                    .text("Вперед ▶️")
                    .callbackData(callbackPrefix + "_page_" + (currentPage + 1))
                    .build());
        }

        List<InlineKeyboardButton> backRow = new ArrayList<>();
        backRow.add(InlineKeyboardButton.builder()
                .text("🏠 Главное меню")
                .callbackData("main_menu")
                .build());

        keyboard.add(paginationRow);
        keyboard.add(backRow);

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    @Override
    public InlineKeyboardMarkup createMyGreetingsKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text("✏️ Редактировать")
                .callbackData("edit_my_greeting")
                .build());
        row1.add(InlineKeyboardButton.builder()
                .text("🗑️ Удалить")
                .callbackData("delete_my_greeting")
                .build());

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder()
                .text("🌐 Сделать публичным")
                .callbackData("make_greeting_public")
                .build());
        row2.add(InlineKeyboardButton.builder()
                .text("🏠 Главное меню")
                .callbackData("main_menu")
                .build());

        keyboard.add(row1);
        keyboard.add(row2);

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    @Override
    public InlineKeyboardMarkup createSingleButton(String buttonText, String callbackData) {
        InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text(buttonText)
                .callbackData(callbackData)
                .build();

        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(button))
                .build();
    }
}

package ru.sazon.forget_to_remember.service;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.sazon.forget_to_remember.dto.birthday.BirthdayDto;
import ru.sazon.forget_to_remember.dto.greeting.GreetingDto;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
        secondRow.add(InlineKeyboardButton.builder()
                .text("✏️ Редактировать сообщение")
                .callbackData("edit")
                .build());

        List<InlineKeyboardButton> thirdRow = new ArrayList<>();
        thirdRow.add(InlineKeyboardButton.builder()
                .text("📤 Отправить поздравление по chatId")
                .callbackData("send")
                .build());

        keyboard.add(firstRow);
        keyboard.add(secondRow);
        keyboard.add(thirdRow);

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

        /*List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text("✏️ Редактировать")
                .callbackData("edit")
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
        keyboard.add(row2);*/

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    @Override
    public InlineKeyboardMarkup createEditSelectionKeyboard(List<GreetingDto> userGreetings) {
        try {
            if (userGreetings == null || userGreetings.isEmpty()) {
                return createSingleButton("📭 У вас пока нет сообщений", "main_menu");
            }

            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

            for (int i = 0; i < userGreetings.size(); i += 2) {
                List<InlineKeyboardButton> row = new ArrayList<>();

                GreetingDto greeting1 = userGreetings.get(i);
                String text1 = (greeting1.isPublic() ? "🌐 " : "🔒 ") +
                        truncateText(greeting1.text(), 15) + " (ID: " + greeting1.id() + ")";
                row.add(InlineKeyboardButton.builder()
                        .text(text1)
                        .callbackData("edit_" + greeting1.id())
                        .build());

                if (i + 1 < userGreetings.size()) {
                    GreetingDto greeting2 = userGreetings.get(i + 1);
                    String text2 = (greeting2.isPublic() ? "🌐 " : "🔒 ") +
                            truncateText(greeting2.text(), 15) + " (ID: " + greeting2.id() + ")";
                    row.add(InlineKeyboardButton.builder()
                            .text(text2)
                            .callbackData("edit_" + greeting2.id())
                            .build());
                }

                keyboard.add(row);
            }

            List<InlineKeyboardButton> backRow = new ArrayList<>();
            backRow.add(InlineKeyboardButton.builder()
                    .text("🏠 Главное меню")
                    .callbackData("main_menu")
                    .build());
            keyboard.add(backRow);

            return InlineKeyboardMarkup.builder().keyboard(keyboard).build();

        } catch (Exception e) {
            return createSingleButton("❌ Ошибка загрузки", "main_menu");
        }
    }

    @Override
    public InlineKeyboardMarkup createSendSelectionKeyboard(
            List<GreetingDto> userGreetings, List<GreetingDto> publicGreetings
    ) {
        try {
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

            List<GreetingDto> privateGreetings = userGreetings.stream()
                    .filter(greeting -> !greeting.isPublic())
                    .toList();

            if (!privateGreetings.isEmpty()) {
                keyboard.add(List.of(InlineKeyboardButton.builder()
                        .text("🔒 Мои сообщения")
                        .callbackData("section_private")
                        .build()));

                for (GreetingDto greeting : privateGreetings) {
                    List<InlineKeyboardButton> row = new ArrayList<>();
                    row.add(InlineKeyboardButton.builder()
                            .text(truncateText(greeting.text(), 20) + " (ID: " + greeting.id() + ")")
                            .callbackData("send_" + greeting.id())
                            .build());
                    keyboard.add(row);
                }
            }

            if (publicGreetings != null && !publicGreetings.isEmpty()) {
                keyboard.add(List.of(InlineKeyboardButton.builder()
                        .text("🌐 Публичные сообщения")
                        .callbackData("section_public")
                        .build()));

                int count = 0;
                for (GreetingDto greeting : publicGreetings) {
                    if (count >= 10) break;
                    if (greeting.isPublic()) {
                        List<InlineKeyboardButton> row = new ArrayList<>();
                        row.add(InlineKeyboardButton.builder()
                                .text(truncateText(greeting.text(), 20) + " (ID: " + greeting.id() + ")")
                                .callbackData("send_" + greeting.id())
                                .build());
                        keyboard.add(row);
                        count++;
                    }
                }
            }

            if (privateGreetings.isEmpty() && (publicGreetings == null || publicGreetings.isEmpty())) {
                keyboard.add(List.of(InlineKeyboardButton.builder()
                        .text("📭 Нет доступных сообщений")
                        .callbackData("create_greeting")
                        .build()));
            }

            List<InlineKeyboardButton> backRow = new ArrayList<>();
            backRow.add(InlineKeyboardButton.builder()
                    .text("🏠 Главное меню")
                    .callbackData("main_menu")
                    .build());
            keyboard.add(backRow);

            return InlineKeyboardMarkup.builder().keyboard(keyboard).build();

        } catch (Exception e) {
            return createSingleButton("❌ Ошибка загрузки", "main_menu");
        }
    }

    @Override
    public InlineKeyboardMarkup createContactSelectionKeyboard(List<BirthdayDto> birthdays) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (int i = 0; i < birthdays.size(); i += 2) {
            List<InlineKeyboardButton> row = new ArrayList<>();

            BirthdayDto birthday1 = birthdays.get(i);
            row.add(InlineKeyboardButton.builder()
                    .text(birthday1.name() + (birthday1.contact() != null ? " 🐵 : " + birthday1.contact() : ""))
                    .callbackData("send_to_" + birthday1.id())
                    .build());

            if (i + 1 < birthdays.size()) {
                BirthdayDto birthday2 = birthdays.get(i + 1);
                row.add(InlineKeyboardButton.builder()
                        .text(birthday2.name() + (birthday2.contact() != null ? " 🐵 : " + birthday2.contact() : ""))
                        .callbackData("send_to_" + birthday2.id())
                        .build());
            }

            keyboard.add(row);
        }

        List<InlineKeyboardButton> cancelRow = new ArrayList<>();
        cancelRow.add(InlineKeyboardButton.builder()
                .text("❌ Отмена")
                .callbackData("main_menu")
                .build());
        keyboard.add(cancelRow);

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
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

    private String createShareLink(String message) {
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);

        return String.format("https://t.me/share/url?url=%s&text=%s",
                URLEncoder.encode("", StandardCharsets.UTF_8),
                encodedMessage);
    }

    private InlineKeyboardButton createShareButton(String message) {
        return InlineKeyboardButton.builder()
                .text("📤 Отправить в Telegram")
                .url(createShareLink(message))
                .build();
    }

    @Override
    public InlineKeyboardMarkup createShare(String message) {
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        keyboardRows.add(List.of(createShareButton(message)));

        return InlineKeyboardMarkup.builder()
                .keyboard(keyboardRows)
                .build();
    }
}

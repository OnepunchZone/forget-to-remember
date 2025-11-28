package ru.sazon.forget_to_remember.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.sazon.forget_to_remember.model.Greeting;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SendServiceImpl implements SendService{
    private final AbsSender bot;

    @Override
    public void sendGreeting(Greeting greeting, String chatId) {
        if (greeting.getMediaUrl() != null) {

            SendPhoto sendPhoto = SendPhoto.builder()
                    .chatId(chatId)
                    .photo(new InputFile(greeting.getMediaUrl()))
                    .caption(greeting.getText())
                    .replyMarkup(createInlineKeyboard())
                    .build();
            try {
                bot.execute(sendPhoto);
            } catch (Exception e) {
                throw new RuntimeException("Send photo failed: " + e.getMessage());
            }

        } else {

            SendMessage sendMessage = SendMessage.builder()
                    .chatId(chatId)
                    .text(greeting.getText())
                    .replyMarkup(createInlineKeyboard())
                    .build();
            try {
                bot.execute(sendMessage);
            } catch (Exception e) {
                throw new RuntimeException("Send message failed: " + e.getMessage());
            }
        }
    }

    private InlineKeyboardMarkup createInlineKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder().text("Отправить").callbackData("send").build());
        row.add(InlineKeyboardButton.builder().text("Редактировать").callbackData("edit").build());
        rows.add(row);

        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }
}

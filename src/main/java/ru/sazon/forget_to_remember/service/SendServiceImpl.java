package ru.sazon.forget_to_remember.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.sazon.forget_to_remember.bot.BotSender;
import ru.sazon.forget_to_remember.model.Greeting;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SendServiceImpl implements SendService {
    @Override
    public void sendGreeting(Greeting greeting, String chatId, boolean preview, BotSender bot) {
        try {
            /*if (greeting.getMediaUrl() != null && !greeting.getMediaUrl().isEmpty()) {
                SendPhoto sendPhoto = SendPhoto.builder()
                        .chatId(chatId)
                        .photo(new InputFile(greeting.getMediaUrl()))
                        .caption(greeting.getText())
                        .build();
                bot.execute(sendPhoto);
                return;
            } */


            SendMessage sendMessage = SendMessage.builder()
                    .chatId(chatId)
                    .text(greeting.getText())
                    .build();
            bot.execute(sendMessage);


            log.info("Сообщение отправлено на chatId: " + chatId);

        } catch (Exception e) {
            log.error("Сбой при отправке сообщения на chatId: " + chatId);
            throw new RuntimeException("Send failed: " + e.getMessage());
        }
    }

    private InlineKeyboardMarkup createInlineKeyboard(Long greetingId) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder().text("Отправить").callbackData("send_" + greetingId).build());
        row.add(InlineKeyboardButton.builder().text("Редактировать").callbackData("edit_" + greetingId).build());
        rows.add(row);

        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }
}

package ru.sazon.forget_to_remember.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.sazon.forget_to_remember.dto.birthday.BirthdayDto;
import ru.sazon.forget_to_remember.dto.greeting.GreetingCreateDto;
import ru.sazon.forget_to_remember.dto.greeting.GreetingDto;
import ru.sazon.forget_to_remember.dto.greeting.GreetingUpdateDto;
import ru.sazon.forget_to_remember.mapper.GreetingMapper;
import ru.sazon.forget_to_remember.model.EditState;
import ru.sazon.forget_to_remember.model.Greeting;
import ru.sazon.forget_to_remember.model.User;
import ru.sazon.forget_to_remember.service.BirthdayService;
import ru.sazon.forget_to_remember.service.GreetingService;
import ru.sazon.forget_to_remember.service.KeyboardService;
import ru.sazon.forget_to_remember.service.SendService;
import ru.sazon.forget_to_remember.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotCommandHandler {
    private final UserService userService;

    private final GreetingService greetingService;

    private final SendService sendService;

    private final KeyboardService keyboardService;

    private final BirthdayService birthdayService;

    private final GreetingMapper greetingMapper;

    private final Map<Long, String> userStates = new ConcurrentHashMap<>();

    private final Map<Long, EditState> editStates = new ConcurrentHashMap<>();

    private final Map<Long, Long> sendSelections = new ConcurrentHashMap<>();

    private void sendMessage(BotSender botSender, String chatId, String text) {
        botSender.sendMessage(chatId, text);
    }

    private void sendMessageWithKeyboard(
            BotSender botSender, String chatId, String text, InlineKeyboardMarkup keyboard
    ) {
        botSender.sendMessageWithKeyboard(chatId, text, keyboard);
    }

    public void handleTextMessage(Update update, BotSender botSender) {
        String messageText = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        String state = userStates.get(chatId);
        if (state != null) {
            if (state.startsWith("awaiting_greeting_text")) {
                handleCreateGreetingText(botSender, chatId, messageText);
                userStates.remove(chatId);

                return;
            } else if (state.startsWith("awaiting_send_id")) {
                handleSendGreeting(botSender, chatId, messageText, state);
                userStates.remove(chatId);

                return;
            } else if (state.startsWith("awaiting_edit_id")) {
                handleEditGreeting(botSender, chatId, messageText, state);
                userStates.remove(chatId);

                return;
            }
        }

        EditState editState = editStates.get(chatId);
        if (editState != null) {
            handleEditGreetingText(botSender, chatId, messageText, editState);
            editStates.remove(chatId);

            return;
        }

        switch (messageText) {
            case "/start":
                User user = findUserByChatId(chatId);

                if (user != null && user.getContact() != null && !user.getContact().isEmpty()) {
                    String welcomeMessage = """
                            👋Привет! Я бот этого приложения.\s

                            Добро пожаловать в *Forget To Remember*!

                            Я буду напоминать тебе о днях рождениях твоих друзей и близких.
                            Ты можешь создавать шаблоны поздравлений и быстро отправлять их!
                            Можешь выбрать команду:
                            /help - Справка
                            /create - Создать новое поздравление
                            /my - Мои поздравления""";

                    sendMessageWithKeyboard(
                            botSender,
                            chatId.toString(),
                            welcomeMessage,
                            keyboardService.createMainMenuKeyboard()
                    );
                } else {
                    sendMessage(botSender, chatId.toString(),
                            "Привет! Я бот Forget To Remember. Выбери действие:\n" +
                                    "Зарегистрируйся на сайте и укажи chatId для уведомлений.");
                }

                break;
            case "/help":
                sendMessage(botSender, chatId.toString(),
                        """
                                Помощь по боту:

                                /start - Главное меню
                                /help - Справка
                                /create - Создать новое поздравление
                                /my - Мои поздравления

                                Я помогу не забыть о днях рождениях! 🎂""");

                break;
            case "/create":
                handleCreateGreeting(botSender, chatId);

                break;
            case "/my":
                handleMyPrivateGreetings(botSender, chatId, greetingService);

                break;
            default:
                sendMessage(botSender, chatId.toString(), "Не понимаю команду. Используй /start или /help");
        }
    }

    public void handleCallbackQuery(Update update, BotSender botSender) {
        log.info("Callback query received: {}", update.getCallbackQuery().getData());
        String callbackData = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String callbackQueryId = update.getCallbackQuery().getId();
        log.info("=== BotCommandHandler.handleCallbackQuery ===");
        log.info("Chat ID: {}, Callback data: {}", chatId, callbackData);

        if (callbackData.startsWith("send_to_")) {
            handleSendToContact(botSender, chatId, callbackData, update);
            answerCallbackQuery(botSender, callbackQueryId);

            return;
        }

        if (callbackData.startsWith("edit_")) {
            handleEditGreetingSelection(botSender, chatId, callbackData);
            answerCallbackQuery(botSender, callbackQueryId);

            return;
        }

        if (callbackData.startsWith("send_")) {
            handleSendGreetingSelection(botSender, chatId, callbackData);
            answerCallbackQuery(botSender, callbackQueryId);

            return;
        }

        switch (callbackData) {
            case "search_popular_greetings":
                handleSearchPopularGreetings(botSender, chatId, greetingService);

                break;
            case "create_greeting":
                handleCreateGreeting(botSender, chatId);

                break;
            case "my_private_greetings":
                handleMyPrivateGreetings(botSender, chatId, greetingService);

                break;
            case "my_birthdays":
                sendMessage(botSender, chatId.toString(), "📅 Ваши дни рождения... (реализовать логику)");

                break;
            case "help":
                sendMessage(botSender, chatId.toString(), "ℹ️ Помощь... (реализовать логику)");

                break;
            case "main_menu":
                sendMessageWithKeyboard(
                        botSender, chatId.toString(), "🏠 Главное меню", keyboardService.createMainMenuKeyboard()
                );

                break;
            case "delete_my_greeting":
                sendMessage(botSender, chatId.toString(), "🗑️ Выберите для удаления... (реализовать)");

                break;
            case "edit":
                User userForEdit = findUserByChatId(chatId);
                if (userForEdit != null) {
                    Pageable pageable = PageRequest.of(0, 50);
                    List<GreetingDto> userGreetings = greetingService.findByOwner(userForEdit, pageable).getContent();

                    sendMessageWithKeyboard(
                            botSender,
                            chatId.toString(),
                            "✏️ Выберите сообщение для редактирования из списка:",
                            keyboardService.createEditSelectionKeyboard(userGreetings)
                    );
                }

                break;
            case "send":
                User userForSend = findUserByChatId(chatId);
                if (userForSend != null) {
                    Pageable pageable = PageRequest.of(0, 50);
                    List<GreetingDto> userGreetings = greetingService.findByOwner(userForSend, pageable).getContent();
                    List<GreetingDto> publicGreetings = greetingService.findPublic(
                            PageRequest.of(0, 10)
                    ).getContent();
                    sendMessageWithKeyboard(
                            botSender,
                            chatId.toString(),
                            "📤 Выберите сообщение для отправки:",
                            keyboardService.createSendSelectionKeyboard(userGreetings, publicGreetings)
                    );
                }

                break;

            default:
                sendMessage(botSender, chatId.toString(), "❌ Неизвестное действие.");

                break;
        }
    }

    private void handleSearchPopularGreetings(BotSender botSender, Long chatId, GreetingService greetingService) {
        try {
            Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "likesCount"));
            Page<GreetingDto> popularGreetings = greetingService.findPublic(pageable);

            if (popularGreetings.isEmpty()) {
                sendMessage(botSender, chatId.toString(), "Пока нет популярных поздравлений. Будьте первым!");

                return;
            }

            StringBuilder message = new StringBuilder();
            message.append("🏆 *Самые популярные поздравления:*\n\n");

            int counter = 1;
            for (GreetingDto greeting : popularGreetings.getContent()) {
                message.append(counter++).append(". ");
                message.append(truncateText(greeting.text(), 100)).append("\n");
                message.append("   👍 ").append(greeting.likesCount() != 0 ? greeting.likesCount() : 0);
                message.append(" лайков\n\n");
            }

            sendMessageWithKeyboard(
                    botSender,
                    chatId.toString(),
                    message.toString(),
                    keyboardService.createEditSelectionKeyboard(popularGreetings.toList())
            );
        } catch (Exception e) {
            log.error("Error getting popular greetings: {}", e.getMessage(), e);
            sendMessage(botSender, chatId.toString(), "❌ Ошибка при загрузке популярных поздравлений.");
        }
    }

    private void handleCreateGreeting(BotSender botSender, Long chatId) {
        userStates.put(chatId, "awaiting_greeting_text");
        sendMessage(botSender, chatId.toString(),
                """
                        📝 *Создание нового поздравления*

                        Отправьте текст поздравления. Например:
                        "С днем рождения! Желаю счастья и здоровья!"

                        Поздравление будет сохранено как приватное.""");
    }

    private void handleCreateGreetingText(BotSender botSender, Long chatId, String text) {
        try {
            User user = findUserByChatId(chatId);

            if (user == null) {
                sendMessage(botSender, chatId.toString(),
                        "❌ Вы не зарегистрированы в системе.\n" +
                                "Пожалуйста, зарегистрируйтесь на сайте и укажите этот ID в настройках: " + chatId);

                return;
            }

            GreetingCreateDto createDto = new GreetingCreateDto(text, "", false);
            GreetingDto savedGreeting = greetingService.createGreeting(createDto, user);

            sendMessageWithKeyboard(botSender, chatId.toString(),
                    "✅ Поздравление сохранено!\n\n" +
                            "Текст: " + savedGreeting.text() + "\n" +
                            "Статус: Приватное 🔒\n\n" +
                            "Используйте кнопку, чтобы его отправить:",
                    keyboardService.createShare(savedGreeting.text()));

            sendMessageWithKeyboard(botSender, chatId.toString(),
                    "Или перейдите в шаблоны ваших поздравлений:",
                    keyboardService.createSingleButton("💬 Мои поздравления","my_private_greetings")
            );

        } catch (Exception e) {
            log.error("Error creating greeting: {}", e.getMessage(), e);
            sendMessage(botSender, chatId.toString(), "❌ Ошибка при сохранении поздравления: " + e.getMessage());
        }
    }

    private void handleSendGreeting(BotSender botSender, Long chatId, String recipientChatId, String state) {
        Long greetingId = extractIdFromState(state);

        if (greetingId == null) {
            sendMessage(botSender, chatId.toString(), "❌ Ошибка: неверный ID.");

            return;
        }

        try {
            Greeting greeting = greetingMapper.toEntity(greetingService.getById(greetingId));

            sendService.sendGreeting(greeting, recipientChatId, false, botSender);

            sendMessage(
                    botSender, chatId.toString(), "✅ Поздравление отправлено получателю: " + recipientChatId
            );
        } catch (Exception e) {
            log.error("Ошибка при отправке поздравления: {}", e.getMessage(), e);
            sendMessage(botSender, chatId.toString(), "❌ Ошибка при отправке: " + e.getMessage());
        }
    }

    private void handleSendGreetingSelection(BotSender botSender, Long chatId, String callbackData) {
        try {
            Long greetingId = Long.parseLong(callbackData.substring(5));
            log.info("Извлечён greetingId: {}", greetingId);

            User user = findUserByChatId(chatId);
            if (user == null) {
                sendMessage(botSender, chatId.toString(), "❌ Вы не зарегистрированы.");

                return;
            }

            List<BirthdayDto> birthdays = birthdayService.findByUser(user);
            log.info("Найдено дней рождений: {}", birthdays.size());

            if (birthdays.isEmpty()) {
                sendMessage(botSender, chatId.toString(), "❌ У вас нет добавленных дней рождений.");

                return;
            }

            sendSelections.put(chatId, greetingId);
            log.info("Добавлено в sendSelections: chatId={}, greetingId={}", chatId, greetingId);

            sendMessageWithKeyboard(
                    botSender,
                    chatId.toString(),
                    "📋 Выберите контакт для отправки:",
                    keyboardService.createContactSelectionKeyboard(birthdays)
            );

        } catch (Exception e) {
            log.error("Ошибка при выборе сообщения для отправки: {}", e.getMessage(), e);
            sendMessage(botSender, chatId.toString(), "❌ Ошибка при выборе сообщения.");
        }
    }

    private void handleSendToContact(BotSender botSender, Long chatId, String callbackData, Update update) {
        try {
            Long birthdayId = Long.parseLong(callbackData.substring(8));

            User user = findUserByChatId(chatId);
            if (user == null) {
                sendMessage(botSender, chatId.toString(), "❌ Вы не зарегистрированы.");

                return;
            }

            Long greetingId = sendSelections.get(chatId);
            if (greetingId == null) {
                sendMessage(botSender, chatId.toString(), "❌ Сообщение не выбрано.");

                return;
            }

            BirthdayDto birthday = birthdayService.getBirthdayById(birthdayId);
            if (birthday == null) {
                sendMessage(botSender, chatId.toString(), "❌ Контакт не найден.");

                return;
            }

            if (!birthday.userId().equals(user.getId())) {
                sendMessage(botSender, chatId.toString(), "❌ У вас нет доступа к этому контакту.");

                return;
            }

            GreetingDto greetingDto = greetingService.getById(greetingId);
            Greeting greeting = greetingMapper.toEntity(greetingDto);

            String contact = birthday.contact();
            if (contact == null || contact.isEmpty()) {
                sendMessage(botSender, chatId.toString(), "❌ У этого контакта не указан номер для отправки.");

                return;
            }

            sendService.sendGreeting(greeting, contact, false, botSender);

            sendMessage(botSender, chatId.toString(),
                    "✅ Сообщение отправлено контакту: " + birthday.name() + " (" + contact + ")");

            sendSelections.remove(chatId);

        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения: {}", e.getMessage(), e);
            sendMessage(botSender, chatId.toString(), "❌ Ошибка при отправке сообщения: " + e.getMessage());
        }
    }

    private void handleEditGreeting(BotSender botSender, Long chatId, String newText, String state) {
        Long greetingId = extractIdFromState(state);

        if (greetingId == null) {
            sendMessage(botSender, chatId.toString(), "❌ Ошибка: неверный ID.");

            return;
        }

        try {
            GreetingDto currentGreeting = greetingService.getById(greetingId);

            GreetingUpdateDto updateDto = new GreetingUpdateDto(
                    newText,
                    currentGreeting.mediaUrl(),
                    currentGreeting.isPublic()
            );

            GreetingDto updated = greetingService.updateGreeting(greetingId, updateDto);

            sendMessageWithKeyboard(
                    botSender,
                    chatId.toString(),
                    "✅ Поздравление обновлено!\nНовый текст: " + updated.text(),
                    keyboardService.createShare(updated.text())
            );

        } catch (Exception e) {
            log.error("Ошибка при редактировании поздравления: {}", e.getMessage(), e);
            sendMessage(botSender, chatId.toString(), "❌ Ошибка при редактировании: " + e.getMessage());
        }
    }

    private void handleEditGreetingSelection(BotSender botSender, Long chatId, String callbackData) {
        try {
            Long greetingId = Long.parseLong(callbackData.substring(5));

            User user = findUserByChatId(chatId);
            if (user == null) {
                sendMessage(botSender, chatId.toString(), "❌ Вы не зарегистрированы.");

                return;
            }

            GreetingDto greetingDto = greetingService.getById(greetingId);

            if (!greetingDto.ownerId().equals(user.getId()) && !greetingDto.isPublic()) {
                sendMessage(botSender, chatId.toString(), "❌ У вас нет прав для редактирования этого сообщения.");

                return;
            }

            if (greetingDto.isPublic() && !greetingDto.ownerId().equals(user.getId())) {
                GreetingCreateDto copyDto = new GreetingCreateDto(
                        greetingDto.text(),
                        greetingDto.mediaUrl(),
                        false
                );

                GreetingDto copiedGreeting = greetingService.createGreeting(copyDto, user);

                EditState editStateNew = new EditState();
                editStateNew.setGreetingId(copiedGreeting.id());
                editStateNew.setOriginalId(greetingDto.id());
                editStateNew.setPublicCopy(true);
                editStateNew.setOriginalText(copiedGreeting.text());
                editStates.put(chatId, editStateNew);

                sendMessage(botSender, chatId.toString(),
                        "📝 Создана ваша копия публичного сообщения. Введите новый текст для редактирования:");
            } else {
                EditState editStateNew = new EditState();
                editStateNew.setGreetingId(greetingDto.id());
                editStateNew.setOriginalId(greetingDto.id());
                editStateNew.setPublicCopy(false);
                editStateNew.setOriginalText(greetingDto.text());
                editStates.put(chatId, editStateNew);

                sendMessage(botSender, chatId.toString(),
                        "📝 Редактирование вашего сообщения. Введите новый текст:\n" +
                                "(Текущий текст: " + truncateText(greetingDto.text(), 1000) + ")"
                );
            }

        } catch (Exception e) {
            log.error("Ошибка при выборе сообщения для редактирования: {}", e.getMessage(), e);
            sendMessage(botSender, chatId.toString(), "❌ Ошибка при выборе сообщения.");
        }
    }

    private void handleEditGreetingText(BotSender botSender, Long chatId, String newText, EditState editState) {
        try {
            GreetingUpdateDto updateDto = new GreetingUpdateDto(
                    newText,
                    null,
                    false
            );

            GreetingDto updated = greetingService.updateGreeting(editState.getGreetingId(), updateDto);

            sendMessageWithKeyboard(
                    botSender,
                    chatId.toString(),
                    "✅ Сообщение успешно обновлено!\n" +
                            "Новый текст: " + truncateText(updated.text(), 200),
                    keyboardService.createShare(updated.text())
            );

        } catch (Exception e) {
            log.error("Ошибка при редактировании сообщения: {}", e.getMessage(), e);
            sendMessage(botSender, chatId.toString(), "❌ Ошибка при редактировании: " + e.getMessage());
        }
    }

    private void handleMyPrivateGreetings(BotSender botSender, Long chatId, GreetingService greetingService) {
        try {
            User user = findUserByChatId(chatId);

            if (user == null) {
                sendMessage(botSender, chatId.toString(),
                        "❌ Вы не зарегистрированы в системе.\n" +
                                "Пожалуйста, зарегистрируйтесь на сайте и укажите этот ID в настройках: " + chatId);

                return;
            }

            Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));
            List<GreetingDto> myGreetings = greetingService.findByOwner(user, pageable)
                    .getContent()
                    .stream()
                    .filter(greeting -> !greeting.isPublic())
                    .toList();

            if (myGreetings.isEmpty()) {
                sendMessage(botSender, chatId.toString(),
                        "📭 У вас пока нет приватных поздравлений.\n" +
                                "Создайте первое поздравление с помощью кнопки \"📝 Создать новое\"");

                return;
            }

            StringBuilder message = new StringBuilder();
            message.append("🔒 *Ваши приватные поздравления:*\n\n");

            int counter = 1;
            for (GreetingDto greeting : myGreetings) {
                message.append(counter++).append(". ");
                message.append(truncateText(greeting.text(), 150)).append("\n");
                message.append(" | 👍 ").append(greeting.likesCount() != 0 ? greeting.likesCount() : 0);
                message.append("\n\n");
            }

            sendMessage(botSender, chatId.toString(), message.toString());
            sendMessageWithKeyboard(
                    botSender,
                    chatId.toString(),
                    "Выберите сообщения для редактировани: \n",
                    keyboardService.createEditSelectionKeyboard(myGreetings)
            );

        } catch (Exception e) {
            log.error("Error getting private greetings: {}", e.getMessage(), e);
            sendMessage(botSender, chatId.toString(), "❌ Ошибка при загрузке ваших поздравлений.");
        }
    }

    private User findUserByChatId(Long chatId) {
        try {
            return userService.findByContact(String.valueOf(chatId));
        } catch (Exception e) {
            log.warn("User not found for chatId: {}. Error: {}", chatId, e.getMessage());

            return null;
        }
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;

        return text.substring(0, maxLength) + "...";
    }

    private void answerCallbackQuery(BotSender botSender, String callbackQueryId) {
        botSender.answerCallbackQuery(callbackQueryId);
    }

    private Long extractIdFromState(String state) {
        try {
            String[] parts = state.split("_");

            return Long.parseLong(parts[parts.length - 1]);
        } catch (NumberFormatException e) {
            log.error("Ошибка парсинга ID из состояния: {}", state);

            return null;
        }
    }
}

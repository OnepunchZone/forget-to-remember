package ru.sazon.forget_to_remember.bot;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.sazon.forget_to_remember.dto.greeting.GreetingCreateDto;
import ru.sazon.forget_to_remember.dto.greeting.GreetingDto;
import ru.sazon.forget_to_remember.model.User;
import ru.sazon.forget_to_remember.service.GreetingService;
import ru.sazon.forget_to_remember.service.KeyboardService;
import ru.sazon.forget_to_remember.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ForgetToRememberBot extends TelegramLongPollingBot {
    private final String botUsername;

    private final UserService userService;

    private final GreetingService greetingService;

    private final KeyboardService keyboardService;

    private final Map<Long, String> userStates = new ConcurrentHashMap<>();

    @Autowired
    private TelegramBotsApi telegramBotsApi;

    public ForgetToRememberBot(
            @Value("${bot.token}") String botToken,
            @Value("${bot.name}") String botUsername,
            UserService userService,
            GreetingService greetingService,
            KeyboardService keyboardService) {
        super(botToken);
        this.botUsername = botUsername;
        this.userService = userService;
        this.greetingService = greetingService;
        this.keyboardService = keyboardService;
    }

    @PostConstruct
    public void init() {
        try {
            telegramBotsApi.registerBot(this);
            log.info("Bot registered successfully: {}", botUsername);
        } catch (TelegramApiException e) {
            log.error("Failed to register bot: {}", e.getMessage(), e);
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleTextMessage(update);
        }

        if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
        }
    }

    private void handleTextMessage(Update update) {
        String messageText = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        if (userStates.containsKey(chatId) && "awaiting_greeting_text".equals(userStates.get(chatId))) {
            handleCreateGreetingText(chatId, messageText);
            userStates.remove(chatId);

            return;
        }

        switch (messageText) {
            case "/start":
                User user = findUserByChatId(chatId);

                if (user != null && user.getContact() != null && !user.getContact().isEmpty()) {
                    String welcomeMessage = "👋Привет! Я бот этого приложения. \n\n" +
                            "Добро пожаловать в *Forget To Remember*!\n\n" +
                            "Я буду напоминать тебе о днях рождениях твоих друзей и близких. " +
                            "Ты можешь создавать шаблоны поздравлений и быстро отправлять их!";

                    sendMessageWithKeyboard(
                            chatId.toString(),
                            welcomeMessage,
                            keyboardService.createMainMenuKeyboard()
                    );
                } else {
                    sendMessage(chatId.toString(),
                            "Привет! Я бот Forget To Remember. Выбери действие:\n" +
                                    "Зарегистрируйся на сайте и укажи chatId для уведомлений.");
                }

                break;
            case "/help":
                sendMessage(chatId.toString(),
                        "Помощь по боту:\n\n" +
                                "/start - Главное меню\n" +
                                "/help - Справка\n" +
                                "/create - Создать новое поздравление\n" +
                                "/my - Мои поздравления\n\n" +
                                "Я помогу не забыть о днях рождениях! 🎂");

                break;
            case "/create":
                handleCreateGreeting(chatId);

                break;
            case "/my":
                handleMyPrivateGreetings(chatId, greetingService);

                break;
            default:
                sendMessage(chatId.toString(),
                        "Не понимаю команду. Используй /start или /help");
        }
    }


    private void handleCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String messageText;

        try {

            switch (callbackData) {
                case "search_popular_greetings":
                    handleSearchPopularGreetings(chatId, greetingService);

                    break;
                case "create_greeting":
                    handleCreateGreeting(chatId);

                    break;
                case "my_private_greetings":
                    handleMyPrivateGreetings(chatId, greetingService);

                    break;

                case "my_birthdays":
                    sendMessage(chatId.toString(), "📅 Функция 'Мои дни рождения' в разработке.");

                    break;

                case "settings":
                    sendMessage(chatId.toString(), "⚙️ Функция 'Настройки' в разработке.");

                    break;

                case "main_menu":
                    messageText = "🏠 Главное меню:";
                    sendMessageWithKeyboard(
                            chatId.toString(),
                            messageText,
                            keyboardService.createMainMenuKeyboard()
                    );

                    break;

                case "send":
                    sendMessage(chatId.toString(), "Отправка в процессе... (реализовать логику).");

                    break;

                case "edit":
                    sendMessage(chatId.toString(), "Редактирование... (реализовать логику).");

                    break;

                default:
                    messageText = "Команда в разработке: " + callbackData;
                    sendMessage(chatId.toString(), messageText);
            }

            answerCallbackQuery(update.getCallbackQuery().getId());

        } catch (Exception e) {
            log.error("Error handling callback: {}", e.getMessage(), e);
            sendMessage(chatId.toString(), "❌ Произошла ошибка: " + e.getMessage());
            answerCallbackQuery(update.getCallbackQuery().getId());
        }
    }

    private void handleSearchPopularGreetings(Long chatId, GreetingService greetingService) {
        try {
            Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "likesCount"));
            Page<GreetingDto> popularGreetings = greetingService.findPublic(pageable);

            if (popularGreetings.isEmpty()) {
                sendMessage(chatId.toString(), "Пока нет популярных поздравлений. Будьте первым!");

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

            sendMessage(chatId.toString(), message.toString());
        } catch (Exception e) {
            log.error("Error getting popular greetings: {}", e.getMessage(), e);
            sendMessage(chatId.toString(), "❌ Ошибка при загрузке популярных поздравлений.");
        }
    }

    private void handleCreateGreeting(Long chatId) {
        userStates.put(chatId, "awaiting_greeting_text");
        sendMessage(chatId.toString(),
                "📝 *Создание нового поздравления*\n\n" +
                        "Отправьте текст поздравления. Например:\n" +
                        "\"С днем рождения! Желаю счастья и здоровья!\"\n\n" +
                        "Поздравление будет сохранено как приватное.");
    }

    private void handleCreateGreetingText(Long chatId, String text) {
        try {
            User user = findUserByChatId(chatId);

            if (user == null) {
                sendMessage(chatId.toString(),
                        "❌ Вы не зарегистрированы в системе.\n" +
                                "Пожалуйста, зарегистрируйтесь на сайте и укажите этот ID в настройках: " + chatId);

                return;
            }

            GreetingCreateDto createDto = new GreetingCreateDto(text, "", false);
            GreetingDto savedGreeting = greetingService.createGreeting(createDto, user);

            sendMessageWithKeyboard(chatId.toString(),
                    "✅ Поздравление сохранено!\n\n" +
                            "Текст: " + savedGreeting.text() + "\n" +
                            "Статус: Приватное 🔒\n\n" +
                            "Используйте кнопку, чтобы просмотреть его",
                    keyboardService.createSingleButton("💬 Мои поздравления", "my_private_greetings"));

        } catch (Exception e) {
            log.error("Error creating greeting: {}", e.getMessage(), e);
            sendMessage(chatId.toString(), "❌ Ошибка при сохранении поздравления: " + e.getMessage());
        }
    }

    private void handleMyPrivateGreetings(Long chatId, GreetingService greetingService) {
        try {
            User user = findUserByChatId(chatId);

            if (user == null) {
                sendMessage(chatId.toString(),
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
                sendMessage(chatId.toString(),
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

            sendMessage(chatId.toString(), message.toString());
        } catch (Exception e) {
            log.error("Error getting private greetings: {}", e.getMessage(), e);
            sendMessage(chatId.toString(), "❌ Ошибка при загрузке ваших поздравлений.");
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

    private void answerCallbackQuery(String callbackQueryId) {
        try {
            AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQueryId)
                    .build();
            execute(answer);
        } catch (TelegramApiException e) {
            log.error("Error answering callback query: {}", e.getMessage());
        }
    }

    private void sendMessage(String chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        executeMessage(message);
    }

    private void sendMessageWithKeyboard(String chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard)
                .build();
        executeMessage(message);
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
            log.debug("Message sent successfully to: {}", message.getChatId());
        } catch (TelegramApiException e) {
            log.error("Failed to send message to {}: {}", message.getChatId(), e.getMessage());
        }
    }
}

package ru.sazon.forget_to_remember.service;

import ru.sazon.forget_to_remember.model.Birthday;
import ru.sazon.forget_to_remember.model.Greeting;
import ru.sazon.forget_to_remember.model.User;

import java.util.List;
import java.util.Map;

public interface NotificationService {
    void sendBirthdayNotification(User user, Map<Birthday, Greeting> birthdaysWithGreetings);

    void sendWelcomeNotification(User user);
}

package ru.sazon.forget_to_remember.service;

import ru.sazon.forget_to_remember.model.Birthday;
import ru.sazon.forget_to_remember.model.User;

import java.util.List;

public interface NotificationService {
    void sendBirthdayNotification(User user, List<Birthday> birthdays);
}

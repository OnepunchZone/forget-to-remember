package ru.sazon.forget_to_remember.service;

import ru.sazon.forget_to_remember.model.Greeting;

public interface SendService {
    void sendGreeting(Greeting greeting, String chatId);
}

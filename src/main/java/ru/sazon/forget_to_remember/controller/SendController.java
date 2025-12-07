package ru.sazon.forget_to_remember.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.sazon.forget_to_remember.config.MyUserDetails;
import ru.sazon.forget_to_remember.mapper.GreetingMapper;
import ru.sazon.forget_to_remember.model.Greeting;
import ru.sazon.forget_to_remember.model.Role;
import ru.sazon.forget_to_remember.model.User;
import ru.sazon.forget_to_remember.service.GreetingService;
import ru.sazon.forget_to_remember.service.SendService;

@RestController
@RequestMapping("/api/v1/send")
@RequiredArgsConstructor
public class SendController {
    private final SendService sendService;

    private final GreetingService greetingService;

    private final GreetingMapper greetingMapper;

    @PostMapping("/{greetingId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> send(
            @PathVariable Long greetingId,
            @RequestParam("chatId") String chatId,
            Authentication auth
    ) {
        User currentUser = ((MyUserDetails) auth.getPrincipal()).getUser();
        Greeting greeting = greetingMapper.toEntity(greetingService.getById(greetingId));

        if (greeting.getOwner().getId() != currentUser.getId() && !currentUser.getRoles().contains(Role.ADMIN)) {
            return ResponseEntity.badRequest().body("Not authorized");
        }

        sendService.sendGreeting(greeting, chatId);

        return ResponseEntity.ok("Sent to " + chatId);
    }
}

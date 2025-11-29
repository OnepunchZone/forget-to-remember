package ru.sazon.forget_to_remember.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import ru.sazon.forget_to_remember.config.MyUserDetails;
import ru.sazon.forget_to_remember.dto.BirthdayDto;
import ru.sazon.forget_to_remember.dto.GreetingDto;
import ru.sazon.forget_to_remember.model.User;
import ru.sazon.forget_to_remember.service.BirthdayService;
import ru.sazon.forget_to_remember.service.GreetingService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebController {
    private final BirthdayService birthdayService;

    private final GreetingService greetingService;

    @GetMapping("/")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String loginSuccess(Authentication auth, Model model) {
        User currentUser = ((MyUserDetails) auth.getPrincipal()).getUser();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("birthdays", birthdayService.findByUser(currentUser));
        model.addAttribute("greetings", greetingService.findByOwner(currentUser));

        return "dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        User currentUser = ((MyUserDetails) auth.getPrincipal()).getUser();
        model.addAttribute("currentUser", currentUser);

        List<BirthdayDto> birthdays = birthdayService.findByUser(currentUser);
        model.addAttribute("birthdays", birthdays);
        System.out.println("Debug: Birthdays size for user " + currentUser.getUsername() + ": " + birthdays.size());  // Лог

        List<GreetingDto> greetings = greetingService.findByOwner(currentUser);
        model.addAttribute("greetings", greetings);
        System.out.println("Debug: Greetings size for user " + currentUser.getUsername() + ": " + greetings.size());  // Лог

        return "dashboard";
    }
}

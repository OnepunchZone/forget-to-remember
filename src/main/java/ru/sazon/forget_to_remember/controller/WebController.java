package ru.sazon.forget_to_remember.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.sazon.forget_to_remember.config.MyUserDetails;
import ru.sazon.forget_to_remember.dto.birthday.BirthdayCreateDto;
import ru.sazon.forget_to_remember.dto.birthday.BirthdayDto;
import ru.sazon.forget_to_remember.dto.greeting.GreetingCreateDto;
import ru.sazon.forget_to_remember.dto.greeting.GreetingDto;
import ru.sazon.forget_to_remember.dto.user.UserDto;
import ru.sazon.forget_to_remember.dto.user.UserRegistrationDto;
import ru.sazon.forget_to_remember.exeption.BusinessLogicException;
import ru.sazon.forget_to_remember.exeption.EntityNotFoundException;
import ru.sazon.forget_to_remember.model.User;
import ru.sazon.forget_to_remember.service.BirthdayService;
import ru.sazon.forget_to_remember.service.GreetingService;
import ru.sazon.forget_to_remember.service.UserService;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebController {
    private final BirthdayService birthdayService;

    private final GreetingService greetingService;

    private final UserService userService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("showHeader", true);
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm() {
        return "user/register";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth,
                            Model model,
                            @PageableDefault(
                                    size = 5,
                                    sort = "id",
                                    direction = Sort.Direction.DESC) Pageable greetingPageable
    ) {
        User currentUser = ((MyUserDetails) auth.getPrincipal()).getUser();
        model.addAttribute("currentUser", currentUser);

        List<BirthdayDto> birthdays = birthdayService.findByUser(currentUser);
        model.addAttribute("birthdays", birthdays);
        System.out.println("Debug: Birthdays size for user " + currentUser.getUsername() + ": " + birthdays.size());

        Page<GreetingDto> greetingPage = greetingService.findByOwner(currentUser, greetingPageable);
        model.addAttribute("greetingPage", greetingPage);
        model.addAttribute("greetingPageNumber", greetingPage.getNumber());
        model.addAttribute("greetingTotalPages", greetingPage.getTotalPages());
        model.addAttribute("greetingHasNext", greetingPage.hasNext());
        model.addAttribute("greetingHasPrevious", greetingPage.hasPrevious());

        return "dashboard";
    }

    @GetMapping("/birthdays")
    @PreAuthorize("isAuthenticated()")
    public String birthdays(Authentication auth, Model model) {
        User currentUser = ((MyUserDetails) auth.getPrincipal()).getUser();
        List<BirthdayDto> birthdays = birthdayService.findByUser(currentUser);
        model.addAttribute("birthdays", birthdays);
        model.addAttribute("newBirthday", new BirthdayCreateDto("", LocalDate.now(), ""));

        return "birthday/list";
    }

    @GetMapping("/greetings")
    @PreAuthorize("isAuthenticated()")
    public String publicGreetings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<GreetingDto> greetingsPage = greetingService.findPublic(pageable);

        model.addAttribute("greetings", greetingsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", greetingsPage.getTotalPages());
        model.addAttribute("totalItems", greetingsPage.getTotalElements());
        model.addAttribute("newGreeting", new GreetingCreateDto("", "", true));

        return "greeting/public-list";
    }

    @GetMapping("/greetings-my")
    @PreAuthorize("isAuthenticated()")
    public String myGreetings(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        User currentUser = ((MyUserDetails) auth.getPrincipal()).getUser();
        Pageable pageable = PageRequest.of(page, size);

        Page<GreetingDto> greetingsPage = greetingService.findByOwner(currentUser, pageable);

        model.addAttribute("greetings", greetingsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", greetingsPage.getTotalPages());
        model.addAttribute("totalItems", greetingsPage.getTotalElements());
        model.addAttribute("newGreeting", new GreetingCreateDto("", "", false));

        return "greeting/user-list";
    }

    // для ADMIN
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String showAdminUsers(Authentication auth, Model model) {
        User currentUser = ((MyUserDetails) auth.getPrincipal()).getUser();
        List<UserDto> users = userService.findAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("updateDto", new UserRegistrationDto("", "", "", "", ""));

        return "user/admin-users";
    }

    @GetMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public UserDto getUser(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PostMapping("/admin/users/{id}/update")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute UserRegistrationDto updateDto,
                             Authentication auth,
                             Model model) {
        try {
            UserDto userDto = userService.updateUser(id, updateDto);

            return "redirect:/admin/users";
        } catch (BusinessLogicException | EntityNotFoundException e) {
            model.addAttribute("error", e.getMessage());

            return showAdminUsers(auth, model);
        }
    }

    @PostMapping("/admin/users/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(@PathVariable Long id, Authentication auth, Model model) {
        try {
            User currentUser = ((MyUserDetails) auth.getPrincipal()).getUser();

            if (currentUser.getId().equals(id)) {
                throw new BusinessLogicException("Нельзя удалить самого себя");
            }

            userService.deleteUser(id);

            return "redirect:/admin/users";
        } catch (BusinessLogicException e) {
            model.addAttribute("error", e.getMessage());

            return showAdminUsers(auth, model);
        }
    }
}

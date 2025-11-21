package ru.sazon.forget_to_remember.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sazon.forget_to_remember.config.MyUserDetails;
import ru.sazon.forget_to_remember.dto.BirthdayDto;
import ru.sazon.forget_to_remember.model.User;
import ru.sazon.forget_to_remember.service.BirthdayService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/birthday")
@RequiredArgsConstructor
public class BirthdayController {
    private final BirthdayService birthdayService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BirthdayDto> create(@RequestBody BirthdayDto dto, Authentication auth) {
        User currentUser = ((MyUserDetails) auth.getPrincipal()).getUser();

        return ResponseEntity.ok(birthdayService.createBirthday(dto, currentUser));
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<BirthdayDto>> getMyBirthdays(Authentication auth) {
        User currentUser = ((MyUserDetails) auth.getPrincipal()).getUser();

        return ResponseEntity.ok(birthdayService.findByUser(currentUser));
    }

    @GetMapping("/today")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<BirthdayDto>> getTodayBirthdays() {
        return ResponseEntity.ok(birthdayService.findByDate(LocalDate.now()));
    }
}

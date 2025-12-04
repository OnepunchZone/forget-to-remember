package ru.sazon.forget_to_remember.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sazon.forget_to_remember.config.MyUserDetails;
import ru.sazon.forget_to_remember.dto.birthday.BirthdayCreateDto;
import ru.sazon.forget_to_remember.dto.birthday.BirthdayDto;
import ru.sazon.forget_to_remember.dto.birthday.BirthdayUpdateDto;
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
    public ResponseEntity<BirthdayDto> create(@Valid @RequestBody BirthdayCreateDto dto, Authentication auth) {
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

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BirthdayDto> update(@PathVariable Long id, @Valid @RequestBody BirthdayUpdateDto dto, Authentication auth) {
        User currentUser = ((MyUserDetails) auth.getPrincipal()).getUser();

        BirthdayDto updated = birthdayService.updateBirthday(id, dto);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> delete(@PathVariable Long id, Authentication auth) {
        birthdayService.deleteBirthday(id);

        return ResponseEntity.ok("Birthday deleted");
    }
}

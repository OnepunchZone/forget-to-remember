package ru.sazon.forget_to_remember.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.sazon.forget_to_remember.config.MyUserDetails;
import ru.sazon.forget_to_remember.dto.GreetingDto;
import ru.sazon.forget_to_remember.exeption.FileTooLargeException;
import ru.sazon.forget_to_remember.model.User;
import ru.sazon.forget_to_remember.service.GreetingService;

import java.util.List;


@RestController
@RequestMapping("/api/v1/greeting")
@RequiredArgsConstructor
public class GreetingController {
    private final GreetingService greetingService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<GreetingDto> create(@RequestBody GreetingDto dto, Authentication auth) {
        User currentUser = ((MyUserDetails) auth.getPrincipal()).getUser();

        return ResponseEntity.ok(greetingService.createGreeting(dto, currentUser));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<GreetingDto>> getMyGreetings(Authentication auth) {
        User currentUser = ((MyUserDetails) auth.getPrincipal()).getUser();

        return ResponseEntity.ok(greetingService.findByOwner(currentUser));
    }

    @GetMapping("/public")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<GreetingDto>> getPublic(Pageable pageable) {

        return ResponseEntity.ok(greetingService.findPublic(pageable));
    }

    @PostMapping("/media")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<GreetingDto> createWithMedia(
            @RequestPart("text") String text,
            @RequestPart(value = "media", required = false) MultipartFile mediaFile,
            @RequestPart("isPublic") String isPublicStr,
            Authentication auth) {
        User currentUser = ((MyUserDetails) auth.getPrincipal()).getUser();

        boolean isPublic = Boolean.parseBoolean(isPublicStr);

        if (mediaFile != null && mediaFile.getSize() > 10 * 1024 * 1024) {
            throw new FileTooLargeException("Слишком большой файл: максимальный размер 10Мб");
        }

        GreetingDto dto = greetingService.createGreetingWithMedia(mediaFile, text, isPublic, currentUser);

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/like")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<GreetingDto> like(@PathVariable Long id) {
        GreetingDto liked = greetingService.addLike(id);

        return ResponseEntity.ok(liked);
    }

    @DeleteMapping("/{id}/like")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<GreetingDto> unlike(@PathVariable Long id) {
        GreetingDto unliked = greetingService.removeLike(id);

        return ResponseEntity.ok(unliked);
    }
}

package ru.sazon.forget_to_remember.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.sazon.forget_to_remember.config.MyUserDetails;
import ru.sazon.forget_to_remember.dto.greeting.GreetingCreateDto;
import ru.sazon.forget_to_remember.dto.greeting.GreetingDto;
import ru.sazon.forget_to_remember.dto.greeting.GreetingUpdateDto;
import ru.sazon.forget_to_remember.exeption.FileTooLargeException;
import ru.sazon.forget_to_remember.model.Role;
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
    public ResponseEntity<GreetingDto> create(@Valid @RequestBody GreetingCreateDto dto, Authentication auth) {
        User currentUser = ((MyUserDetails) auth.getPrincipal()).getUser();

        return ResponseEntity.ok(greetingService.createGreeting(dto, currentUser));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<GreetingDto> update(@PathVariable Long id, @Valid @RequestBody GreetingUpdateDto dto, Authentication auth) {
        User currentUser = ((MyUserDetails) auth.getPrincipal()).getUser();
        GreetingDto greetingDto = greetingService.getById(id);

        if (!currentUser.getId().equals(greetingDto.ownerId()) && !currentUser.getRoles().contains(Role.ADMIN)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Access denied: You can only edit your own greetings"
            );
        }

        GreetingDto updated = greetingService.updateGreeting(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> delete(@PathVariable Long id, Authentication auth) {
        User currentUser = ((MyUserDetails) auth.getPrincipal()).getUser();
        GreetingDto greetingDto = greetingService.getById(id);

        if (!currentUser.getId().equals(greetingDto.ownerId()) && !currentUser.getRoles().contains(Role.ADMIN)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Access denied: You can only delete your own greetings"
            );
        }

        greetingService.deleteGreeting(id);
        return ResponseEntity.ok("Greeting deleted");
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<GreetingDto>> getMyGreetings(
            Authentication auth,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        User currentUser = ((MyUserDetails) auth.getPrincipal()).getUser();

        return ResponseEntity.ok(greetingService.findByOwner(currentUser, pageable));
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

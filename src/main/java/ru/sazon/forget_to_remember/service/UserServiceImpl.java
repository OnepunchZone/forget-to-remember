package ru.sazon.forget_to_remember.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sazon.forget_to_remember.dto.user.UserDto;
import ru.sazon.forget_to_remember.dto.user.UserRegistrationDto;
import ru.sazon.forget_to_remember.exeption.BusinessLogicException;
import ru.sazon.forget_to_remember.mapper.UserMapper;
import ru.sazon.forget_to_remember.model.Role;
import ru.sazon.forget_to_remember.model.User;
import ru.sazon.forget_to_remember.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    @Override
    public UserDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id %d не найден".formatted(id)));

        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> findAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream().map(userMapper::toDto).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public UserDto createUser(UserRegistrationDto registrationDto) {
        if (userRepository.findByUsername(registrationDto.username()).isPresent()) {
            throw new BusinessLogicException("Пользователь с таким именем уже существует");
        }

        User user = new User();
        user.setUsername(registrationDto.username());
        user.setPassword(passwordEncoder.encode(registrationDto.password()));
        user.setEmail(registrationDto.email());
        user.setContact(registrationDto.contact());

        user.getRoles().add(Role.USER);
        if ("ADMIN".equals(registrationDto.role())) {
            user.getRoles().add(Role.ADMIN);
        }

        User savedUser = userRepository.save(user);

        return userMapper.toDto(savedUser);
    }

    @Transactional
    @Override
    public UserDto updateUser(Long id, UserRegistrationDto updateDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        if (updateDto.username() != null
                && !updateDto.username().isEmpty()
                && !updateDto.username().equals(user.getUsername())) {

            if (userRepository.findByUsername(updateDto.username()).isPresent()) {
                throw new BusinessLogicException("Username already exists");
            }
            user.setUsername(updateDto.username());
        }

        if (updateDto.email() != null) user.setEmail(updateDto.email());

        if (updateDto.contact() != null) user.setContact(updateDto.contact());

        if (updateDto.role() != null && !updateDto.role().isEmpty()) {
            user.getRoles().clear();
            user.getRoles().add(Role.valueOf(updateDto.role()));
        }

        User saved = userRepository.save(user);

        return userMapper.toDto(saved);
    }

    @Override
    public User findByContact(String contact) {
        return userRepository.findByContact(contact)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с contact %s не найден".formatted(contact)));
    }

    @Transactional
    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        userRepository.delete(user);
    }
}

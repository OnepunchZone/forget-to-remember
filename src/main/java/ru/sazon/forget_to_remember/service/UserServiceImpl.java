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
}

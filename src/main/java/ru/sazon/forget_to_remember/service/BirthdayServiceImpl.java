package ru.sazon.forget_to_remember.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sazon.forget_to_remember.dto.birthday.BirthdayCreateDto;
import ru.sazon.forget_to_remember.dto.birthday.BirthdayDto;
import ru.sazon.forget_to_remember.dto.birthday.BirthdayUpdateDto;
import ru.sazon.forget_to_remember.exeption.EntityNotFoundException;
import ru.sazon.forget_to_remember.mapper.BirthdayMapper;
import ru.sazon.forget_to_remember.model.Birthday;
import ru.sazon.forget_to_remember.model.Greeting;
import ru.sazon.forget_to_remember.model.User;
import ru.sazon.forget_to_remember.repository.BirthdayRepository;
import ru.sazon.forget_to_remember.repository.GreetingRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BirthdayServiceImpl implements BirthdayService {
    private final BirthdayRepository birthdayRepository;

    private final BirthdayMapper birthdayMapper;

    private final GreetingRepository greetingRepository;

    @Transactional(readOnly = true)
    @Override
    public List<BirthdayDto> findByUser(User user) {
        List<Birthday> birthdays = birthdayRepository.findByUser(user);

        return birthdays.stream().map(birthdayMapper::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<BirthdayDto> findByDate(LocalDate date) {
        List<Birthday> birthdays = birthdayRepository.findByDate(date);

        return birthdays.stream().map(birthdayMapper::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public BirthdayDto getBirthdayById(Long id) {
        Birthday birthday = birthdayRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Birthday not found: " + id));
        return birthdayMapper.toDto(birthday);
    }

    @Transactional
    @Override
    public BirthdayDto createBirthday(BirthdayCreateDto dto, User currentUser) {
        Birthday birthday = new Birthday();

        birthday.setName(dto.name());
        birthday.setDate(dto.date());
        birthday.setContact(dto.contact());
        birthday.setUser(currentUser);

        if (dto.greetingId() != null) {
            Greeting greeting = greetingRepository.findById(dto.greetingId())
                    .orElseThrow(() -> new EntityNotFoundException("Greeting not found: " + dto.greetingId()));
            birthday.setGreeting(greeting);
        } else {
            birthday.setGreeting(null);
        }

        Birthday saved = birthdayRepository.save(birthday);

        return birthdayMapper.toDto(saved);
    }

    @Transactional
    @Override
    public BirthdayDto updateBirthday(Long id, BirthdayUpdateDto dto) {
        Birthday birthday = birthdayRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Birthday not found: " + id));

        birthday.setName(dto.name());
        birthday.setDate(dto.date());
        birthday.setContact(dto.contact());

        if (dto.greetingId() != null) {
            Greeting greeting = greetingRepository.findById(dto.greetingId())
                    .orElseThrow(() -> new EntityNotFoundException("Greeting not found: " + dto.greetingId()));

            birthday.setGreeting(greeting);

        } else if (dto.greetingId() == null && dto.name() == null && dto.date() == null && dto.contact() == null) {
            birthday.setGreeting(null);
        }

        Birthday saved = birthdayRepository.save(birthday);

        return birthdayMapper.toDto(saved);
    }

    @Transactional
    @Override
    public void deleteBirthday(Long id) {
        if (!birthdayRepository.existsById(id)) {
            throw new EntityNotFoundException("Birthday not found with id: " + id);
        }
        birthdayRepository.deleteById(id);
    }
}

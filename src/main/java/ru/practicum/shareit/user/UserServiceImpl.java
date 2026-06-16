package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.user.model.User;
import java.util.*;

@Service
@Slf4j
@Qualifier("UserServiceImpl")
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDto create(UserDtoNew newUserRequestDto) {
        log.info("UserServiceImpl->create start");
        validateEmail(newUserRequestDto.getEmail());
        User userToSave = UserMapper.mapToUser(newUserRequestDto);
        User savedUser = userRepository.save(userToSave);
        log.info("UserServiceImpl->create end");
        return UserMapper.mapToUserDto(savedUser);
    }

    @Override
    @Transactional
    public void delete(Integer userId) {
        log.info("UserServiceImpl->delete start");
        userRepository.deleteById(userId);
        log.info("UserServiceImpl->delete end");
    }

    @Override
    public List<UserDto> getAll() {
        log.info("UserServiceImpl->getAll start");
        List<UserDto> users = userRepository.findAll().stream()
                .filter(Objects::nonNull)
                .map(UserMapper::mapToUserDto)
                .toList();
        log.info("UserServiceImpl->getAll end");
        return users;
    }

    @Override
    public UserDto getById(Integer userId) {
        log.info("UserServiceImpl->getById start");
        User user = userRepository.findById(userId).orElseThrow(() -> {
            String error = String.format("User with ID: %d not found", userId);
            log.warn(error);
            return new NotFoundException(error);
        });
        log.info("UserServiceImpl->getById end");
        return UserMapper.mapToUserDto(user);
    }

    @Override
    @Transactional
    public UserDto update(UserDtoUpdate updateUserRequestDto, Integer userId) {
        log.info("UserServiceImpl->update start");
        validateEmail(updateUserRequestDto.getEmail());

        User existingUser = userRepository.findById(userId).orElseThrow(() ->
                new ValidationException("User with ID: " + userId + " not found")
        );

        User updatedUser = UserMapper.updateUserField(existingUser, updateUserRequestDto);
        User savedUser = userRepository.save(updatedUser);

        log.info("UserServiceImpl->update end");
        return UserMapper.mapToUserDto(savedUser);
    }

    private void validateEmail(String email) {
        Optional<User> existingUser = userRepository.findByEmailIgnoreCase(email);
        if (existingUser.isPresent()) {
            String error = String.format("Email: %s is already taken by another user", email);
            log.warn(error);
            throw new ConflictException(error);
        }
    }
}

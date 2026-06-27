package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.user.UserDto;
import ru.practicum.common.user.UserDtoNew;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.validation.ValidationService;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Qualifier("UserServiceImpl")
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ValidationService validationService;

    @Override
    @Transactional
    public UserDto create(UserDtoNew newUserRequestDto) {
        log.info("UserServiceImpl->create start, newUserRequestDto={}", newUserRequestDto);
        validationService.validateEmailNotTaken(newUserRequestDto.getEmail());

        User userToSave = UserMapper.mapToUser(newUserRequestDto);
        User savedUser = userRepository.save(userToSave);
        UserDto result = UserMapper.mapToUserDto(savedUser);
        log.info("UserServiceImpl->create end, result={}", result);
        return result;
    }

    @Override
    @Transactional
    public void delete(Integer userId) {
        log.info("UserServiceImpl->delete start, userId={}", userId);
        validationService.validateUserExists(userId);
        userRepository.deleteById(userId);
        log.info("UserServiceImpl->delete end");
    }

    @Override
    public List<UserDto> getAll() {
        log.info("UserServiceImpl->getAll start");
        List<UserDto> result = userRepository.findAll().stream()
                .filter(Objects::nonNull)
                .map(UserMapper::mapToUserDto)
                .toList();
        log.info("UserServiceImpl->getAll end, result size={}", result.size());
        return result;
    }

    @Override
    public UserDto getById(Integer userId) {
        log.info("UserServiceImpl->getById start, userId={}", userId);
        User user = validationService.validateUserExists(userId);
        UserDto result = UserMapper.mapToUserDto(user);
        log.info("UserServiceImpl->getById end, result={}", result);
        return result;
    }

    @Override
    @Transactional
    public UserDto update(UserDtoNew updateUserRequestDto, Integer userId) {
        log.info("UserServiceImpl->update start, updateUserRequestDto={}, userId={}", updateUserRequestDto, userId);
        validationService.validateEmailNotTaken(updateUserRequestDto.getEmail());

        User existingUser = validationService.validateUserExists(userId);
        User updatedUser = UserMapper.updateUserField(existingUser, updateUserRequestDto);
        User savedUser = userRepository.save(updatedUser);
        UserDto result = UserMapper.mapToUserDto(savedUser);

        log.info("UserServiceImpl->update end, result={}", result);
        return result;
    }
}

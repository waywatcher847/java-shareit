package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.user.UserDto;
import ru.practicum.shareit.exception.InternalServerException;
import ru.practicum.shareit.exception.NotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserMapper userMapper;
    private static final String THIS_CLASS = "UserService";

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        log.info("UserServiceImpl->create start");
        log.info("userDto={}", userDto);

        log.info("Creating user with email: {}", userDto.getEmail());

        if (repository.findByEmail(userDto.getEmail()).isPresent()) {
            log.warn("User with email {} already exists", userDto.getEmail());
            throw new InternalServerException("User with email " + userDto.getEmail() + " already exists");
        }

        User user = userMapper.toEntity(userDto);

        try {
            User savedUser = repository.save(user);
            log.info("User created with id: {}", savedUser.getId());
            UserDto result = userMapper.toDto(savedUser);

            log.info("UserServiceImpl->create end");
            return result;
        } catch (DataIntegrityViolationException e) {
            log.error("Error saving user: {}", e.getMessage());
            throw new InternalServerException("User with this email already exists");
        }
    }

    @Override
    @Transactional
    public UserDto update(Integer id, UserDto userDto) {
        log.info("UserServiceImpl->update start");
        log.info("id={}, userDto={}", id, userDto);

        log.info("Updating user with id: {}", id);

        if (userDto.getId() != null && !userDto.getId().equals(id)) {
            log.warn("ID in request body {} =/= ID in path {}", userDto.getId(), id);
            throw new InternalServerException("ID in request =/= ID in URL");
        }

        User existingUser = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with ID " + id + " not found"));

        log.debug("Existing user: {}", existingUser);

        if (userDto.getEmail() != null && !userDto.getEmail().equals(existingUser.getEmail())) {
            log.debug("Checking email uniqueness: {}", userDto.getEmail());
            repository.findByEmail(userDto.getEmail()).ifPresent(user -> {
                if (!user.getId().equals(id)) {
                    log.warn("Email {} is already used by user with id {}", userDto.getEmail(), user.getId());
                    throw new InternalServerException("Email " + userDto.getEmail() + " is already used");
                }
            });
        }
        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            existingUser.setEmail(userDto.getEmail());
        }

        log.debug("Updated user before saving: {}", existingUser);

        try {
            User updatedUser = repository.save(existingUser);
            log.info("User with id {} successfully updated", id);
            UserDto result = userMapper.toDto(updatedUser);

            log.info("UserServiceImpl->update end");
            return result;
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity error while updating user: {}", e.getMessage());
            throw new InternalServerException("Email already exists");
        } catch (Exception e) {
            log.error("Unexpected error while updating user: {}", e.getMessage(), e);
            throw new InternalServerException("Error updating user: " + e.getMessage());
        }
    }

    @Override
    public UserDto getUserById(Integer id) {
        log.info("UserServiceImpl->getUserById start");
        log.info("id={}", id);

        log.info("Fetching user with id: {}", id);
        User user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with ID " + id + " not found"));

        UserDto result = userMapper.toDto(user);

        log.info("UserServiceImpl->getUserById end");
        return result;
    }

    @Override
    @Transactional
    public void deleteUserById(Integer id) {
        log.info("UserServiceImpl->deleteUserById start");
        log.info("id={}", id);

        log.info("Deleting user with id: {}", id);
        if (!repository.existsById(id)) {
            throw new NotFoundException("User with ID " + id + " not found");
        }
        repository.deleteById(id);
        log.info("User with id {} deleted", id);

        log.info("UserServiceImpl->deleteUserById end");
    }
}
package ru.practicum.shareit.validation;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.User;

@Slf4j
public class ValidationTool {

    private ValidationTool() {
        throw new UnsupportedOperationException();
    }

    public static void checkId(Integer id, String level, String description) {
        if (id == null || id < 1) {
            throw new ValidationException(level + ": " + description);
        }
    }

    public static void userCheck(User user, String level) {
        log.info("userCheck start");
        if (user.getName() == null || user.getName().isBlank()) {
            log.warn("userCheck name bad");
            throw new ValidationException(level + ": user name empty or null");
        }

        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("userCheck email bad");
            throw new ValidationException(level + ": email without @ or empty");
        }
        log.info("userCheck good");
    }
}

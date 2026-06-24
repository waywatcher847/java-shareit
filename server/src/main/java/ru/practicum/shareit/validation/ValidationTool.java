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

}

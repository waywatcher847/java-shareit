package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.validation.ValidationTool;

import static org.junit.jupiter.api.Assertions.*;

class ValidationToolTests {

    @Test
    void checkId_WhenIdIsNull_ShouldThrowValidationException() {
        assertThrows(ValidationException.class, () ->
                ValidationTool.checkId(null, "TestClass", "ID cannot be null"));
    }

    @Test
    void checkId_WhenIdIsZero_ShouldThrowValidationException() {
        assertThrows(ValidationException.class, () ->
                ValidationTool.checkId(0, "TestClass", "ID must be positive"));
    }

    @Test
    void checkId_WhenIdIsNegative_ShouldThrowValidationException() {
        assertThrows(ValidationException.class, () ->
                ValidationTool.checkId(-1, "TestClass", "ID must be positive"));
    }

    @Test
    void checkId_WhenIdIsValid_ShouldNotThrowException() {
        assertDoesNotThrow(() ->
                ValidationTool.checkId(1, "TestClass", "ID is valid"));
    }
}
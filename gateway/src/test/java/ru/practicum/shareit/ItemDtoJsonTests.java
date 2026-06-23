package ru.practicum.shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.common.item.ItemDto;

import java.io.IOException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemDtoJsonTests {
    @Autowired
    private JacksonTester<ItemDto> json;

    private final Validator validator;

    public ItemDtoJsonTests() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    void ItemDto_WhenNameIsInvalid_ReturnsValidationErrors() throws IOException {
        String invalidJson1 = """
                {
                                        "description": "description",
                                        "available": true,
                                        "requestId": 111
                                    }""";

        ItemDto itemDto1 = json.parseObject(invalidJson1);

        Set<ConstraintViolation<ItemDto>> violations1 =
                validator.validate(itemDto1);

        assertThat(violations1).hasSize(1);
        assertThat(violations1)
                .extracting(ConstraintViolation::getMessage)
                .contains("Name is mandatory");

        String invalidJson2 = """
                {
                                        "name": "",
                                        "description": "description",
                                        "available": true,
                                        "requestId": 111
                                    }""";

        ItemDto itemDto2 = json.parseObject(invalidJson2);

        Set<ConstraintViolation<ItemDto>> violations2 =
                validator.validate(itemDto2);

        assertThat(violations2).hasSize(1);
        assertThat(violations2)
                .extracting(ConstraintViolation::getMessage)
                .contains("Name is mandatory");

        String invalidJson3 = """
                {
                                        "name": "     ",
                                        "description": "description",
                                        "available": true,
                                        "requestId": 111
                                    }""";

        ItemDto itemDto3 = json.parseObject(invalidJson3);

        Set<ConstraintViolation<ItemDto>> violations3 =
                validator.validate(itemDto3);

        assertThat(violations3).hasSize(1);
        assertThat(violations3)
                .extracting(ConstraintViolation::getMessage)
                .contains("Name is mandatory");
    }

    @Test
    void ItemDto_WhenValidJson_DeserializesSuccessfully() throws IOException {
        String validJson = """
                {
                                        "name": "Item",
                                        "description": "description",
                                        "available": true,
                                        "requestId": 111
                                    }""";

        ItemDto itemDto = json.parseObject(validJson);

        assertThat(itemDto.getName()).isEqualTo("Item");
        assertThat(itemDto.getDescription()).isEqualTo("description");
        assertThat(itemDto.getAvailable()).isEqualTo(true);
        assertThat(itemDto.getRequestId()).isEqualTo(111);
    }

    @Test
    void ItemDto_WhenAvailableIsInvalid_ReturnsValidationErrors() throws IOException {
        String invalidJson1 = """
                {
                                        "name": "Item",
                                        "description": "description",
                                        "requestId": 111
                                    }""";

        ItemDto itemDto1 = json.parseObject(invalidJson1);

        Set<ConstraintViolation<ItemDto>> violations1 =
                validator.validate(itemDto1);

        assertThat(violations1).hasSize(1);
        assertThat(violations1)
                .extracting(ConstraintViolation::getMessage)
                .contains("available is mandatory");
    }

    @Test
    void ItemDto_WhendescriptionIsInvalid_ReturnsValidationErrors() throws IOException {
        String invalidJson1 = """
                {
                                        "name": "Item",
                                        "available": true,
                                        "requestId": 111
                                    }""";

        ItemDto itemDto1 = json.parseObject(invalidJson1);

        Set<ConstraintViolation<ItemDto>> violations1 =
                validator.validate(itemDto1);

        assertThat(violations1).hasSize(1);
        assertThat(violations1)
                .extracting(ConstraintViolation::getMessage)
                .contains("description is mandatory");

        String invalidJson2 = """
                {
                                        "name": "Item",
                                        "description": "",
                                        "available": true,
                                        "requestId": 111
                                    }""";

        ItemDto itemDto2 = json.parseObject(invalidJson2);

        Set<ConstraintViolation<ItemDto>> violations2 =
                validator.validate(itemDto2);

        assertThat(violations2).hasSize(1);
        assertThat(violations2)
                .extracting(ConstraintViolation::getMessage)
                .contains("description is mandatory");

        String invalidJson3 = """
                {
                                        "name": "Item",
                                        "description": "     ",
                                        "available": true,
                                        "requestId": 111
                                    }""";

        ItemDto itemDto3 = json.parseObject(invalidJson3);

        Set<ConstraintViolation<ItemDto>> violations3 =
                validator.validate(itemDto3);

        assertThat(violations3).hasSize(1);
        assertThat(violations3)
                .extracting(ConstraintViolation::getMessage)
                .contains("description is mandatory");
    }
}
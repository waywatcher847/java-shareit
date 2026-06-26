package ru.practicum.shareit.json;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.common.item.ItemDtoRequest;

import java.io.IOException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemDtoJsonTests {
    @Autowired
    private JacksonTester<ItemDtoRequest> json;

    private final Validator validator;

    public ItemDtoJsonTests() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    void itemDto_WhenNameIsInvalid_ReturnsValidationErrors() throws IOException {
        String invalidJson1 = "{\n" +
                "                        \"description\": \"description\",\n" +
                "                        \"available\": true,\n" +
                "                        \"requestId\": 111\n" +
                "                    }";

        ItemDtoRequest itemDto1 = json.parseObject(invalidJson1);

        Set<ConstraintViolation<ItemDtoRequest>> violations1 =
                validator.validate(itemDto1, ItemDtoRequest.Create.class);

        assertThat(violations1).hasSize(1);
        assertThat(violations1)
                .extracting(ConstraintViolation::getMessage)
                .contains("Name is mandatory");

        String invalidJson2 = "{\n" +
                "                        \"name\": \"     \",\n" +
                "                        \"description\": \"description\",\n" +
                "                        \"available\": true,\n" +
                "                        \"requestId\": 111\n" +
                "                    }";

        ItemDtoRequest itemDto2 = json.parseObject(invalidJson2);

        Set<ConstraintViolation<ItemDtoRequest>> violations2 =
                validator.validate(itemDto2, ItemDtoRequest.Create.class);

        assertThat(violations2).hasSize(1);
        assertThat(violations2)
                .extracting(ConstraintViolation::getMessage)
                .contains("Name is mandatory");

        String invalidJson3 = "{\n" +
                "                        \"name\": \"     \",\n" +
                "                        \"description\": \"description\",\n" +
                "                        \"available\": true,\n" +
                "                        \"requestId\": 111\n" +
                "                    }";

        ItemDtoRequest itemDto3 = json.parseObject(invalidJson3);

        Set<ConstraintViolation<ItemDtoRequest>> violations3 =
                validator.validate(itemDto3, ItemDtoRequest.Create.class);

        assertThat(violations3).hasSize(1);
        assertThat(violations3)
                .extracting(ConstraintViolation::getMessage)
                .contains("Name is mandatory");
    }

    @Test
    void itemDto_WhenValidJson_DeserializesSuccessfully() throws IOException {
        String validJson = "{\n" +
                "                        \"name\": \"Item\",\n" +
                "                        \"description\": \"description\",\n" +
                "                        \"available\": true,\n" +
                "                        \"requestId\": 111\n" +
                "                    }";

        ItemDtoRequest itemDtoRequest = json.parseObject(validJson);

        assertThat(itemDtoRequest.getName()).isEqualTo("Item");
        assertThat(itemDtoRequest.getDescription()).isEqualTo("description");
        assertThat(itemDtoRequest.getAvailable()).isEqualTo(true);
        assertThat(itemDtoRequest.getRequestId()).isEqualTo(111);
    }

    @Test
    void itemDto_WhenAvailableIsInvalid_ReturnsValidationErrors() throws IOException {
        String invalidJson1 = "{\n" +
                "                        \"name\": \"Item\",\n" +
                "                        \"description\": \"Description\",\n" +
                "                        \"requestId\": 111\n" +
                "                    }";

        ItemDtoRequest itemDto1 = json.parseObject(invalidJson1);

        Set<ConstraintViolation<ItemDtoRequest>> violations1 =
                validator.validate(itemDto1, ItemDtoRequest.Create.class);

        assertThat(violations1).hasSize(1);
        assertThat(violations1)
                .extracting(ConstraintViolation::getMessage)
                .contains("available is mandatory");
    }

    @Test
    void itemDto_WhendescriptionIsInvalid_ReturnsValidationErrors() throws IOException {
        String invalidJson1 = "{\n" +
                "                        \"name\": \"Item\",\n" +
                "                        \"available\": true,\n" +
                "                        \"requestId\": 111\n" +
                "                    }";

        ItemDtoRequest itemDto1 = json.parseObject(invalidJson1);

        Set<ConstraintViolation<ItemDtoRequest>> violations1 =
                validator.validate(itemDto1, ItemDtoRequest.Create.class);

        assertThat(violations1).hasSize(1);
        assertThat(violations1)
                .extracting(ConstraintViolation::getMessage)
                .contains("description is mandatory");

        String invalidJson2 = "{\n" +
                "                        \"name\": \"Item\",\n" +
                "                        \"description\": \"     \",\n" +
                "                        \"available\": true,\n" +
                "                        \"requestId\": 111\n" +
                "                    }";


        ItemDtoRequest itemDto2 = json.parseObject(invalidJson2);

        Set<ConstraintViolation<ItemDtoRequest>> violations2 =
                validator.validate(itemDto2, ItemDtoRequest.Create.class);

        assertThat(violations2).hasSize(1);
        assertThat(violations2)
                .extracting(ConstraintViolation::getMessage)
                .contains("description is mandatory");

        String invalidJson3 = "{\n" +
                "                        \"name\": \"Item\",\n" +
                "                        \"description\": \"\",\n" +
                "                        \"available\": true,\n" +
                "                        \"requestId\": 111\n" +
                "                    }";


        ItemDtoRequest itemDto3 = json.parseObject(invalidJson3);

        Set<ConstraintViolation<ItemDtoRequest>> violations3 =
                validator.validate(itemDto3, ItemDtoRequest.Create.class);

        assertThat(violations3).hasSize(1);
        assertThat(violations3)
                .extracting(ConstraintViolation::getMessage)
                .contains("description is mandatory");
    }
}
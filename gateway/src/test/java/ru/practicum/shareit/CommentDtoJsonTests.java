package ru.practicum.shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.common.comment.CommentRequestDto;

import java.io.IOException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class CommentDtoJsonTests {
    @Autowired
    private JacksonTester<CommentRequestDto> json;

    private final Validator validator;

    public CommentDtoJsonTests() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    void commentRequestDto_WhenTextIsInvalid_ReturnsValidationErrors() throws IOException {

        String invalidJson1 = "{\n" +
                "                        \"text\": \"\"\n" +
                "                    }";

        CommentRequestDto commentDto1 = json.parseObject(invalidJson1);

        Set<ConstraintViolation<CommentRequestDto>> violations1 = validator.validate(commentDto1);

        assertThat(violations1).hasSize(1);
        assertThat(violations1).extracting(ConstraintViolation::getMessage).contains("text is mandatory");

        String invalidJson2 = "{\n" +
                "                        \"text\": \"     \"\n" +
                "                    }";

        CommentRequestDto commentDto2 = json.parseObject(invalidJson2);

        Set<ConstraintViolation<CommentRequestDto>> violations2 = validator.validate(commentDto2);

        assertThat(violations2).hasSize(1);
        assertThat(violations2).extracting(ConstraintViolation::getMessage).contains("text is mandatory");

        String invalidJson3 = "{\n" +
                "                    }";

        CommentRequestDto commentDto3 = json.parseObject(invalidJson3);

        Set<ConstraintViolation<CommentRequestDto>> violations3 = validator.validate(commentDto3);

        assertThat(violations3).hasSize(1);
        assertThat(violations3).extracting(ConstraintViolation::getMessage).contains("text is mandatory");

    }

    @Test
    void commentRequestDto_WhenValidJson_DeserializesSuccessfully() throws IOException {
        String validJson = "{\n" +
                "                        \"text\": \"Text\"\n" +
                "                    }";

        CommentRequestDto commentDto = json.parseObject(validJson);

        assertThat(commentDto.getText()).isEqualTo("Text");
    }
}
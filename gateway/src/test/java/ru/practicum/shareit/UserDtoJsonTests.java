package ru.practicum.shareit;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.validation.annotation.Validated;
import ru.practicum.common.user.UserDto;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@Validated
public class UserDtoJsonTests {
    @Autowired
    private JacksonTester<UserDto> json;

    public UserDtoJsonTests() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
    }

    @Test
    void userDto_WhenValidJson_DeserializesSuccessfully() throws IOException {
        String validJson = """
                {
                
                                        "name": "Name",
                                        "email": "email@email.ru"
                                    }""";

        UserDto userDto = json.parseObject(validJson);

        assertThat(userDto.getName()).isEqualTo("Name");
        assertThat(userDto.getEmail()).isEqualTo("email@email.ru");
    }

}
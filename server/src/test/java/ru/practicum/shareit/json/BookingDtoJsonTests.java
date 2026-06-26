package ru.practicum.shareit.json;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.common.booking.BookingDtoRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingDtoJsonTests {
    @Autowired
    private JacksonTester<BookingDtoRequest> json;

    private final Validator validator;

    public BookingDtoJsonTests() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    void bookingRequestDto_WhenValidJson_DeserializesSuccessfully() throws IOException {
        String validJson = "{\n" +
                "                    \"itemId\": 111,\n" +
                "                    \"start\": \"2222-12-25T10:00:00\",\n" +
                "                    \"end\": \"2222-12-26T18:00:00\"\n" +
                "                }";

        BookingDtoRequest bookingDto = json.parseObject(validJson);

        assertThat(bookingDto.getItemId()).isEqualTo(111);
        assertThat(bookingDto.getStart()).isEqualTo(LocalDateTime.of(2222, 12, 25, 10, 0, 0));
        assertThat(bookingDto.getEnd()).isEqualTo(LocalDateTime.of(2222, 12, 26, 18, 0, 0));
    }

    @Test
    void bookingRequestDto_WhenItemIdIsInvalid_ReturnsValidationErrors() throws IOException {
        String invalidJson1 = "{\n" +
                "                        \"start\": \"2222-12-25T10:00:00\",\n" +
                "                        \"end\": \"2222-12-26T18:00:00\"\n" +
                "                    }";

        BookingDtoRequest bookingDto1 = json.parseObject(invalidJson1);

        Set<ConstraintViolation<BookingDtoRequest>> violations1 =
                validator.validate(bookingDto1);

        assertThat(violations1).hasSize(1);
        assertThat(violations1)
                .extracting(ConstraintViolation::getMessage)
                .contains("ID is mandatory");

        String invalidJson2 = "{\n" +
                "                        \"itemId\": 0,\n" +
                "                        \"start\": \"2222-12-25T10:00:00\",\n" +
                "                        \"end\": \"2222-12-26T18:00:00\"\n" +
                "                    }";

        BookingDtoRequest bookingDto2 = json.parseObject(invalidJson2);

        Set<ConstraintViolation<BookingDtoRequest>> violations2 =
                validator.validate(bookingDto2);

        assertThat(violations2).hasSize(1);
        assertThat(violations2)
                .extracting(ConstraintViolation::getMessage)
                .contains("ID can't be negative");

        String invalidJson3 = "{\n" +
                "                        \"itemId\": -1,\n" +
                "                        \"start\": \"2222-12-25T10:00:00\",\n" +
                "                        \"end\": \"2222-12-26T18:00:00\"\n" +
                "                    }";

        BookingDtoRequest bookingDto3 = json.parseObject(invalidJson3);

        Set<ConstraintViolation<BookingDtoRequest>> violations3 =
                validator.validate(bookingDto3);

        assertThat(violations3).hasSize(1);
        assertThat(violations3)
                .extracting(ConstraintViolation::getMessage)
                .contains("ID can't be negative");
    }

    @Test
    void bookingRequestDto_WhenEndIsInvalid_ReturnsValidationErrors() throws IOException {
        String invalidJson1 = "{\n" +
                "                        \"itemId\": 111,\n" +
                "                        \"start\": \"2222-12-26T18:00:00\"\n" +
                "                    }";


        BookingDtoRequest bookingDto1 = json.parseObject(invalidJson1);

        Set<ConstraintViolation<BookingDtoRequest>> violations1 =
                validator.validate(bookingDto1);

        assertThat(violations1).hasSize(1);
        assertThat(violations1)
                .extracting(ConstraintViolation::getMessage)
                .contains("endDate is mandatory");
    }

    @Test
    void bookingRequestDto_WhenStartIsAfterOrEqualToEnd_ReturnsValidationErrors() throws IOException {
        String invalidJson1 = "{\n" +
                "                        \"itemId\": 111,\n" +
                "                        \"start\": \"2222-12-25T10:00:00\",\n" +
                "                        \"end\": \"2222-12-25T10:00:00\"\n" +
                "                    }";

        BookingDtoRequest bookingDto1 = json.parseObject(invalidJson1);

        Set<ConstraintViolation<BookingDtoRequest>> violations1 =
                validator.validate(bookingDto1);

        assertThat(violations1).hasSize(1);
        assertThat(violations1)
                .extracting(ConstraintViolation::getMessage)
                .contains("Dates should be chronological");

        String invalidJson2 = "{\n" +
                "                        \"itemId\": 111,\n" +
                "                        \"start\": \"2222-12-25T19:00:00\",\n" +
                "                        \"end\": \"2222-12-25T10:00:00\"\n" +
                "                    }";

        BookingDtoRequest bookingDto2 = json.parseObject(invalidJson2);

        Set<ConstraintViolation<BookingDtoRequest>> violations2 =
                validator.validate(bookingDto2);

        assertThat(violations2).hasSize(1);
        assertThat(violations2)
                .extracting(ConstraintViolation::getMessage)
                .contains("Dates should be chronological");
    }

    @Test
    void bookingRequestDto_WhenStartIsInvalid_ReturnsValidationErrors() throws IOException {
        String invalidJson1 = "{\n" +
                "                        \"itemId\": 111,\n" +
                "                        \"end\": \"2222-12-25T10:00:00\"\n" +
                "                    }";

        BookingDtoRequest bookingDto1 = json.parseObject(invalidJson1);

        Set<ConstraintViolation<BookingDtoRequest>> violations1 =
                validator.validate(bookingDto1);

        assertThat(violations1).hasSize(1);
        assertThat(violations1)
                .extracting(ConstraintViolation::getMessage)
                .contains("startDate is mandatory");

        String invalidJson2 = "{\n" +
                "                        \"itemId\": 111,\n" +
                "                        \"start\": \"2022-12-25T10:00:00\",\n" +
                "                        \"end\": \"2222-12-25T09:59:59\"\n" +
                "                    }";

        BookingDtoRequest bookingDto2 = json.parseObject(invalidJson2);

        Set<ConstraintViolation<BookingDtoRequest>> violations2 =
                validator.validate(bookingDto2);

        assertThat(violations2).hasSize(1);
        assertThat(violations2)
                .extracting(ConstraintViolation::getMessage)
                .contains("startDate can't be in the past");
    }

}
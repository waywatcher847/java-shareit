package ru.practicum.shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.common.booking.BookingRequestDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingDtoJsonTests {
    @Autowired
    private JacksonTester<BookingRequestDto> json;

    private final Validator validator;

    public BookingDtoJsonTests() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    void BookingRequestDto_WhenValidJson_DeserializesSuccessfully() throws IOException {
        String validJson = """
                {
                                    "itemId": 111,
                                    "start": "2222-12-25T10:00:00",
                                    "end": "2222-12-26T18:00:00"
                                }""";

        BookingRequestDto bookingDto = json.parseObject(validJson);

        assertThat(bookingDto.getItemId()).isEqualTo(111);
        assertThat(bookingDto.getStart()).isEqualTo(LocalDateTime.of(2222, 12, 25, 10, 0, 0));
        assertThat(bookingDto.getEnd()).isEqualTo(LocalDateTime.of(2222, 12, 26, 18, 0, 0));
    }

    @Test
    void BookingRequestDto_WhenItemIdIsInvalid_ReturnsValidationErrors() throws IOException {
        String invalidJson1 = """
                {
                                        "start": "2222-12-25T10:00:00",
                                        "end": "2222-12-26T18:00:00"
                                    }""";

        BookingRequestDto bookingDto1 = json.parseObject(invalidJson1);

        Set<ConstraintViolation<BookingRequestDto>> violations1 =
                validator.validate(bookingDto1);

        assertThat(violations1).hasSize(1);
        assertThat(violations1)
                .extracting(ConstraintViolation::getMessage)
                .contains("ID is mandatory");

        String invalidJson2 = """
                {
                                        "itemId": 0,
                                        "start": "2222-12-25T10:00:00",
                                        "end": "2222-12-26T18:00:00"
                                    }""";

        BookingRequestDto bookingDto2 = json.parseObject(invalidJson2);

        Set<ConstraintViolation<BookingRequestDto>> violations2 =
                validator.validate(bookingDto2);

        assertThat(violations2).hasSize(1);
        assertThat(violations2)
                .extracting(ConstraintViolation::getMessage)
                .contains("ID can't be negative");

        String invalidJson3 = """
                {
                                        "itemId": -1,
                                        "start": "2222-12-25T10:00:00",
                                        "end": "2222-12-26T18:00:00"
                                    }""";

        BookingRequestDto bookingDto3 = json.parseObject(invalidJson3);

        Set<ConstraintViolation<BookingRequestDto>> violations3 =
                validator.validate(bookingDto3);

        assertThat(violations3).hasSize(1);
        assertThat(violations3)
                .extracting(ConstraintViolation::getMessage)
                .contains("ID can't be negative");
    }

    @Test
    void BookingRequestDto_WhenEndIsInvalid_ReturnsValidationErrors() throws IOException {
        String invalidJson1 = """
                {
                                        "itemId": 111,
                                        "start": "2222-12-25T10:00:00"
                                    }""";

        BookingRequestDto bookingDto1 = json.parseObject(invalidJson1);

        Set<ConstraintViolation<BookingRequestDto>> violations1 =
                validator.validate(bookingDto1);

        assertThat(violations1).hasSize(1);
        assertThat(violations1)
                .extracting(ConstraintViolation::getMessage)
                .contains("endDate is mandatory");
    }

    @Test
    void BookingRequestDto_WhenStartIsAfterOrEqualToEnd_ReturnsValidationErrors() throws IOException {
        String invalidJson1 = """
                {
                                        "itemId": 111,
                                        "start": "2222-12-25T10:00:00",
                                        "end": "2222-12-25T10:00:00"
                                    }""";

        BookingRequestDto bookingDto1 = json.parseObject(invalidJson1);

        Set<ConstraintViolation<BookingRequestDto>> violations1 =
                validator.validate(bookingDto1);

        assertThat(violations1).hasSize(1);
        assertThat(violations1)
                .extracting(ConstraintViolation::getMessage)
                .contains("Dates should be chronological");

        String invalidJson2 = """
                {
                                        "itemId": 111,
                                        "start": "2222-12-25T10:00:00",
                                        "end": "2222-12-25T09:59:59"
                                    }""";

        BookingRequestDto bookingDto2 = json.parseObject(invalidJson2);

        Set<ConstraintViolation<BookingRequestDto>> violations2 =
                validator.validate(bookingDto2);

        assertThat(violations2).hasSize(1);
        assertThat(violations2)
                .extracting(ConstraintViolation::getMessage)
                .contains("Dates should be chronological");
    }

    @Test
    void BookingRequestDto_WhenStartIsInvalid_ReturnsValidationErrors() throws IOException {
        String invalidJson1 = """
                {
                                        "itemId": 111,
                                        "end": "2222-12-26T18:00:00"
                                    }""";

        BookingRequestDto bookingDto1 = json.parseObject(invalidJson1);

        Set<ConstraintViolation<BookingRequestDto>> violations1 =
                validator.validate(bookingDto1);

        assertThat(violations1).hasSize(1);
        assertThat(violations1)
                .extracting(ConstraintViolation::getMessage)
                .contains("startDate is mandatory");

        String invalidJson2 = """
                {
                                        "itemId": 111,
                                        "start": "2022-12-25T10:00:00",
                                        "end": "2222-12-26T18:00:00"
                                    }""";

        BookingRequestDto bookingDto2 = json.parseObject(invalidJson2);

        Set<ConstraintViolation<BookingRequestDto>> violations2 =
                validator.validate(bookingDto2);

        assertThat(violations2).hasSize(1);
        assertThat(violations2)
                .extracting(ConstraintViolation::getMessage)
                .contains("startDate can't be in the past");
    }

}
package ru.practicum.shareit.booking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.common.booking.BookingStatus;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "bookings")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Integer id;

    @FutureOrPresent(message = "Дата начала должна быть в настоящем или будущем")
    @Column(name = "start_date")
    LocalDateTime startDate;

    @Future(message = "Дата окончания должна быть в будущем")
    @Column(name = "end_date")
    LocalDateTime endDate;

    @Column(name = "user_id")
    Integer userId;

    @Column(name = "item_id")
    Integer itemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    BookingStatus status;
}
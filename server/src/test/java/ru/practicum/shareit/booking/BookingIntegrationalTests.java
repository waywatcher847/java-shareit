package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.booking.BookingDto;
import ru.practicum.common.booking.BookingDtoRequest;
import ru.practicum.common.booking.BookingState;
import ru.practicum.common.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class BookingIntegrationalTests {
    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;


    @Test
    void create_ValidData_ReturnsSavedBooking() {
        User booker = createUser("Booker", "booker@test.com");
        User owner = createUser("Owner", "owner@test.com");
        Item item = createItem("Item", "Desc", true, owner);
        BookingDtoRequest request = createBookingRequest(item.getId(),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        BookingDto result = bookingService.create(request, booker.getId());

        assertThat(result.getId()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(result.getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    void create_UnavailableItem_ThrowsIllegalArgumentException() {
        User booker = createUser("Booker", "booker@test.com");
        User owner = createUser("Owner", "owner@test.com");
        Item item = createItem("Item", "Desc", false, owner);
        BookingDtoRequest request = createBookingRequest(item.getId(),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        assertThrows(IllegalArgumentException.class, () -> bookingService.create(request, booker.getId()));
    }

    @Test
    void create_OverlappingDates_ThrowsConflictException() {
        User booker1 = createUser("Booker1", "b1@test.com");
        User booker2 = createUser("Booker2", "b2@test.com");
        User owner = createUser("Owner", "owner@test.com");
        Item item = createItem("Item", "Desc", true, owner);

        createBooking(item, booker1, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(5), BookingStatus.APPROVED);

        BookingDtoRequest request = createBookingRequest(item.getId(),
                LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3));

        assertThrows(ConflictException.class, () -> bookingService.create(request, booker2.getId()));
    }

    @Test
    void create_OwnerBooksOwnItem_ThrowsConflictException() {
        User owner = createUser("Owner", "owner@test.com");
        Item item = createItem("Item", "Desc", true, owner);
        BookingDtoRequest request = createBookingRequest(item.getId(),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        assertThrows(ConflictException.class, () -> bookingService.create(request, owner.getId()));
    }

    @Test
    void approve_ByOwner_ApprovesBooking() {
        User booker = createUser("Booker", "booker@test.com");
        User owner = createUser("Owner", "owner@test.com");
        Item item = createItem("Item", "Desc", true, owner);
        Booking booking = createBooking(item, booker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), BookingStatus.WAITING);

        BookingDto result = bookingService.approve(booking.getId(), owner.getId(), true);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void approve_ByNonOwner_ThrowsConflictException() {
        User booker = createUser("Booker", "booker@test.com");
        User owner = createUser("Owner", "owner@test.com");
        User stranger = createUser("Stranger", "stranger@test.com");
        Item item = createItem("Item", "Desc", true, owner);
        Booking booking = createBooking(item, booker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), BookingStatus.WAITING);

        assertThrows(ConflictException.class, () -> bookingService.approve(booking.getId(), stranger.getId(), true));
    }

    @Test
    void approve_AlreadyProcessed_ThrowsConflictException() {
        User booker = createUser("Booker", "booker@test.com");
        User owner = createUser("Owner", "owner@test.com");
        Item item = createItem("Item", "Desc", true, owner);
        Booking booking = createBooking(item, booker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), BookingStatus.APPROVED);

        assertThrows(ConflictException.class, () -> bookingService.approve(booking.getId(), owner.getId(), true));
    }

    @Test
    void approve_WithOverlap_ThrowsConflictException() {
        User booker1 = createUser("Booker1", "b1@test.com");
        User booker2 = createUser("Booker2", "b2@test.com");
        User owner = createUser("Owner", "owner@test.com");
        Item item = createItem("Item", "Desc", true, owner);

        createBooking(item, booker1, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(5), BookingStatus.APPROVED);
        Booking waiting = createBooking(item, booker2, LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3), BookingStatus.WAITING);

        assertThrows(ConflictException.class, () -> bookingService.approve(waiting.getId(), owner.getId(), true));
    }

    @Test
    void approve_NullApproved_ThrowsNullPointerException() {
        User booker = createUser("Booker", "booker@test.com");
        User owner = createUser("Owner", "owner@test.com");
        Item item = createItem("Item", "Desc", true, owner);
        Booking booking = createBooking(item, booker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), BookingStatus.WAITING);

        assertThrows(NullPointerException.class, () -> bookingService.approve(booking.getId(), owner.getId(), null));
    }


    @Test
    void getById_ByBooker_ReturnsBooking() {
        User booker = createUser("Booker", "booker@test.com");
        User owner = createUser("Owner", "owner@test.com");
        Item item = createItem("Item", "Desc", true, owner);
        Booking booking = createBooking(item, booker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), BookingStatus.WAITING);

        BookingDto result = bookingService.getById(booking.getId(), booker.getId());
        assertThat(result.getId()).isEqualTo(booking.getId());
    }

    @Test
    void getById_ByOwner_ReturnsBooking() {
        User booker = createUser("Booker", "booker@test.com");
        User owner = createUser("Owner", "owner@test.com");
        Item item = createItem("Item", "Desc", true, owner);
        Booking booking = createBooking(item, booker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), BookingStatus.WAITING);

        BookingDto result = bookingService.getById(booking.getId(), owner.getId());
        assertThat(result.getId()).isEqualTo(booking.getId());
    }

    @Test
    void getById_ByThirdParty_ThrowsConflictException() {
        User booker = createUser("Booker", "booker@test.com");
        User owner = createUser("Owner", "owner@test.com");
        User stranger = createUser("Stranger", "stranger@test.com");
        Item item = createItem("Item", "Desc", true, owner);
        Booking booking = createBooking(item, booker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), BookingStatus.WAITING);

        assertThrows(ConflictException.class, () -> bookingService.getById(booking.getId(), stranger.getId()));
    }


    @Test
    void getUserBookings_FilterByState_ReturnsCorrectSubset() {
        User booker = createUser("Booker", "booker@test.com");
        User owner = createUser("Owner", "owner@test.com");
        Item item = createItem("Item", "Desc", true, owner);

        LocalDateTime now = LocalDateTime.now();
        createBooking(item, booker, now.minusDays(5), now.minusDays(4), BookingStatus.APPROVED);
        createBooking(item, booker, now.minusDays(1), now.plusDays(1), BookingStatus.APPROVED);
        createBooking(item, booker, now.plusDays(2), now.plusDays(3), BookingStatus.APPROVED);
        createBooking(item, booker, now.plusDays(4), now.plusDays(5), BookingStatus.WAITING);
        createBooking(item, booker, now.plusDays(6), now.plusDays(7), BookingStatus.REJECTED);

        assertThat(bookingService.getUserBookings(booker.getId(), BookingState.ALL)).hasSize(5);
        assertThat(bookingService.getUserBookings(booker.getId(), BookingState.PAST)).hasSize(1);
        assertThat(bookingService.getUserBookings(booker.getId(), BookingState.CURRENT)).hasSize(1);
        assertThat(bookingService.getUserBookings(booker.getId(), BookingState.FUTURE)).hasSize(1);
        assertThat(bookingService.getUserBookings(booker.getId(), BookingState.WAITING)).hasSize(1);
        assertThat(bookingService.getUserBookings(booker.getId(), BookingState.REJECTED)).hasSize(1);
    }

    @Test
    void getOwnerBookings_FilterByState_ReturnsCorrectSubset() {
        User booker = createUser("Booker", "booker@test.com");
        User owner = createUser("Owner", "owner@test.com");
        Item item = createItem("Item", "Desc", true, owner);

        LocalDateTime now = LocalDateTime.now();
        createBooking(item, booker, now.minusDays(2), now.minusDays(1), BookingStatus.APPROVED);
        createBooking(item, booker, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING);

        assertThat(bookingService.getOwnerBookings(owner.getId(), BookingState.ALL)).hasSize(2);
        assertThat(bookingService.getOwnerBookings(owner.getId(), BookingState.PAST)).hasSize(1);
        assertThat(bookingService.getOwnerBookings(owner.getId(), BookingState.WAITING)).hasSize(1);
        assertThat(bookingService.getOwnerBookings(owner.getId(), BookingState.FUTURE)).isEmpty();
    }

    private User createUser(String name, String email) {
        return userRepository.save(User.builder().name(name).email(email).build());
    }

    private Item createItem(String name, String description, Boolean available, User owner) {
        return itemRepository.save(Item.builder()
                .name(name).description(description).available(available).owner(owner).build());
    }

    private Booking createBooking(Item item, User booker, LocalDateTime start, LocalDateTime end, BookingStatus status) {
        return bookingRepository.save(Booking.builder()
                .item(item).booker(booker).start(start).end(end).status(status).build());
    }

    private BookingDtoRequest createBookingRequest(Integer itemId, LocalDateTime start, LocalDateTime end) {
        BookingDtoRequest request = new BookingDtoRequest();
        request.setItemId(itemId);
        request.setStart(start);
        request.setEnd(end);
        return request;
    }
}
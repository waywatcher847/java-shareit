package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.booking.BookingDto;
import ru.practicum.common.booking.BookingRequestDto;
import ru.practicum.common.comment.CommentDto;
import ru.practicum.common.comment.CommentRequestDto;
import ru.practicum.common.item.ItemDto;
import ru.practicum.common.user.UserDto;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemIntegrationalTests {

    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;

    @Test
    void updateItem_WhenCalledByOwner_ShouldUpdateItem() {
        UserDto user = new UserDto();
        user.setName("Name2");
        user.setEmail("name2@mail.ru");
        UserDto createdUser = userService.create(user);
        Integer createdUserId = createdUser.getId();

        ItemDto item = new ItemDto();
        item.setName("Item2");
        item.setDescription("Description2");
        item.setAvailable(true);
        ItemDto createdItem = itemService.create(item, createdUserId);
        Integer createdItemId = createdItem.getId();

        ItemDto updateItemDto = new ItemDto();
        updateItemDto.setName("UpdatedItem2");
        updateItemDto.setDescription("UpdatedDescription2");
        updateItemDto.setAvailable(false);

        ItemDto updatedItem = itemService.update(createdItemId, updateItemDto, createdUserId);

        assertThat(updatedItem.getName()).isEqualTo("UpdatedItem2");
        assertThat(updatedItem.getDescription()).isEqualTo("UpdatedDescription2");
        assertThat(updatedItem.getAvailable()).isFalse();
    }

    @Test
    void getItemById_WhenItemExists_ShouldReturnItem() {
        UserDto user = new UserDto();
        user.setName("Name1");
        user.setEmail("name1@mail.ru");
        UserDto createdUser = userService.create(user);
        Integer createdUserId = createdUser.getId();

        ItemDto item = new ItemDto();
        item.setName("Item1");
        item.setDescription("Description1");
        item.setAvailable(true);
        ItemDto createdItem = itemService.create(item, createdUserId);
        Integer createdItemId = createdItem.getId();

        ItemDto retrievedItem = itemService.getItemById(createdItemId);

        assertThat(retrievedItem.getId()).isEqualTo(createdItemId);
        assertThat(retrievedItem.getName()).isEqualTo("Item1");
        assertThat(retrievedItem.getDescription()).isEqualTo("Description1");
        assertThat(retrievedItem.getAvailable()).isTrue();
        assertThat(retrievedItem.getUserId()).isEqualTo(createdUserId);
    }

    @Test
    void getItemById_WhenItemDoesNotExist_ShouldThrowNotFoundException() {
        Integer nonExistentItemId = Integer.MAX_VALUE;

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.getItemById(nonExistentItemId));

        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void addComment_WhenUserHasApprovedBooking_ShouldAddComment() throws InterruptedException {
        UserDto owner = new UserDto();
        owner.setName("Owner");
        owner.setEmail("owner@mail.ru");
        UserDto createdOwner = userService.create(owner);
        Integer ownerId = createdOwner.getId();

        UserDto booker = new UserDto();
        booker.setName("Booker");
        booker.setEmail("booker@mail.ru");
        UserDto createdBooker = userService.create(booker);
        Integer bookerId = createdBooker.getId();

        ItemDto item = new ItemDto();
        item.setName("Item3");
        item.setDescription("Description3");
        item.setAvailable(true);
        ItemDto createdItem = itemService.create(item, ownerId);
        Integer itemId = createdItem.getId();

        LocalDateTime start = LocalDateTime.now().plusSeconds(1);
        LocalDateTime end = LocalDateTime.now().plusSeconds(2);;

        BookingRequestDto bookingRequest = new BookingRequestDto();
        bookingRequest.setItemId(itemId);
        bookingRequest.setStart(start);
        bookingRequest.setEnd(end);

        BookingDto createdBooking = bookingService.create(bookingRequest, bookerId);

        bookingService.approve(createdBooking.getId(), true, ownerId);

        CommentRequestDto commentRequest = new CommentRequestDto();
        commentRequest.setText("Great item!");

        TimeUnit.SECONDS.sleep( 5 );
        CommentDto createdComment = itemService.addComment(bookerId, itemId, commentRequest);

        assertThat(createdComment.getText()).isEqualTo("Great item!");
        assertThat(createdComment.getAuthorName()).isEqualTo("Booker");
        assertThat(createdComment.getItemId()).isEqualTo(itemId);
        assertThat(createdComment.getUserId()).isEqualTo(bookerId);
    }

    @Test
    void updateItem_WhenCalledByNotOwner_ShouldThrowNotFoundException() {
        UserDto owner = new UserDto();
        owner.setName("Owner2");
        owner.setEmail("owner2@mail.ru");
        UserDto createdOwner = userService.create(owner);
        Integer ownerId = createdOwner.getId();

        UserDto notOwner = new UserDto();
        notOwner.setName("NotOwner");
        notOwner.setEmail("notowner@mail.ru");
        UserDto createdNotOwner = userService.create(notOwner);
        Integer notOwnerId = createdNotOwner.getId();

        ItemDto item = new ItemDto();
        item.setName("Item6");
        item.setDescription("Description6");
        item.setAvailable(true);
        ItemDto createdItem = itemService.create(item, ownerId);
        Integer createdItemId = createdItem.getId();

        ItemDto updateItemDto = new ItemDto();
        updateItemDto.setName("UpdatedItem6");

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.update(createdItemId, updateItemDto, notOwnerId));

        assertTrue(exception.getMessage().contains("Owner id =/= provided id"));
    }

    @Test
    void createItem_WhenUserDoesNotExist_ShouldThrowNotFoundException() {
        Integer nonExistentUserId = Integer.MAX_VALUE;

        ItemDto item = new ItemDto();
        item.setName("Item5");
        item.setDescription("Description5");
        item.setAvailable(true);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.create(item, nonExistentUserId));

        assertTrue(exception.getMessage().contains("owner not found"));
    }
}
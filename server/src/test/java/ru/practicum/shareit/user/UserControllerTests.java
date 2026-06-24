package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.common.user.UserDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.request.ItemRequestService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private ItemService itemService;

    @MockBean
    private ItemRequestService requestService;

    @Test
    void getUserById_ReturnsOk() throws Exception {
        Integer userId = 1;
        UserDto responseDto = UserDto.builder().id(userId).name("John").email("john@test.com").build();

        when(userService.getUserById(userId)).thenReturn(responseDto);

        mockMvc.perform(get("/internal/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("John"));
    }

    @Test
    void createUser_ReturnsCreated() throws Exception {
        UserDto requestDto = UserDto.builder().name("John").email("john@test.com").build();
        UserDto responseDto = UserDto.builder().id(1).name("John").email("john@test.com").build();

        when(userService.create(requestDto)).thenReturn(responseDto);

        mockMvc.perform(post("/internal/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john@test.com"));
    }

    @Test
    void updateUser_InvalidId_ReturnsBadRequest() throws Exception {
        UserDto requestDto = UserDto.builder().id(2).name("John").email("john@test.com").build();

        when(userService.update(1, requestDto))
                .thenThrow(new ValidationException("ID in request =/= ID in URL"));

        mockMvc.perform(patch("/internal/users/{userId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_NotFound_Returns404() throws Exception {
        Integer userId = 999;

        when(userService.getUserById(userId)).thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get("/internal/users/{userId}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUser_WhenInvalidJson_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/internal/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json string"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_WhenInvalidIdType_ReturnsBadRequest() throws Exception {
        mockMvc.perform(patch("/internal/users/{userId}", "not_a_number")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}

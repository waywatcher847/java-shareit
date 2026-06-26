package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.common.user.UserDto;
import ru.practicum.common.user.UserDtoNew;
import ru.practicum.shareit.exception.NotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void createUser_shouldReturn201() throws Exception {
        UserDtoNew newUser = new UserDtoNew();
        newUser.setName("User");
        newUser.setEmail("User@yandex.ru");

        UserDto savedUser = new UserDto();
        savedUser.setId(1);
        savedUser.setName("User");
        savedUser.setEmail("user@yandex.ru");

        when(userService.create(any(UserDtoNew.class))).thenReturn(savedUser);

        mockMvc.perform(post("/internal/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/users/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("User"));
    }

    @Test
    void getUserById_shouldReturn200() throws Exception {
        UserDto user = new UserDto();
        user.setId(1);
        user.setName("User");

        when(userService.getById(1)).thenReturn(user);

        mockMvc.perform(get("/internal/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteUser_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/internal/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void update_shouldReturn200() throws Exception {
        UserDtoNew request = new UserDtoNew();
        request.setName("upd Name");
        request.setEmail("upd@yandex.ru");

        UserDto updatedUser = new UserDto();
        updatedUser.setId(1);
        updatedUser.setName("upd Name");
        updatedUser.setEmail("upd@yandex.ru");

        when(userService.update(any(UserDtoNew.class), eq(1)))
                .thenReturn(updatedUser);

        mockMvc.perform(patch("/internal/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("upd Name"))
                .andExpect(jsonPath("$.email").value("upd@yandex.ru"));
    }

    @Test
    void update_partialData_shouldReturn200() throws Exception {
        UserDtoNew request = new UserDtoNew();
        request.setName("Only Name Updated");

        UserDto updatedUser = new UserDto();
        updatedUser.setId(1);
        updatedUser.setName("Only Name Updated");
        updatedUser.setEmail("old@yandex.ru");

        when(userService.update(any(UserDtoNew.class), eq(1)))
                .thenReturn(updatedUser);

        mockMvc.perform(patch("/internal/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Only Name Updated"))
                .andExpect(jsonPath("$.email").value("old@yandex.ru"));
    }

    @Test
    void updateUser_notFound_shouldReturn404() throws Exception {
        UserDtoNew request = new UserDtoNew();
        request.setName("upd Name");
        request.setEmail("upd@yandex.ru");

        when(userService.update(any(UserDtoNew.class), eq(999)))
                .thenThrow(new NotFoundException("User with id=999 not found"));

        mockMvc.perform(patch("/internal/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_notFound_shouldReturn404() throws Exception {
        org.mockito.Mockito.doThrow(new NotFoundException("User with id=999 not found"))
                .when(userService).delete(eq(999));

        mockMvc.perform(delete("/internal/users/999"))
                .andExpect(status().isNotFound());
    }
}
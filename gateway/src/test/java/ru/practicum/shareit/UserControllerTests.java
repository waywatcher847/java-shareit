package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.UserController;
import ru.practicum.common.user.UserDto;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTests {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserClient userClient;


    @Test
    void UserController_WhenNameIsNull_ReturnsBadRequest() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName(null);
        userDto.setEmail("email@email.ru");

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void UserController_WhenCreatingUser_ReturnsOkStatus() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("Name");
        userDto.setEmail("email@email.ru");

        when(userClient.create(userDto))
                .thenReturn(ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(userDto));

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @Test
    void UserController_WhenNameIsEmpty_ReturnsBadRequest() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("");
        userDto.setEmail("email@email.ru");

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void UserController_WhenNameIsBlank_ReturnsBadRequest() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("    ");
        userDto.setEmail("email@email.ru");

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void UserController_WhenEmailIsBlank_ReturnsBadRequest() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("Name");
        userDto.setEmail("    ");

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void UserController_WhenEmailIsNull_ReturnsBadRequest() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("Name");
        userDto.setEmail(null);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void UserController_WhenEmailIsInvalid_ReturnsBadRequest() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("Name");
        userDto.setEmail("@email.ru");

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());

        UserDto userDto2 = new UserDto();
        userDto2.setName("Name");
        userDto2.setEmail("email@");

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto2)))
                .andExpect(status().isBadRequest());

        UserDto userDto3 = new UserDto();
        userDto3.setName("Name");
        userDto3.setEmail("email");

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto3)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void UserController_WhenEmailIsEmpty_ReturnsBadRequest() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("Name");
        userDto.setEmail("");

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void UserController_WhenEditingUserWithBlankEmail_ReturnsBadRequest() throws Exception {
        Integer userId = 1;

        UserDto userDto = new UserDto();
        userDto.setName("Name");
        userDto.setEmail("    ");

        mvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void UserController_WhenEditingUser_ReturnsOkStatus() throws Exception {
        Integer userId = 1;

        UserDto userDto = new UserDto();
        userDto.setName("Name");
        userDto.setEmail("email@email.ru");

        when(userClient.update(userId, userDto))
                .thenReturn(ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(userDto));

        mvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @Test
    void UserController_WhenGettingUser_ReturnsOkStatus() throws Exception {
        Integer userId = 1;

        mvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk());
    }

    @Test
    void UserController_WhenEditingUserWithInvalidEmail_ReturnsBadRequest() throws Exception {
        Integer userId = 1;

        UserDto userDto = new UserDto();
        userDto.setName("Name");
        userDto.setEmail("@email.ru");

        mvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());

        UserDto userDto2 = new UserDto();
        userDto2.setName("Name");
        userDto2.setEmail("email@");

        mvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto2)))
                .andExpect(status().isBadRequest());

        UserDto userDto3 = new UserDto();
        userDto3.setName("Name");
        userDto3.setEmail("email");

        mvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto3)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void UserController_WhenDeletingUser_ReturnsNoContentStatus() throws Exception {
        Integer userId = 1;

        mvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    void UserController_WhenGettingUsers_ReturnsOkStatus() throws Exception {
        Integer userId = 1;

        mvc.perform(get("/users"))
                .andExpect(status().isOk());
    }
}
package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.common.user.UserDtoNew;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    @Test
    void getUserDto_shouldReturnOk() throws Exception {
        when(userClient.getUserDto(1)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getUserList_shouldReturnOk() throws Exception {
        when(userClient.getUserList()).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());
    }

    @Test
    void create_withValidData_shouldReturnCreated() throws Exception {
        UserDtoNew user = UserDtoNew.builder().name("User").email("user@test.com").build();
        when(userClient.create(any(UserDtoNew.class))).thenReturn(ResponseEntity.status(201).build());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated());
    }

    @Test
    void create_withEmptyName_shouldReturnBadRequest() throws Exception {
        UserDtoNew user = UserDtoNew.builder().name("").email("john@test.com").build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_withNullEmail_shouldReturnBadRequest() throws Exception {
        UserDtoNew user = UserDtoNew.builder().name("John").build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_withInvalidEmail_shouldReturnBadRequest() throws Exception {
        UserDtoNew user = UserDtoNew.builder().email("not-an-email").build();

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_withInvalidEmail_shouldReturnBadRequest() throws Exception {
        UserDtoNew user = UserDtoNew.builder().name("User").email("invalid-email").build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturnOk() throws Exception {
        UserDtoNew user = UserDtoNew.builder().name("Updated").email("upd@test.com").build();
        when(userClient.update(eq(1), any(UserDtoNew.class))).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        when(userClient.delete(1)).thenReturn(ResponseEntity.noContent().build());

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());
    }
}
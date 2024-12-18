package com.itm.space.backendresources.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.exception.BackendResourcesException;
import com.itm.space.backendresources.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WithMockUser(roles = "MODERATOR")
class UserControllerTest extends BaseIntegrationTest {

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getUserById() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResponse mockResponse = new UserResponse(
                "Ivan",
                "Ivanov",
                "ivan@mail.ru",
                List.of("ROLE_MODERATOR"),
                List.of("Moderators")
        );
        Mockito.when(userService.getUserById(userId)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("Ivan"))
                .andExpect(jsonPath("$.lastName").value("Ivanov"))
                .andExpect(jsonPath("$.email").value("ivan@mail.ru"))
                .andExpect(jsonPath("$.roles").value("ROLE_MODERATOR"))
                .andExpect(jsonPath("$.groups").value("Moderators"));
        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void getUserById_UserNotFound() throws Exception {
        UUID userId = UUID.randomUUID();

        Mockito.when(userService.getUserById(userId)).thenThrow(new BackendResourcesException("User not found", HttpStatus.NOT_FOUND));
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }

    @Test
    public void testCreateUser() throws Exception{
        UserRequest userRequest =
                new UserRequest("username","email@example.com",
                        "password","firstName","lastName");
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk());
        verify(userService,times(1)).createUser(any(UserRequest.class));
    }

    @Test
    public void testCreateUser_WithInvalidData() throws Exception{
        UserRequest userRequest =
                new UserRequest("1","email",
                        "123","","");
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value("Username should be between 2 and 30 characters long"))
                .andExpect(jsonPath("$.email").value("Email should be valid"))
                .andExpect(jsonPath("$.password").value("Password should be greater than 4 characters long"));
    }

    @Test
    @WithMockUser(username = "Moderator", roles = "MODERATOR")
    void helloEndpoint() throws Exception {
        mockMvc.perform(get("/api/users/hello"))
                .andExpect(status().isOk());
    }
}
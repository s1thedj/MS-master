package com.itm.space.backendresources.api;

import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(roles = "MODERATOR")
public class UserServiceTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private Keycloak keycloak;

    private final String realm = "ITM";

    private final UserRequest testRequest = new UserRequest(
            "testUsername",
            "testEmail@mail.ru",
            "testPassword",
            "testFirstName",
            "testLastName"
    );

    private void removeUserIfExists(String username) {
        List<UserRepresentation> existingUsers = keycloak.realm(realm).users().search(testRequest.getUsername());
        if (!existingUsers.isEmpty()) {
            keycloak.realm(realm).users().get(existingUsers.get(0).getId()).remove();
        }
    }

    @BeforeEach
    void setUp() {
        removeUserIfExists(testRequest.getUsername());
    }

    @AfterEach
    void tearDown() {
        removeUserIfExists(testRequest.getUsername());
    }

    @Test
    void testCreateUser() throws Exception {
        mvc.perform(requestWithContent(post("/api/users"), testRequest))
                .andExpect(status().isOk());

        UserRepresentation newUser = keycloak.realm(realm).users().search(testRequest.getUsername()).get(0);

        assertEquals(testRequest.getUsername().toLowerCase(), newUser.getUsername().toLowerCase());
        assertEquals(testRequest.getEmail().toLowerCase(), newUser.getEmail().toLowerCase());
        assertEquals(testRequest.getFirstName().toLowerCase(), newUser.getFirstName().toLowerCase());
        assertEquals(testRequest.getLastName().toLowerCase(), newUser.getLastName().toLowerCase());
        keycloak.realm(realm).users().get(newUser.getId()).remove();
    }

    @Test
    void testGetUserById() throws Exception {
        mvc.perform(requestWithContent(post("/api/users"), testRequest))
                .andExpect(status().isOk());

        UserRepresentation newUser = keycloak.realm(realm).users().search(testRequest.getUsername()).get(0);
        UserResponse response = userService.getUserById(UUID.fromString(newUser.getId()));

        assertNotNull(response);
        assertEquals(testRequest.getUsername().toLowerCase(), newUser.getUsername().toLowerCase());
        assertEquals(testRequest.getEmail().toLowerCase(), newUser.getEmail().toLowerCase());
        assertEquals(testRequest.getFirstName().toLowerCase(), newUser.getFirstName().toLowerCase());
        assertEquals(testRequest.getLastName().toLowerCase(), newUser.getLastName().toLowerCase());
    }
}

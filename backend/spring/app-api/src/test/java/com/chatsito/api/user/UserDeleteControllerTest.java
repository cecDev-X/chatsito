package com.chatsito.api.user;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chatsito.api.auth.LegacyJwtService;
import com.chatsito.api.config.LegacyValidationExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UserDeleteControllerTest {
    private static final String USER_ID = "000000000000000000000004";

    private UserDeleteService deleteService;
    private LegacyJwtService jwtService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        deleteService = Mockito.mock(UserDeleteService.class);
        jwtService = Mockito.mock(LegacyJwtService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserDeleteController(deleteService, jwtService))
                .setControllerAdvice(new LegacyValidationExceptionHandler())
                .build();
    }

    @Test
    void returnsLegacySuccessMessage() throws Exception {
        when(jwtService.authenticate("Bearer valid-token")).thenReturn(USER_ID);
        when(deleteService.delete(USER_ID)).thenReturn(true);

        performDelete(USER_ID)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("user Delted Successfully."));
    }

    @Test
    void rejectsDifferentAuthenticatedUser() throws Exception {
        when(jwtService.authenticate("Bearer valid-token")).thenReturn("different-user");

        performDelete(USER_ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("you are not authorized to delete this profile"));
    }

    @Test
    void returnsLegacyNullForMissingUser() throws Exception {
        when(jwtService.authenticate("Bearer valid-token")).thenReturn(USER_ID);
        when(deleteService.delete(USER_ID)).thenReturn(false);

        performDelete(USER_ID)
                .andExpect(status().isOk())
                .andExpect(content().string("null"));
    }

    private org.springframework.test.web.servlet.ResultActions performDelete(String id) throws Exception {
        return mockMvc.perform(delete("/user/delete/{id}", id)
                .header("Authorization", "Bearer valid-token"));
    }
}

package com.chatsito.api.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record SignupRequest(
        @NotNull String firstName,
        @NotNull String lastName,
        @NotNull @Email String email,
        @NotNull String password) {
}

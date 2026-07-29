package com.chatsito.api.user;

import jakarta.validation.constraints.NotBlank;

public record GoogleAuthRequest(@NotBlank String credential) {
}

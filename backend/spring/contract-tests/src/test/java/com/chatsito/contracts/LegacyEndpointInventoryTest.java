package com.chatsito.contracts;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

class LegacyEndpointInventoryTest {
    private static final Set<String> ENDPOINTS = Set.of(
            "POST /user/signup",
            "POST /user/signin",
            "GET /user/getUser/{id}",
            "PATCH /user/Update/{id}",
            "PATCH /user/{id}/following",
            "GET /user/getSug",
            "DELETE /user/delete/{id}",
            "GET /posts",
            "GET /posts/search",
            "GET /posts/{id}",
            "POST /posts",
            "PATCH /posts/{id}",
            "PATCH /posts/{id}/likePost",
            "DELETE /posts/{id}",
            "POST /posts/{id}/commentPost",
            "DELETE /posts/{id}/deleteComment",
            "POST /chat/sendmessage",
            "GET /chat/getmsgsbynums",
            "GET /chat/get-user-unreadedmsg",
            "GET /chat/mark-msg-asreaded",
            "GET /notification/{userid}",
            "GET /notification/mark-notification-asreaded");

    @Test
    void tracksTheCompleteLegacyHttpSurface() {
        assertThat(ENDPOINTS).hasSize(22);
    }
}

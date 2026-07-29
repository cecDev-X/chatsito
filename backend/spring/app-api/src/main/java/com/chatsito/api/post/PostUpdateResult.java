package com.chatsito.api.post;

public record PostUpdateResult(Status status, PostResponse post) {
    public enum Status {
        SUCCESS,
        NOT_FOUND,
        NOT_AUTHORIZED,
        FAILED
    }

    static PostUpdateResult success(PostResponse post) {
        return new PostUpdateResult(Status.SUCCESS, post);
    }

    static PostUpdateResult failure(Status status) {
        return new PostUpdateResult(status, null);
    }
}

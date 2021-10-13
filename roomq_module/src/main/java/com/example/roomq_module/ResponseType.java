package com.example.roomq_module;

enum CompletionType {
    ENTER_DIRECTLY,
    ENTER_FROM_ROOM,
    ENTER_WITH_ERROR
}

enum ExtendSessionResponse {
    EXTENDED,
    EXPIRED,
    SERVER_ERROR
}

enum DeleteSessionResponse {
    SUCCESS,
    SERVER_ERROR
}
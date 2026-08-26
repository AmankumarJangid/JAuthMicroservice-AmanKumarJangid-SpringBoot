package com.gamegrind.dev.AuthApplication.dtos;

public record LoginRequest(
        String email,
        String password
) {
}

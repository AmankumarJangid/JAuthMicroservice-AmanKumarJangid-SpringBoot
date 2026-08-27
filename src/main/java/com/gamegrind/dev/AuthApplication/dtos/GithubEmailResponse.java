package com.gamegrind.dev.AuthApplication.dtos;

public record GithubEmailResponse(
        String email,
        boolean primary,
        boolean verified,
        String visibility
) {
}

package com.gamegrind.dev.AuthApplication.helpers;

import java.util.UUID;

public class UserHelper {
    public static UUID parseUserId(String userId) throws IllegalArgumentException {
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid user ID format: " + userId, e);
        }
    }
}

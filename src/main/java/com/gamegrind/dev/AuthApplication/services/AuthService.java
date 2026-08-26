package com.gamegrind.dev.AuthApplication.services;

import com.gamegrind.dev.AuthApplication.dtos.LoginRequest;
import com.gamegrind.dev.AuthApplication.dtos.TokenResponse;
import com.gamegrind.dev.AuthApplication.dtos.UserDto;

public interface AuthService {
    UserDto registerUser(UserDto userDto);

    TokenResponse loginUser(LoginRequest loginRequest);

    // login user

}

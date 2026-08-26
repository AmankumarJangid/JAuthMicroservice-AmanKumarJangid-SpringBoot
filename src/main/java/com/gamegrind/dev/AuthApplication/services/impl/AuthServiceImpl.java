package com.gamegrind.dev.AuthApplication.services.impl;

import com.gamegrind.dev.AuthApplication.dtos.LoginRequest;
import com.gamegrind.dev.AuthApplication.dtos.TokenResponse;
import com.gamegrind.dev.AuthApplication.dtos.UserDto;
import com.gamegrind.dev.AuthApplication.services.AuthService;
import com.gamegrind.dev.AuthApplication.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto registerUser(UserDto userDto) {
        // lgoic to verify all
        // verify email
        // verify password strength
        // verify username
        // if all is good, then create user
        // default roles

        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
        return userService.createUser(userDto);
    }

    @Override
    public TokenResponse loginUser(LoginRequest loginRequest) {
        return null;
    }


}

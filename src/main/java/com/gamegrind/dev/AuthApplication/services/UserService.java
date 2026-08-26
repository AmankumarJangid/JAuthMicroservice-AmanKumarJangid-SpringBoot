package com.gamegrind.dev.AuthApplication.services;

import com.gamegrind.dev.AuthApplication.dtos.UserDto;

public interface UserService {
    UserDto createUser(UserDto userDto);

    UserDto updateUser(String userId, UserDto userDto);

    void deleteUser(String userId);

    UserDto getUserById(String userId);

    UserDto getUserByEmail(String email);

    Iterable<UserDto> getAllUsers();
}

package com.gamegrind.dev.AuthApplication.services.impl;

import com.gamegrind.dev.AuthApplication.dtos.RoleDto;
import com.gamegrind.dev.AuthApplication.dtos.UserDto;
import com.gamegrind.dev.AuthApplication.entities.Provider;
import com.gamegrind.dev.AuthApplication.entities.Role;
import com.gamegrind.dev.AuthApplication.entities.User;
import com.gamegrind.dev.AuthApplication.exceptions.ResourceNotFoundException;
import com.gamegrind.dev.AuthApplication.helpers.UserHelper;
import com.gamegrind.dev.AuthApplication.repositories.RefreshTokenRepository;
import com.gamegrind.dev.AuthApplication.repositories.UserRepository;
import com.gamegrind.dev.AuthApplication.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;


    @Override
    public UserDto createUser(UserDto userDto) {

        if( userDto.getEmail() == null) {
            throw new IllegalArgumentException("Email is required");
        }
        if( userDto.getName() == null){
            throw new IllegalArgumentException("Username is required");
        }
        if( userDto.getPassword() == null){
            throw new IllegalArgumentException("Password is required");
        }


        if( userRepository.existsByEmail(userDto.getEmail()) ) {
            throw new IllegalArgumentException("User with given email already exists");
        }

        // if your have extra checks for username or password, you can add them here
        User user = modelMapper.map(userDto, User.class);

        user.setProvider(userDto.getProvider() != null ? userDto.getProvider() : Provider.LOCAL);

        // role assign here to user --- for authorization
        //TODO:
//        if( userDto.getRoles() != null){
//            Set<RoleDto> roles = userDto.getRoles();
//
//
//        }

        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDto.class);
    }

    @Override
    public UserDto updateUser(String userId, UserDto userDto) {
        UUID uid = UUID.fromString(userId);
        User user = userRepository.findById(uid).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // we are not going to change email and username in update, but you can add logic to allow that if you want
        if( userDto.getName() != null){
            user.setName(userDto.getName());
        }
        if( userDto.getImage() != null) {
            user.setImage(userDto.getImage());
        }
        if( userDto.getProvider() != null && userDto.getProvider() != user.getProvider()) {
            user.setProvider(userDto.getProvider());
        }

        // TODO : change password updation logic
        // RESULT: Done updating in
        if( userDto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode( userDto.getPassword()));
        }
        user.setEnable(userDto.isEnable());
        User updatedUser = userRepository.save(user);
        return modelMapper.map(updatedUser, UserDto.class);
    }

    @Override
    public void deleteUser(String userId) {
        UUID uid = UUID.fromString(userId);
        /*if( !userRepository.existsById(uid) ) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }*/

        User user = userRepository.findById(uid).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        refreshTokenRepository.deleteAllByUser(user);

        userRepository.delete(user);
    }

    @Override
    @Transactional
    public UserDto getUserById(String userId) {
        UUID uid = UserHelper.parseUserId(userId);
        User user = userRepository.findById(uid).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    @Transactional
    public UserDto getUserByEmail(String email) {

        // custom exception handling for user not found by email
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    @Transactional // to avoid LazyInitializationException when fetching roles
    public Iterable<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .toList();
    }
}

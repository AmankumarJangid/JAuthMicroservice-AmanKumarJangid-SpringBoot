package com.gamegrind.dev.AuthApplication.security;

import com.gamegrind.dev.AuthApplication.exceptions.ResourceNotFoundException;
import com.gamegrind.dev.AuthApplication.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // our username is email in our case, so we will find the user by email and return the user details
        return userRepository
                .findByEmail(username)
                .orElseThrow(()-> new ResourceNotFoundException("Invalid Email or Password!!: "));
    }
}

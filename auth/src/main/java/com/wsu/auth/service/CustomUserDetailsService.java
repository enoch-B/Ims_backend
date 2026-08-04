package com.wsu.auth.service;


import com.wsu.auth.UserPrincipal;
import com.wsu.core.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String userName) {
        var user = userRepository.findByUserName(userName);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + userName);
        }

        return new UserPrincipal(user);
    }
}

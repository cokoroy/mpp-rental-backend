package com.mpp.rental.service;

import com.mpp.rental.model.User;
import com.mpp.rental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * CustomUserDetailsService - Implements Spring Security's UserDetailsService
 * Loads user data from database for authentication
 */
@Service
@RequiredArgsConstructor // Lombok: generates constructor for final fields
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user by username (email in our case)
     * This method is called by Spring Security during authentication
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Find user by email
        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Convert our User entity to Spring Security's UserDetails
        // We need to provide:
        // 1. Username (email)
        // 2. Password (hashed)
        // 3. Authorities (roles/permissions)
        return new org.springframework.security.core.userdetails.User(
                user.getUserEmail(),
                user.getUserPassword(),
                // Grant authority based on user category (MPP, STUDENT, NON_STUDENT)
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getUserCategory().name()))
        );
    }
}
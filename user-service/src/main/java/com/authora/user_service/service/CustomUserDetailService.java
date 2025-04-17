package com.authora.user_service.service;

import com.authora.user_service.repository.UserRepository;
import com.authora.user_service.modal.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;



    public CustomUserDetailService(UserRepository userRepository
                                  ) {
        this.userRepository = userRepository;

    }




    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        System.out.println("Loading user details for: " + email);
        System.out.println("Role ID: " + user.getRole().getRoleId());
        System.out.println("Role Name: " + user.getRole().getRoleName());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().getRoleName()))
        );
    }
}

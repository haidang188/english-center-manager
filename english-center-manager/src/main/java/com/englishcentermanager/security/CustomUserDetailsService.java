package com.englishcentermanager.security;

import com.englishcentermanager.entity.User;
import com.englishcentermanager.entity.enums;
import com.englishcentermanager.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Khong tim thay tai khoan"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(new SimpleGrantedAuthority(normalizeRole(user.getRole().getName())))
                .disabled(user.getStatus() == enums.UserStatus.INACTIVE)
                .build();
    }

    private String normalizeRole(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            throw new UsernameNotFoundException("Tai khoan chua duoc gan vai tro");
        }
        return roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
    }
}

package com.englishcentermanager.config;

import com.englishcentermanager.entity.Role;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.entity.enums;
import com.englishcentermanager.repository.RoleRepository;
import com.englishcentermanager.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements ApplicationRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        Role adminRole = ensureRole("ADMIN", "Quan tri vien");
        Role staffRole = ensureRole("STAFF", "Nhan vien");
        ensureRole("TEACHER", "Giao vien");
        ensureRole("STUDENT", "Hoc vien");

        ensureUser(
                "admin@english-center.local",
                "admin123",
                "Default Admin",
                "0900000001",
                adminRole
        );
        ensureUser(
                "staff@english-center.local",
                "staff123",
                "Default Staff",
                "0900000002",
                staffRole
        );
    }

    private Role ensureRole(String name, String description) {
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(name);
                    role.setDescription(description);
                    return roleRepository.save(role);
                });
    }

    private void ensureUser(String email,
                            String rawPassword,
                            String fullName,
                            String phoneNumber,
                            Role role) {
        if (userRepository.existsByEmail(email)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName);
        user.setPhoneNumber(phoneNumber);
        user.setRole(role);
        user.setStatus(enums.UserStatus.ACTIVE);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        userRepository.save(user);
    }
}

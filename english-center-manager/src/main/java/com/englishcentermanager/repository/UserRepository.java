package com.englishcentermanager.repository;

import com.englishcentermanager.entity.Role;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.entity.enums;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByIdentityNumber(String identityNumber);
    List<User> findByRole(Role role);
    List<User> findByStatus(enums.UserStatus status);
    List<User> findByFullNameContainingIgnoreCase(String keyword);
    List<User> findByEmailContainingIgnoreCase(String keyword);
    List<User> findByPhoneNumberContaining(String phoneNumber);
}

package com.englishcentermanager.service;

import com.englishcentermanager.entity.Role;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.entity.enums;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> findAll();

    Optional<User> findById(Long id);

    User save(User user);

    User update(Long id, User user);

    void deleteById(Long id);

    void lockUser(Long id);

    void unlockUser(Long id);

    List<User> findByRole(Role role);

    List<User> findByStatus(enums.UserStatus status);

    List<User> searchByKeyword(String keyword);

    boolean existsByEmail(String email);

    boolean existsByIdentityNumber(String identityNumber);
    Optional<User> findByEmail(String email);
    User register(User user);
    User createByAdmin(User user);
    User updateByAdmin(Long id, User user);
}

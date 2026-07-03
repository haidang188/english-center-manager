package com.englishcentermanager.service;
import com.englishcentermanager.entity.Role;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.entity.enums.UserStatus;
import com.englishcentermanager.repository.UserRepository;
import com.englishcentermanager.service.UserService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User save(User user) {
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    public User update(Long id, User user) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        existingUser.setEmail(user.getEmail());
        existingUser.setPassword(user.getPassword());
        existingUser.setPhoneNumber(user.getPhoneNumber());
        existingUser.setFullName(user.getFullName());
        existingUser.setDateOfBirth(user.getDateOfBirth());
        existingUser.setAddress(user.getAddress());
        existingUser.setIdentityNumber(user.getIdentityNumber());
        existingUser.setRole(user.getRole());
        existingUser.setStatus(user.getStatus());
        existingUser.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(existingUser);
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public void lockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        user.setStatus(UserStatus.INACTIVE);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    @Override
    public void unlockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        user.setStatus(UserStatus.ACTIVE);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    @Override
    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }

    @Override
    public List<User> findByStatus(UserStatus status) {
        return userRepository.findByStatus(status);
    }

    @Override
    public List<User> searchByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAll();
        }

        return userRepository.findByFullNameContainingIgnoreCase(keyword.trim());
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByIdentityNumber(String identityNumber) {
        return userRepository.existsByIdentityNumber(identityNumber);
    }
}
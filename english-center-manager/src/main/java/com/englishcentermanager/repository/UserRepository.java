package com.englishcentermanager.repository;

import com.englishcentermanager.entity.Role;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.entity.enums;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByIdentityNumber(String identityNumber);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByIdentityNumberAndIdNot(String identityNumber, Long id);
    List<User> findByRole(Role role);
    List<User> findByStatus(enums.UserStatus status);
    List<User> findByFullNameContainingIgnoreCase(String keyword);
    List<User> findByEmailContainingIgnoreCase(String keyword);
    List<User> findByPhoneNumberContaining(String phoneNumber);

    @Query("""
            select u from User u
            where (:keyword is null
                or lower(u.fullName) like lower(concat('%', :keyword, '%'))
                or lower(u.email) like lower(concat('%', :keyword, '%'))
                or u.phoneNumber like concat('%', :keyword, '%'))
            and (:roleId is null or u.role.id = :roleId)
            and (:status is null or u.status = :status)
            """)
    Page<User> searchUsers(@Param("keyword") String keyword,
                           @Param("roleId") Long roleId,
                           @Param("status") enums.UserStatus status,
                           Pageable pageable);
}

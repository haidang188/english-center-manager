package com.englishcentermanager.repository;

import com.englishcentermanager.entity.CourseType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseTypeRepository extends JpaRepository<CourseType, Long> {
    Optional<CourseType> findByTypeCode(String typeCode);

    boolean existsByTypeCode(String typeCode);

    List<CourseType> findByActiveTrue();

    List<CourseType> findByTypeNameContainingIgnoreCase(String keyword);
}

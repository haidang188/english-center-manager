package com.englishcentermanager.service;

import com.englishcentermanager.entity.CourseType;

import java.util.List;
import java.util.Optional;

public interface CourseTypeService {
    List<CourseType> findAll();

    List<CourseType> findAllActive();

    Optional<CourseType> findById(Long id);

    Optional<CourseType> findByTypeCode(String typeCode);

    CourseType save(CourseType courseType);

    CourseType update(Long id, CourseType courseType);

    void activate(Long id);

    void deactivate(Long id);

    List<CourseType> searchByKeyword(String keyword);

    boolean existsByTypeCode(String typeCode);
}

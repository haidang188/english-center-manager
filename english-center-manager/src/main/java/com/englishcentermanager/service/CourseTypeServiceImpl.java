package com.englishcentermanager.service;

import com.englishcentermanager.entity.CourseType;
import com.englishcentermanager.repository.CourseTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CourseTypeServiceImpl implements CourseTypeService {
    private final CourseTypeRepository courseTypeRepository;

    public CourseTypeServiceImpl(CourseTypeRepository courseTypeRepository) {
        this.courseTypeRepository = courseTypeRepository;
    }

    @Override
    public List<CourseType> findAll() {
        return courseTypeRepository.findAll();
    }

    @Override
    public List<CourseType> findAllActive() {
        return courseTypeRepository.findByActiveTrue();
    }

    @Override
    public Optional<CourseType> findById(Long id) {
        return courseTypeRepository.findById(id);
    }

    @Override
    public Optional<CourseType> findByTypeCode(String typeCode) {
        return courseTypeRepository.findByTypeCode(typeCode);
    }

    @Override
    public CourseType save(CourseType courseType) {
        courseType.setActive(true);
        return courseTypeRepository.save(courseType);
    }

    @Override
    public CourseType update(Long id, CourseType courseType) {
        CourseType existingCourseType = courseTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay loai khoa hoc"));

        existingCourseType.setTypeCode(courseType.getTypeCode());
        existingCourseType.setTypeName(courseType.getTypeName());
        existingCourseType.setDescription(courseType.getDescription());
        existingCourseType.setActive(courseType.getActive());

        return courseTypeRepository.save(existingCourseType);
    }

    @Override
    public void activate(Long id) {
        CourseType courseType = courseTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay loai khoa hoc"));

        courseType.setActive(true);
        courseTypeRepository.save(courseType);
    }

    @Override
    public void deactivate(Long id) {
        CourseType courseType = courseTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay loai khoa hoc"));

        courseType.setActive(false);
        courseTypeRepository.save(courseType);
    }

    @Override
    public List<CourseType> searchByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return courseTypeRepository.findAll();
        }

        return courseTypeRepository.findByTypeNameContainingIgnoreCase(keyword.trim());
    }

    @Override
    public boolean existsByTypeCode(String typeCode) {
        return courseTypeRepository.existsByTypeCode(typeCode);
    }
}

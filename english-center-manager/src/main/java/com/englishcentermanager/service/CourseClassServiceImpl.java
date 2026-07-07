package com.englishcentermanager.service;

import com.englishcentermanager.entity.Course;
import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.entity.enums;
import com.englishcentermanager.repository.CourseClassRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CourseClassServiceImpl implements CourseClassService {
    private final CourseClassRepository courseClassRepository;

    public CourseClassServiceImpl(CourseClassRepository courseClassRepository) {
        this.courseClassRepository = courseClassRepository;
    }

    @Override
    public List<CourseClass> findAll() {
        return courseClassRepository.findAll();
    }

    @Override
    public Optional<CourseClass> findById(Long id) {
        return courseClassRepository.findById(id);
    }

    @Override
    public Optional<CourseClass> findByClassCode(String classCode) {
        return courseClassRepository.findByClassCode(classCode);
    }

    @Override
    public CourseClass save(CourseClass courseClass) {
        courseClass.setCreatedAt(LocalDateTime.now());
        courseClass.setUpdatedAt(LocalDateTime.now());
        return courseClassRepository.save(courseClass);
    }

    @Override
    public CourseClass update(Long id, CourseClass courseClass) {
        CourseClass existingClass = courseClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay lop hoc"));

        existingClass.setClassCode(courseClass.getClassCode());
        existingClass.setClassName(courseClass.getClassName());
        existingClass.setCourse(courseClass.getCourse());
        existingClass.setTeacher(courseClass.getTeacher());
        existingClass.setStartDate(courseClass.getStartDate());
        existingClass.setEndDate(courseClass.getEndDate());
        existingClass.setStatus(courseClass.getStatus());
        existingClass.setUpdatedAt(LocalDateTime.now());

        return courseClassRepository.save(existingClass);
    }

    @Override
    public void updateStatus(Long id, enums.ClassStatus status) {
        CourseClass courseClass = courseClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay lop hoc"));

        courseClass.setStatus(status);
        courseClass.setUpdatedAt(LocalDateTime.now());
        courseClassRepository.save(courseClass);
    }

    @Override
    public List<CourseClass> findByCourse(Course course) {
        return courseClassRepository.findByCourse(course);
    }

    @Override
    public List<CourseClass> findByTeacher(User teacher) {
        return courseClassRepository.findByTeacher(teacher);
    }

    @Override
    public List<CourseClass> findByStatus(enums.ClassStatus status) {
        return courseClassRepository.findByStatus(status);
    }

    @Override
    public List<CourseClass> searchByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return courseClassRepository.findAll();
        }

        return courseClassRepository.findByClassNameContainingIgnoreCase(keyword.trim());
    }

    @Override
    public boolean existsByClassCode(String classCode) {
        return courseClassRepository.existsByClassCode(classCode);
    }

    @Override
    public boolean existsByClassCodeAndIdNot(String classCode, Long id) {
        return courseClassRepository.existsByClassCodeAndIdNot(classCode, id);
    }
}

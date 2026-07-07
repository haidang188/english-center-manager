package com.englishcentermanager.service;

import com.englishcentermanager.entity.Course;
import com.englishcentermanager.entity.CourseType;
import com.englishcentermanager.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;

    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    @Override
    public List<Course> findAllActive() {
        return courseRepository.findByActiveTrue();
    }

    @Override
    public Optional<Course> findById(Long id) {
        return courseRepository.findById(id);
    }

    @Override
    public Optional<Course> findByCourseCode(String courseCode) {
        return courseRepository.findByCourseCode(courseCode);
    }

    @Override
    public Course save(Course course) {
        course.setActive(true);
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());
        return courseRepository.save(course);
    }

    @Override
    public Course update(Long id, Course course) {
        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay khoa hoc"));

        existingCourse.setCourseCode(course.getCourseCode());
        existingCourse.setCourseName(course.getCourseName());
        existingCourse.setCourseType(course.getCourseType());
        existingCourse.setDescription(course.getDescription());
        existingCourse.setActive(course.getActive());
        existingCourse.setUpdatedAt(LocalDateTime.now());

        return courseRepository.save(existingCourse);
    }

    @Override
    public void activate(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay khoa hoc"));

        course.setActive(true);
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);
    }

    @Override
    public void deactivate(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay khoa hoc"));

        course.setActive(false);
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);
    }

    @Override
    public List<Course> findByCourseType(CourseType courseType) {
        return courseRepository.findByCourseType(courseType);
    }

    @Override
    public List<Course> searchByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return courseRepository.findAll();
        }

        return courseRepository.findByCourseNameContainingIgnoreCase(keyword.trim());
    }

    @Override
    public boolean existsByCourseCode(String courseCode) {
        return courseRepository.existsByCourseCode(courseCode);
    }
}

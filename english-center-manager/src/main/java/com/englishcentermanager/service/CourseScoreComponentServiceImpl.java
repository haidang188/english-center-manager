package com.englishcentermanager.service;

import com.englishcentermanager.entity.Course;
import com.englishcentermanager.entity.CourseScoreComponent;
import com.englishcentermanager.repository.CourseScoreComponentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CourseScoreComponentServiceImpl implements CourseScoreComponentService {
    private final CourseScoreComponentRepository courseScoreComponentRepository;

    public CourseScoreComponentServiceImpl(CourseScoreComponentRepository courseScoreComponentRepository) {
        this.courseScoreComponentRepository = courseScoreComponentRepository;
    }

    @Override
    public List<CourseScoreComponent> findAll() {
        return courseScoreComponentRepository.findAll();
    }

    @Override
    public Optional<CourseScoreComponent> findById(Long id) {
        return courseScoreComponentRepository.findById(id);
    }

    @Override
    public CourseScoreComponent save(CourseScoreComponent courseScoreComponent) {
        return courseScoreComponentRepository.save(courseScoreComponent);
    }

    @Override
    public CourseScoreComponent update(Long id, CourseScoreComponent courseScoreComponent) {
        CourseScoreComponent existingComponent = courseScoreComponentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay cau hinh diem"));

        existingComponent.setCourse(courseScoreComponent.getCourse());
        existingComponent.setComponentCode(courseScoreComponent.getComponentCode());
        existingComponent.setComponentName(courseScoreComponent.getComponentName());
        existingComponent.setMaxScore(courseScoreComponent.getMaxScore());
        existingComponent.setWeightPercent(courseScoreComponent.getWeightPercent());
        existingComponent.setDisplayOrder(courseScoreComponent.getDisplayOrder());
        existingComponent.setRequired(courseScoreComponent.getRequired());
        existingComponent.setCalculated(courseScoreComponent.getCalculated());

        return courseScoreComponentRepository.save(existingComponent);
    }

    @Override
    public void deleteById(Long id) {
        courseScoreComponentRepository.deleteById(id);
    }

    @Override
    public List<CourseScoreComponent> findByCourse(Course course) {
        return courseScoreComponentRepository.findByCourse(course);
    }

    @Override
    public List<CourseScoreComponent> findByCourseOrderByDisplayOrder(Course course) {
        return courseScoreComponentRepository.findByCourseOrderByDisplayOrderAsc(course);
    }

    @Override
    public List<CourseScoreComponent> searchByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return courseScoreComponentRepository.findAll();
        }

        return courseScoreComponentRepository.findByComponentNameContainingIgnoreCase(keyword.trim());
    }

    @Override
    public boolean existsByCourseAndComponentCode(Course course, String componentCode) {
        return courseScoreComponentRepository.existsByCourseAndComponentCode(course, componentCode);
    }
}

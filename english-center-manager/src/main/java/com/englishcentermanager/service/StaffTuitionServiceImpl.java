package com.englishcentermanager.service;

import com.englishcentermanager.dto.TuitionBatchForm;
import com.englishcentermanager.entity.ClassStudent;
import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.StudentTuition;
import com.englishcentermanager.entity.TuitionBatch;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.entity.enums;
import com.englishcentermanager.repository.ClassStudentRepository;
import com.englishcentermanager.repository.CourseClassRepository;
import com.englishcentermanager.repository.StudentTuitionRepository;
import com.englishcentermanager.repository.TuitionBatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StaffTuitionServiceImpl implements StaffTuitionService {
    private final TuitionBatchRepository tuitionBatchRepository;
    private final StudentTuitionRepository studentTuitionRepository;
    private final CourseClassRepository courseClassRepository;
    private final ClassStudentRepository classStudentRepository;

    public StaffTuitionServiceImpl(TuitionBatchRepository tuitionBatchRepository,
                                   StudentTuitionRepository studentTuitionRepository,
                                   CourseClassRepository courseClassRepository,
                                   ClassStudentRepository classStudentRepository) {
        this.tuitionBatchRepository = tuitionBatchRepository;
        this.studentTuitionRepository = studentTuitionRepository;
        this.courseClassRepository = courseClassRepository;
        this.classStudentRepository = classStudentRepository;
    }

    @Override
    public List<TuitionBatch> findBatches(Long classId) {
        if (classId == null) {
            return tuitionBatchRepository.findAllByOrderByCreatedAtDesc();
        }

        CourseClass courseClass = courseClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay lop hoc"));
        return tuitionBatchRepository.findByCourseClassOrderByCreatedAtDesc(courseClass);
    }

    @Override
    public Optional<TuitionBatch> findById(Long id) {
        return tuitionBatchRepository.findById(id);
    }

    @Override
    @Transactional
    public TuitionBatch createBatch(TuitionBatchForm form, User createdBy) {
        CourseClass courseClass = courseClassRepository.findById(form.getClassId())
                .orElseThrow(() -> new RuntimeException("Khong tim thay lop hoc"));

        TuitionBatch tuitionBatch = new TuitionBatch();
        tuitionBatch.setCourseClass(courseClass);
        tuitionBatch.setAmount(form.getAmount());
        tuitionBatch.setDueDate(form.getDueDate());
        tuitionBatch.setNote(form.getNote());
        tuitionBatch.setCreatedByUser(createdBy);
        tuitionBatch.setCreatedAt(LocalDateTime.now());

        TuitionBatch savedBatch = tuitionBatchRepository.save(tuitionBatch);
        createStudentTuitions(savedBatch);

        return savedBatch;
    }

    @Override
    public List<StudentTuition> findStudentTuitions(TuitionBatch tuitionBatch) {
        return studentTuitionRepository.findByTuitionBatchOrderByStudentName(tuitionBatch);
    }

    @Override
    public long countStudentTuitions(TuitionBatch tuitionBatch) {
        return studentTuitionRepository.countByTuitionBatch(tuitionBatch);
    }

    private void createStudentTuitions(TuitionBatch tuitionBatch) {
        List<ClassStudent> classStudents = classStudentRepository.findByCourseClass(tuitionBatch.getCourseClass());

        for (ClassStudent classStudent : classStudents) {
            if (studentTuitionRepository.existsByTuitionBatchAndStudent(tuitionBatch, classStudent.getStudent())) {
                continue;
            }

            StudentTuition studentTuition = new StudentTuition();
            studentTuition.setTuitionBatch(tuitionBatch);
            studentTuition.setStudent(classStudent.getStudent());
            studentTuition.setAmount(tuitionBatch.getAmount());
            studentTuition.setStatus(enums.TuitionStatus.UNPAID);
            studentTuition.setCreatedAt(LocalDateTime.now());
            studentTuitionRepository.save(studentTuition);
        }
    }
}

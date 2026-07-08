package com.englishcentermanager.service;

import com.englishcentermanager.dto.PaymentForm;
import com.englishcentermanager.dto.TuitionBatchForm;
import com.englishcentermanager.entity.*;
import com.englishcentermanager.repository.*;
import com.englishcentermanager.security.SecurityService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Service
@Transactional
@RequiredArgsConstructor
public class StaffTuitionServiceImpl implements StaffTuitionService {

    private final TuitionBatchRepository tuitionBatchRepository;
    private final StudentTuitionRepository studentTuitionRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final CourseClassRepository courseClassRepository;
    private final ClassStudentRepository classStudentRepository;
    private final SecurityService securityService;

    @Override
    @Transactional(readOnly = true)
    public Page<TuitionBatch> getTuitionBatches(String keyword, Pageable pageable) {
        String normalizedKeyword = keyword == null || keyword.trim().isEmpty() ? null : keyword.trim();
        return tuitionBatchRepository.search(normalizedKeyword, pageable);
    }

    @Override
    @Transactional
    public TuitionBatch createBatch(TuitionBatchForm form) {

        CourseClass courseClass = courseClassRepository.findById(form.getClassId())
                .orElseThrow(() ->
                        new EntityNotFoundException("Không tìm thấy lớp học."));

        TuitionBatch batch = new TuitionBatch();

        batch.setCourseClass(courseClass);
        batch.setAmount(form.getAmount());
        batch.setDueDate(form.getDueDate());
        batch.setNote(form.getNote());

        batch.setCreatedAt(LocalDateTime.now());

        batch.setCreatedByUser(securityService.getCurrentUser());

        tuitionBatchRepository.save(batch);

        List<ClassStudent> classStudents =
                classStudentRepository.findByCourseClassIdAndStatus(
                        courseClass.getId(),
                        enums.StudentClassStatus.STUDYING
                );

        List<StudentTuition> studentTuitions = new ArrayList<>();

        for (ClassStudent item : classStudents) {

            StudentTuition tuition = new StudentTuition();

            tuition.setTuitionBatch(batch);

            tuition.setStudent(item.getStudent());

            tuition.setAmount(batch.getAmount());

            tuition.setStatus(enums.TuitionStatus.UNPAID);

            tuition.setCreatedAt(LocalDateTime.now());

            studentTuitions.add(tuition);

        }

        studentTuitionRepository.saveAll(studentTuitions);

        return batch;

    }

    @Override
    @Transactional(readOnly = true)
    public TuitionBatch getBatch(Long id) {
        return tuitionBatchRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đợt học phí"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentTuition> getStudentTuitions(Long batchId, String keyword, Pageable pageable) {

        TuitionBatch batch = getBatch(batchId);

        if (keyword == null || keyword.isBlank()) {
            return studentTuitionRepository.findByTuitionBatch(batch, pageable);
        }

        return studentTuitionRepository.search(batchId, keyword.trim(), pageable);
    }

    @Override
    @Transactional
    public void updatePayment(Long studentTuitionId, PaymentForm form) {

        StudentTuition studentTuition = studentTuitionRepository.findById(studentTuitionId).orElseThrow(() ->
                                new EntityNotFoundException("Không tìm thấy học phí."));

        PaymentHistory history = new PaymentHistory();

        history.setStudentTuition(studentTuition);

        history.setPaidAmount(form.getPaidAmount());

        history.setPaymentMethod(form.getPaymentMethod());

        history.setPaidAt(LocalDateTime.now());

        history.setNote(form.getNote());

        paymentHistoryRepository.save(history);

        studentTuition.setStatus(enums.TuitionStatus.PAID);

        studentTuition.setPaidAt(LocalDateTime.now());

        studentTuitionRepository.save(studentTuition);

    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentHistory> getPaymentHistory(Long studentTuitionId) {

        StudentTuition studentTuition = studentTuitionRepository.findById(studentTuitionId).orElseThrow(() -> new EntityNotFoundException("Không tìm thấy học phí."));

        return paymentHistoryRepository.findByStudentTuitionOrderByPaidAtDesc(studentTuition);

    }

    @Override
    @Transactional(readOnly = true)
    public StudentTuition getStudentTuition(Long id) {
        return studentTuitionRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Không tìm thấy học phí của học viên."));
    }
}

package com.englishcentermanager.service;

import com.englishcentermanager.entity.ClassSchedule;
import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.Room;
import com.englishcentermanager.repository.ClassScheduleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ClassScheduleServiceImpl implements ClassScheduleService {
    private final ClassScheduleRepository classScheduleRepository;

    public ClassScheduleServiceImpl(ClassScheduleRepository classScheduleRepository) {
        this.classScheduleRepository = classScheduleRepository;
    }

    @Override
    public List<ClassSchedule> findAll() {
        return classScheduleRepository.findAll();
    }

    @Override
    public Optional<ClassSchedule> findById(Long id) {
        return classScheduleRepository.findById(id);
    }

    @Override
    public ClassSchedule save(ClassSchedule classSchedule) {
        return classScheduleRepository.save(classSchedule);
    }

    @Override
    public ClassSchedule update(Long id, ClassSchedule classSchedule) {
        ClassSchedule existingSchedule = classScheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay thoi khoa bieu"));

        existingSchedule.setCourseClass(classSchedule.getCourseClass());
        existingSchedule.setRoom(classSchedule.getRoom());
        existingSchedule.setDayOfWeek(classSchedule.getDayOfWeek());
        existingSchedule.setStartTime(classSchedule.getStartTime());
        existingSchedule.setEndTime(classSchedule.getEndTime());
        existingSchedule.setNote(classSchedule.getNote());

        return classScheduleRepository.save(existingSchedule);
    }

    @Override
    public void deleteById(Long id) {
        classScheduleRepository.deleteById(id);
    }

    @Override
    public List<ClassSchedule> findByCourseClass(CourseClass courseClass) {
        return classScheduleRepository.findByCourseClass(courseClass);
    }

    @Override
    public List<ClassSchedule> findByRoom(Room room) {
        return classScheduleRepository.findByRoom(room);
    }

    @Override
    public List<ClassSchedule> findByDayOfWeek(String dayOfWeek) {
        return classScheduleRepository.findByDayOfWeek(dayOfWeek);
    }

    @Override
    public boolean existsByCourseClassAndDayOfWeekAndStartTime(CourseClass courseClass, String dayOfWeek, LocalTime startTime) {
        return classScheduleRepository.existsByCourseClassAndDayOfWeekAndStartTime(courseClass, dayOfWeek, startTime);
    }

    @Override
    public boolean existsByRoomAndDayOfWeekAndStartTime(Room room, String dayOfWeek, LocalTime startTime) {
        return classScheduleRepository.existsByRoomAndDayOfWeekAndStartTime(room, dayOfWeek, startTime);
    }
}

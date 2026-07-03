package com.englishcentermanager.service;

import com.englishcentermanager.entity.ClassSchedule;
import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.Room;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ClassScheduleService {
    List<ClassSchedule> findAll();

    Optional<ClassSchedule> findById(Long id);

    ClassSchedule save(ClassSchedule classSchedule);

    ClassSchedule update(Long id, ClassSchedule classSchedule);

    void deleteById(Long id);

    List<ClassSchedule> findByCourseClass(CourseClass courseClass);

    List<ClassSchedule> findByRoom(Room room);

    List<ClassSchedule> findByDayOfWeek(String dayOfWeek);

    boolean existsByCourseClassAndDayOfWeekAndStartTime(CourseClass courseClass, String dayOfWeek, LocalTime startTime);

    boolean existsByRoomAndDayOfWeekAndStartTime(Room room, String dayOfWeek, LocalTime startTime);
}

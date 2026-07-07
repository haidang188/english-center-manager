package com.englishcentermanager.service;

import com.englishcentermanager.entity.ClassSchedule;
import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.Room;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.entity.enums;

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

    List<ClassSchedule> findByDayOfWeek(enums.DayOfWeek dayOfWeek);

    boolean existsRoomTimeConflict(Room room,
                                   enums.DayOfWeek dayOfWeek,
                                   LocalTime startTime,
                                   LocalTime endTime,
                                   Long ignoredId);

    boolean existsClassTimeConflict(CourseClass courseClass,
                                    enums.DayOfWeek dayOfWeek,
                                    LocalTime startTime,
                                    LocalTime endTime,
                                    Long ignoredId);

    boolean existsTeacherTimeConflict(User teacher,
                                      enums.DayOfWeek dayOfWeek,
                                      LocalTime startTime,
                                      LocalTime endTime,
                                      Long ignoredId);
}

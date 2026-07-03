package com.englishcentermanager.repository;

import com.englishcentermanager.entity.ClassSchedule;
import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;

public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, Long> {
    List<ClassSchedule> findByCourseClass(CourseClass courseClass);

    List<ClassSchedule> findByRoom(Room room);

    List<ClassSchedule> findByDayOfWeek(String dayOfWeek);

    boolean existsByCourseClassAndDayOfWeekAndStartTime(CourseClass courseClass, String dayOfWeek, LocalTime startTime);

    boolean existsByRoomAndDayOfWeekAndStartTime(Room room, String dayOfWeek, LocalTime startTime);
}

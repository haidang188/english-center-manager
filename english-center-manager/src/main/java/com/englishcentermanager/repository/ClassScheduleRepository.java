package com.englishcentermanager.repository;

import com.englishcentermanager.entity.ClassSchedule;
import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.Room;
import com.englishcentermanager.entity.User;
import com.englishcentermanager.entity.enums;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, Long> {
    List<ClassSchedule> findByCourseClass(CourseClass courseClass);

    List<ClassSchedule> findByRoom(Room room);

    List<ClassSchedule> findByDayOfWeek(enums.DayOfWeek dayOfWeek);

    @Query("""
            select count(schedule) > 0
            from ClassSchedule schedule
            where schedule.room = :room
              and schedule.dayOfWeek = :dayOfWeek
              and (:ignoredId is null or schedule.id <> :ignoredId)
              and schedule.startTime < :endTime
              and schedule.endTime > :startTime
            """)
    boolean existsRoomTimeConflict(@Param("room") Room room,
                                   @Param("dayOfWeek") enums.DayOfWeek dayOfWeek,
                                   @Param("startTime") LocalTime startTime,
                                   @Param("endTime") LocalTime endTime,
                                   @Param("ignoredId") Long ignoredId);

    @Query("""
            select count(schedule) > 0
            from ClassSchedule schedule
            where schedule.courseClass = :courseClass
              and schedule.dayOfWeek = :dayOfWeek
              and (:ignoredId is null or schedule.id <> :ignoredId)
              and schedule.startTime < :endTime
              and schedule.endTime > :startTime
            """)
    boolean existsClassTimeConflict(@Param("courseClass") CourseClass courseClass,
                                    @Param("dayOfWeek") enums.DayOfWeek dayOfWeek,
                                    @Param("startTime") LocalTime startTime,
                                    @Param("endTime") LocalTime endTime,
                                    @Param("ignoredId") Long ignoredId);

    @Query("""
            select count(schedule) > 0
            from ClassSchedule schedule
            where schedule.courseClass.teacher = :teacher
              and schedule.dayOfWeek = :dayOfWeek
              and (:ignoredId is null or schedule.id <> :ignoredId)
              and schedule.startTime < :endTime
              and schedule.endTime > :startTime
            """)
    boolean existsTeacherTimeConflict(@Param("teacher") User teacher,
                                      @Param("dayOfWeek") enums.DayOfWeek dayOfWeek,
                                      @Param("startTime") LocalTime startTime,
                                      @Param("endTime") LocalTime endTime,
                                      @Param("ignoredId") Long ignoredId);
}

package com.englishcentermanager.controller;

import com.englishcentermanager.dto.ClassScheduleForm;
import com.englishcentermanager.entity.ClassSchedule;
import com.englishcentermanager.entity.CourseClass;
import com.englishcentermanager.entity.Room;
import com.englishcentermanager.entity.enums;
import com.englishcentermanager.service.ClassScheduleService;
import com.englishcentermanager.service.CourseClassService;
import com.englishcentermanager.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/schedules")
public class AdminScheduleController {
    private final ClassScheduleService classScheduleService;
    private final CourseClassService courseClassService;
    private final RoomService roomService;

    public AdminScheduleController(ClassScheduleService classScheduleService,
                                   CourseClassService courseClassService,
                                   RoomService roomService) {
        this.classScheduleService = classScheduleService;
        this.courseClassService = courseClassService;
        this.roomService = roomService;
    }

    @GetMapping
    public String listSchedules(@RequestParam(required = false) Long classId,
                                @RequestParam(required = false) Long roomId,
                                @RequestParam(required = false) enums.DayOfWeek dayOfWeek,
                                Model model) {
        List<ClassSchedule> schedules = classScheduleService.findAll();

        if (classId != null) {
            schedules = schedules.stream()
                    .filter(schedule -> schedule.getCourseClass().getId().equals(classId))
                    .toList();
        }

        if (roomId != null) {
            schedules = schedules.stream()
                    .filter(schedule -> schedule.getRoom().getId().equals(roomId))
                    .toList();
        }

        if (dayOfWeek != null) {
            schedules = schedules.stream()
                    .filter(schedule -> schedule.getDayOfWeek() == dayOfWeek)
                    .toList();
        }

        schedules = schedules.stream()
                .sorted(Comparator.comparing(ClassSchedule::getDayOfWeek)
                        .thenComparing(ClassSchedule::getStartTime))
                .toList();

        model.addAttribute("schedules", schedules);
        model.addAttribute("classes", courseClassService.findAll());
        model.addAttribute("rooms", roomService.findAllActive());
        model.addAttribute("days", Arrays.asList(enums.DayOfWeek.values()));
        model.addAttribute("dayLabels", buildDayLabels());
        model.addAttribute("scheduleRows", buildScheduleRows(schedules));
        model.addAttribute("selectedClassId", classId);
        model.addAttribute("selectedRoomId", roomId);
        model.addAttribute("selectedDayOfWeek", dayOfWeek);

        return "admin/schedules/list";
    }

    @GetMapping("/create")
    public String showCreateForm(@RequestParam(required = false) Long classId,
                                 Model model) {
        ClassScheduleForm scheduleForm = new ClassScheduleForm();
        scheduleForm.setClassId(classId);

        model.addAttribute("scheduleForm", scheduleForm);
        model.addAttribute("isEdit", false);
        addFormOptions(model);

        return "admin/schedules/form";
    }

    @PostMapping("/create")
    public String createSchedule(@Valid @ModelAttribute("scheduleForm") ClassScheduleForm scheduleForm,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        validateSchedule(scheduleForm, null, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            addFormOptions(model);
            return "admin/schedules/form";
        }

        classScheduleService.save(toEntity(scheduleForm));
        redirectAttributes.addFlashAttribute("successMessage", "Thêm lịch học thành công.");

        return "redirect:/admin/schedules";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        ClassSchedule schedule = classScheduleService.findById(id).orElse(null);

        if (schedule == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy lịch học.");
            return "redirect:/admin/schedules";
        }

        model.addAttribute("scheduleForm", toForm(schedule));
        model.addAttribute("isEdit", true);
        addFormOptions(model);

        return "admin/schedules/form";
    }

    @PostMapping("/{id}/edit")
    public String updateSchedule(@PathVariable Long id,
                                 @Valid @ModelAttribute("scheduleForm") ClassScheduleForm scheduleForm,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (classScheduleService.findById(id).isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy lịch học.");
            return "redirect:/admin/schedules";
        }

        validateSchedule(scheduleForm, id, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            addFormOptions(model);
            return "admin/schedules/form";
        }

        classScheduleService.update(id, toEntity(scheduleForm));
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật lịch học thành công.");

        return "redirect:/admin/schedules";
    }

    @PostMapping("/{id}/delete")
    public String deleteSchedule(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        classScheduleService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa lịch học thành công.");

        return "redirect:/admin/schedules";
    }

    private void validateSchedule(ClassScheduleForm form, Long ignoredId, BindingResult bindingResult) {
        if (form.getStartTime() != null
                && form.getEndTime() != null
                && !form.getEndTime().isAfter(form.getStartTime())) {
            bindingResult.rejectValue("endTime", "invalid", "Giờ kết thúc phải sau giờ bắt đầu");
            return;
        }

        if (form.getClassId() == null
                || form.getRoomId() == null
                || form.getDayOfWeek() == null
                || form.getStartTime() == null
                || form.getEndTime() == null) {
            return;
        }

        CourseClass courseClass = courseClassService.findById(form.getClassId()).orElse(null);
        Room room = roomService.findById(form.getRoomId()).orElse(null);

        if (courseClass == null || room == null) {
            return;
        }

        if (classScheduleService.existsRoomTimeConflict(room, form.getDayOfWeek(), form.getStartTime(), form.getEndTime(), ignoredId)) {
            bindingResult.rejectValue("roomId", "conflict", "Phòng học đã có lịch trong khoảng thời gian này");
        }

        if (classScheduleService.existsClassTimeConflict(courseClass, form.getDayOfWeek(), form.getStartTime(), form.getEndTime(), ignoredId)) {
            bindingResult.rejectValue("classId", "conflict", "Lớp học đã có lịch trong khoảng thời gian này");
        }

        if (classScheduleService.existsTeacherTimeConflict(courseClass.getTeacher(), form.getDayOfWeek(), form.getStartTime(), form.getEndTime(), ignoredId)) {
            bindingResult.rejectValue("classId", "conflict", "Giáo viên của lớp đã có lịch trong khoảng thời gian này");
        }
    }

    private void addFormOptions(Model model) {
        model.addAttribute("classes", courseClassService.findAll());
        model.addAttribute("rooms", roomService.findAllActive());
        model.addAttribute("days", enums.DayOfWeek.values());
        model.addAttribute("dayLabels", buildDayLabels());
    }

    private ClassSchedule toEntity(ClassScheduleForm form) {
        CourseClass courseClass = courseClassService.findById(form.getClassId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học"));
        Room room = roomService.findById(form.getRoomId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng học"));

        ClassSchedule schedule = new ClassSchedule();
        schedule.setCourseClass(courseClass);
        schedule.setRoom(room);
        schedule.setDayOfWeek(form.getDayOfWeek());
        schedule.setStartTime(form.getStartTime());
        schedule.setEndTime(form.getEndTime());
        schedule.setNote(form.getNote());

        return schedule;
    }

    private ClassScheduleForm toForm(ClassSchedule schedule) {
        ClassScheduleForm form = new ClassScheduleForm();
        form.setId(schedule.getId());
        form.setClassId(schedule.getCourseClass().getId());
        form.setRoomId(schedule.getRoom().getId());
        form.setDayOfWeek(schedule.getDayOfWeek());
        form.setStartTime(schedule.getStartTime());
        form.setEndTime(schedule.getEndTime());
        form.setNote(schedule.getNote());

        return form;
    }

    private List<ScheduleBoardRow> buildScheduleRows(List<ClassSchedule> schedules) {
        return schedules.stream()
                .map(this::buildTimeLabel)
                .distinct()
                .sorted()
                .map(timeLabel -> new ScheduleBoardRow(timeLabel, buildSchedulesByDay(schedules, timeLabel)))
                .toList();
    }

    private Map<enums.DayOfWeek, List<ClassSchedule>> buildSchedulesByDay(List<ClassSchedule> schedules, String timeLabel) {
        Map<enums.DayOfWeek, List<ClassSchedule>> schedulesByDay = new LinkedHashMap<>();

        for (enums.DayOfWeek day : enums.DayOfWeek.values()) {
            List<ClassSchedule> schedulesInCell = schedules.stream()
                    .filter(schedule -> schedule.getDayOfWeek() == day)
                    .filter(schedule -> buildTimeLabel(schedule).equals(timeLabel))
                    .sorted(Comparator.comparing(schedule -> schedule.getRoom().getRoomCode()))
                    .toList();

            schedulesByDay.put(day, schedulesInCell);
        }

        return schedulesByDay;
    }

    private String buildTimeLabel(ClassSchedule schedule) {
        return schedule.getStartTime() + " - " + schedule.getEndTime();
    }

    private Map<enums.DayOfWeek, String> buildDayLabels() {
        Map<enums.DayOfWeek, String> dayLabels = new LinkedHashMap<>();
        dayLabels.put(enums.DayOfWeek.MONDAY, "Thứ 2");
        dayLabels.put(enums.DayOfWeek.TUESDAY, "Thứ 3");
        dayLabels.put(enums.DayOfWeek.WEDNESDAY, "Thứ 4");
        dayLabels.put(enums.DayOfWeek.THURSDAY, "Thứ 5");
        dayLabels.put(enums.DayOfWeek.FRIDAY, "Thứ 6");
        dayLabels.put(enums.DayOfWeek.SATURDAY, "Thứ 7");
        dayLabels.put(enums.DayOfWeek.SUNDAY, "Chủ nhật");
        return dayLabels;
    }

    public static class ScheduleBoardRow {
        private final String timeLabel;
        private final Map<enums.DayOfWeek, List<ClassSchedule>> schedulesByDay;

        public ScheduleBoardRow(String timeLabel, Map<enums.DayOfWeek, List<ClassSchedule>> schedulesByDay) {
            this.timeLabel = timeLabel;
            this.schedulesByDay = schedulesByDay;
        }

        public String getTimeLabel() {
            return timeLabel;
        }

        public Map<enums.DayOfWeek, List<ClassSchedule>> getSchedulesByDay() {
            return schedulesByDay;
        }
    }
}

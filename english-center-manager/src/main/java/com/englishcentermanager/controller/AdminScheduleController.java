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
import java.util.Comparator;
import java.util.List;

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
        model.addAttribute("days", enums.DayOfWeek.values());
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
        redirectAttributes.addFlashAttribute("successMessage", "Them lich hoc thanh cong.");

        return "redirect:/admin/schedules";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        ClassSchedule schedule = classScheduleService.findById(id).orElse(null);

        if (schedule == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khong tim thay lich hoc.");
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
            redirectAttributes.addFlashAttribute("errorMessage", "Khong tim thay lich hoc.");
            return "redirect:/admin/schedules";
        }

        validateSchedule(scheduleForm, id, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            addFormOptions(model);
            return "admin/schedules/form";
        }

        classScheduleService.update(id, toEntity(scheduleForm));
        redirectAttributes.addFlashAttribute("successMessage", "Cap nhat lich hoc thanh cong.");

        return "redirect:/admin/schedules";
    }

    @PostMapping("/{id}/delete")
    public String deleteSchedule(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        classScheduleService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xoa lich hoc thanh cong.");

        return "redirect:/admin/schedules";
    }

    private void validateSchedule(ClassScheduleForm form, Long ignoredId, BindingResult bindingResult) {
        if (form.getStartTime() != null
                && form.getEndTime() != null
                && !form.getEndTime().isAfter(form.getStartTime())) {
            bindingResult.rejectValue("endTime", "invalid", "Gio ket thuc phai sau gio bat dau");
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
            bindingResult.rejectValue("roomId", "conflict", "Phong hoc da co lich trong khoang thoi gian nay");
        }

        if (classScheduleService.existsClassTimeConflict(courseClass, form.getDayOfWeek(), form.getStartTime(), form.getEndTime(), ignoredId)) {
            bindingResult.rejectValue("classId", "conflict", "Lop hoc da co lich trong khoang thoi gian nay");
        }

        if (classScheduleService.existsTeacherTimeConflict(courseClass.getTeacher(), form.getDayOfWeek(), form.getStartTime(), form.getEndTime(), ignoredId)) {
            bindingResult.rejectValue("classId", "conflict", "Giao vien cua lop da co lich trong khoang thoi gian nay");
        }
    }

    private void addFormOptions(Model model) {
        model.addAttribute("classes", courseClassService.findAll());
        model.addAttribute("rooms", roomService.findAllActive());
        model.addAttribute("days", enums.DayOfWeek.values());
    }

    private ClassSchedule toEntity(ClassScheduleForm form) {
        CourseClass courseClass = courseClassService.findById(form.getClassId())
                .orElseThrow(() -> new RuntimeException("Khong tim thay lop hoc"));
        Room room = roomService.findById(form.getRoomId())
                .orElseThrow(() -> new RuntimeException("Khong tim thay phong hoc"));

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
}

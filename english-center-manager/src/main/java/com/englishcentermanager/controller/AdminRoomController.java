package com.englishcentermanager.controller;

import com.englishcentermanager.dto.RoomForm;
import com.englishcentermanager.entity.Room;
import com.englishcentermanager.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/admin/rooms")
public class AdminRoomController {
    private static final int PAGE_SIZE = 8;

    private final RoomService roomService;

    public AdminRoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public String listRooms(@RequestParam(required = false) String keyword,
                            @RequestParam(required = false) Boolean active,
                            @RequestParam(defaultValue = "0") int page,
                            Model model) {
        List<Room> rooms = roomService.searchByKeyword(keyword);

        if (active != null) {
            rooms = rooms.stream()
                    .filter(room -> Boolean.TRUE.equals(room.getActive()) == active)
                    .toList();
        }

        rooms = rooms.stream()
                .sorted(Comparator.comparing(Room::getId).reversed())
                .toList();

        Page<Room> roomPage = toPage(rooms, page);

        model.addAttribute("roomPage", roomPage);
        model.addAttribute("rooms", roomPage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("active", active);

        return "admin/rooms/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        RoomForm roomForm = new RoomForm();
        roomForm.setActive(true);

        model.addAttribute("roomForm", roomForm);
        model.addAttribute("isEdit", false);

        return "admin/rooms/form";
    }

    @PostMapping("/create")
    public String createRoom(@Valid @ModelAttribute("roomForm") RoomForm roomForm,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (hasText(roomForm.getRoomCode()) && roomService.existsByRoomCode(roomForm.getRoomCode().trim())) {
            bindingResult.rejectValue("roomCode", "duplicate", "Mã phòng đã tồn tại.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "admin/rooms/form";
        }

        roomService.save(toEntity(roomForm));
        redirectAttributes.addFlashAttribute("successMessage", "Thêm phòng học thành công.");

        return "redirect:/admin/rooms";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Room room = roomService.findById(id).orElse(null);

        if (room == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy phòng học.");
            return "redirect:/admin/rooms";
        }

        model.addAttribute("roomForm", toForm(room));
        model.addAttribute("isEdit", true);

        return "admin/rooms/form";
    }

    @PostMapping("/{id}/edit")
    public String updateRoom(@PathVariable Long id,
                             @Valid @ModelAttribute("roomForm") RoomForm roomForm,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        Room existingRoom = roomService.findById(id).orElse(null);

        if (existingRoom == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy phòng học.");
            return "redirect:/admin/rooms";
        }

        if (hasText(roomForm.getRoomCode()) && roomService.existsByRoomCodeAndIdNot(roomForm.getRoomCode().trim(), id)) {
            bindingResult.rejectValue("roomCode", "duplicate", "Mã phòng đã tồn tại.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "admin/rooms/form";
        }

        roomService.update(id, toEntity(roomForm));
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phòng học thành công.");

        return "redirect:/admin/rooms";
    }

    @PostMapping("/{id}/toggle")
    public String toggleRoomStatus(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        Room room = roomService.findById(id).orElse(null);

        if (room == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy phòng học.");
            return "redirect:/admin/rooms";
        }

        if (Boolean.TRUE.equals(room.getActive())) {
            roomService.deactivate(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã ngưng sử dụng phòng học.");
        } else {
            roomService.activate(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã kích hoạt phòng học.");
        }

        return "redirect:/admin/rooms";
    }

    private Page<Room> toPage(List<Room> rooms, int page) {
        int safePage = Math.max(page, 0);
        int start = Math.min(safePage * PAGE_SIZE, rooms.size());
        int end = Math.min(start + PAGE_SIZE, rooms.size());

        return new PageImpl<>(
                rooms.subList(start, end),
                PageRequest.of(safePage, PAGE_SIZE),
                rooms.size()
        );
    }

    private Room toEntity(RoomForm form) {
        Room room = new Room();
        room.setRoomCode(form.getRoomCode().trim());
        room.setRoomName(form.getRoomName().trim());
        room.setCapacity(form.getCapacity());
        room.setActive(form.getActive() != null ? form.getActive() : true);

        return room;
    }

    private RoomForm toForm(Room room) {
        RoomForm form = new RoomForm();
        form.setId(room.getId());
        form.setRoomCode(room.getRoomCode());
        form.setRoomName(room.getRoomName());
        form.setCapacity(room.getCapacity());
        form.setActive(room.getActive());

        return form;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

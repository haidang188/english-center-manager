package com.englishcentermanager.controller;

import com.englishcentermanager.dto.PaymentForm;
import com.englishcentermanager.dto.TuitionBatchForm;
import com.englishcentermanager.entity.StudentTuition;
import com.englishcentermanager.entity.TuitionBatch;
import com.englishcentermanager.entity.enums;
import com.englishcentermanager.service.CourseClassService;
import com.englishcentermanager.service.StaffTuitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffTuitionController {

    private final StaffTuitionService staffTuitionService;
    private final CourseClassService courseClassService;

    @GetMapping("/tuition-batches")
    public String list(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {

        Page<TuitionBatch> page = staffTuitionService.getTuitionBatches(keyword, pageable);

        model.addAttribute("tuitionBatches", page.getContent());

        model.addAttribute("tuitionBatchPage", page);

        model.addAttribute("keyword", keyword);

        return "staff/tuition-batches";

    }

    @GetMapping("/tuition-batches/create")
    public String showCreateForm(Model model) {

        model.addAttribute("tuitionBatchForm", new TuitionBatchForm());

        model.addAttribute("classes", courseClassService.findAll());

        return "staff/create-tuition-batch";

    }

    @PostMapping("/tuition-batches/create")
    public String create(
            @Valid
            @ModelAttribute TuitionBatchForm tuitionBatchForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {

            model.addAttribute("classes", courseClassService.findAll());

            return "staff/create-tuition-batch";

        }

        staffTuitionService.createBatch(tuitionBatchForm);

        redirectAttributes.addFlashAttribute("successMessage", "Tạo đợt học phí thành công.");

        return "redirect:/staff/tuition-batches";

    }

    @GetMapping("/tuition-batches/{id}")
    public String detail(
            @PathVariable Long id,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {

        TuitionBatch batch = staffTuitionService.getBatch(id);

        Page<StudentTuition> page = staffTuitionService.getStudentTuitions(id, keyword, pageable);

        model.addAttribute("batch", batch);

        model.addAttribute("studentTuitions", page.getContent());

        model.addAttribute("studentTuitionPage", page);

        model.addAttribute("keyword", keyword);

        return "staff/tuition-detail";

    }

    @GetMapping("/student-tuitions/{id}/payment")
    public String paymentForm(
            @PathVariable Long id,
            Model model) {

        model.addAttribute("paymentForm", new PaymentForm());

        StudentTuition studentTuition = staffTuitionService.getStudentTuition(id);

        model.addAttribute("studentTuition", studentTuition);
        model.addAttribute("batch", studentTuition.getTuitionBatch());

        model.addAttribute("paymentMethods", enums.PaymentMethod.values());

        return "staff/payment";

    }

    @PostMapping("/student-tuitions/{id}/payment")
    public String payment(
            @PathVariable Long id,
            @Valid
            @ModelAttribute PaymentForm paymentForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        StudentTuition studentTuition = staffTuitionService.getStudentTuition(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("studentTuition", studentTuition);
            model.addAttribute("batch", studentTuition.getTuitionBatch());
            model.addAttribute("paymentMethods", enums.PaymentMethod.values());
            return "staff/payment";

        }

        staffTuitionService.updatePayment(id, paymentForm);

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thanh toán thành công.");

        return "redirect:/staff/tuition-batches/" + studentTuition.getTuitionBatch().getId();

    }

    @GetMapping("/student-tuitions/{id}/history")
    public String history(
            @PathVariable Long id,
            Model model) {

        StudentTuition studentTuition = staffTuitionService.getStudentTuition(id);

        model.addAttribute("studentTuition", studentTuition);

        model.addAttribute("histories", staffTuitionService.getPaymentHistory(id));

        return "staff/payment-history";

    }
}

package com.englishcentermanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/score-components")
public class AdminScoreComponentController {
    @GetMapping
    public String redirectToCourseList(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Hay chon mot khoa hoc roi bam nut Diem de cau hinh loai diem."
        );

        return "redirect:/admin/courses";
    }
}

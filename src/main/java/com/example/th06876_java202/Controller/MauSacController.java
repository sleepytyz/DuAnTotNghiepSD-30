package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.MauSac;
import com.example.th06876_java202.Service.MauSacService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/mausac")
public class MauSacController {

    private final MauSacService maSacService;

    public MauSacController(MauSacService maSacService) {
        this.maSacService = maSacService;
    }

    @GetMapping("/index")
    public String index(Model model) {
        List<MauSac> msac = maSacService.findAll();
        model.addAttribute("listms", msac);
        model.addAttribute("mausac", new MauSac());
        return "mausac/index";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("mausac")@Valid MauSac maSac, Errors errors, Model model, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            List<MauSac> msac = maSacService.findAll();
            model.addAttribute("listms", msac);
            return "mausac/index";
        }
        if (maSacService.existbyten(maSac.getTenMauSac())) {
            redirectAttributes.addFlashAttribute("mess", "Màu sắc đã tồn tại");
            return "redirect:/mausac/index";
        }
        maSacService.add(maSac);
        return "redirect:/mausac/index";
    }

}

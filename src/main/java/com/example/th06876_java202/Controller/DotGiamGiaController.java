package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.DotGiamGia;
import com.example.th06876_java202.Service.DotGiamGiaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class DotGiamGiaController {

    private final DotGiamGiaService dotGiamGiaService;

    public DotGiamGiaController(DotGiamGiaService dotGiamGiaService) {
        this.dotGiamGiaService = dotGiamGiaService;
    }

    @GetMapping("/dot-giam-gia")
    public String showList(Model model, @RequestParam(required = false) String keyword) {
        List<DotGiamGia> list = this.dotGiamGiaService.searchByTen(keyword);
        model.addAttribute("list", list);
        model.addAttribute("keyword", keyword);
        return "dotgiamgia/list";
    }

    @GetMapping("/dot-giam-gia/create")
    public String getCreatePage(Model model) {
        model.addAttribute("dotGiamGia", new DotGiamGia());
        return "dotgiamgia/create";
    }

    @PostMapping("/dot-giam-gia/create")
    public String postCreatePage(@Valid @ModelAttribute DotGiamGia dotGiamGia, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "dotgiamgia/create";
        }
        this.dotGiamGiaService.create(dotGiamGia);
        return "redirect:/dot-giam-gia";
    }

    @GetMapping("/dot-giam-gia/update/{id}")
    public String getUpdatePage(Model model, @PathVariable int id) {
        DotGiamGia dotGiamGia = this.dotGiamGiaService.findById(id);
        model.addAttribute("dotGiamGia", dotGiamGia);
        model.addAttribute("id", id);
        return "dotgiamgia/update";
    }

    @PostMapping("/dot-giam-gia/update")
    public String postUpdatePage(@Valid @ModelAttribute DotGiamGia dotGiamGia, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("dotGiamGia", dotGiamGia);
            model.addAttribute("id", dotGiamGia.getMaGiamGia());
            return "dotgiamgia/update";
        }
        this.dotGiamGiaService.update(dotGiamGia);
        return "redirect:/dot-giam-gia";
    }

    @PostMapping("/dot-giam-gia/delete/{id}")
    public String postDelete(@PathVariable int id) {
        this.dotGiamGiaService.deleteById(id);
        return "redirect:/dot-giam-gia";
    }
}

package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.DotGiamGia;
import com.example.th06876_java202.Repository.DotGiamGiaRepo;
import com.example.th06876_java202.Service.DotGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class DotGiamGiaCon {
    @Autowired
    private DotGiamGiaService dotGiamGiaService;

    @GetMapping("/dot-giam-gia/hien-thi")
    public String hienThi(Model model) {
        model.addAttribute("dgg", new DotGiamGia());
        model.addAttribute("listDGG", dotGiamGiaService.getAll());
        return "index";
    }

    @GetMapping("/dot-giam-gia/detail/{id}")
    public String dichVu(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("dgg", dotGiamGiaService.getById(id));
        model.addAttribute("listDGG", dotGiamGiaService.getAll());
        return "index";
    }

    @GetMapping("/dot-giam-gia/delete/{id}")
    public String delete(@PathVariable("id") Integer id, Model model) {
        dotGiamGiaService.delete(id);
        return "redirect:/dot-giam-gia/hien-thi";
    }

    @PostMapping("/dot-giam-gia/add")
    public String add(@ModelAttribute DotGiamGia dgg) {
        dotGiamGiaService.save(dgg);
        return "redirect:/dot-giam-gia/hien-thi";
    }
    @PostMapping("/dot-giam-gia/update")
    public String update(@ModelAttribute DotGiamGia dgg) {
        dotGiamGiaService.save(dgg);
        return "redirect:/dot-giam-gia/hien-thi";
    }
}

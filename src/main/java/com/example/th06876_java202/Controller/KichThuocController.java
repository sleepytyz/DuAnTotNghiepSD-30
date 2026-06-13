package com.example.th06876_java202.Controller;


import com.example.th06876_java202.Entity.KichThuoc;
import com.example.th06876_java202.Service.KichThuocService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/kichthuoc")
public class KichThuocController {

    private final KichThuocService kichThuocService;

    public KichThuocController(  KichThuocService kichThuocService ) {
        this.kichThuocService = kichThuocService;
    }

    @GetMapping("/index")
    public String index(Model model) {
        List<KichThuoc> listkt = kichThuocService.getAllKichThuoc();
        model.addAttribute("listk", listkt);
        model.addAttribute("kichthuoc", new KichThuoc());
        return "kichthuoc/index";
    }

//    @GetMapping("/edit/{id}")
//    public String edit(@PathVariable("makt") int id, Model model) {
//        KichThuoc listkt = kichThuocService.getKichThuocById(id).orElse(null);
//        model.addAttribute("listk", listkt);
//        return "kichthuoc/index";
//    }

    @PostMapping("/add")
    public String add(@ModelAttribute("kichthuoc")@Valid KichThuoc kichThuoc, Errors errors, RedirectAttributes redirectAttributes, Model model) {
        if (errors.hasErrors()) {
            List<KichThuoc> listkt = kichThuocService.getAllKichThuoc();
            model.addAttribute("listk", listkt);
            return "kichthuoc/index";
        }
        if (kichThuocService.existsKichThuocByTenKichThuoc(kichThuoc.getTenKichThuoc())) {
            redirectAttributes.addFlashAttribute("mess", "Kích thước này đã tồn tại");
            return "kichthuoc/index";
        }
        kichThuocService.add(kichThuoc);
        return "redirect:/kichthuoc/index";
    }

}

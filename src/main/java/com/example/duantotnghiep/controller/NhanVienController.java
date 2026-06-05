package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.model.NhanVien;
import com.example.duantotnghiep.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/nhan-vien")
public class NhanVienController {

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @GetMapping
    public String index(Model model) {
        List<NhanVien> dsNhanVien = nhanVienRepository.findAll();
        model.addAttribute("list", dsNhanVien);

        if (!model.containsAttribute("nhanVien")) {
            model.addAttribute("nhanVien", new NhanVien());
        }
        return "nhanvien/index";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("nhanVien") NhanVien nv) {
        nhanVienRepository.save(nv);
        return "redirect:/nhan-vien";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, Model model) {
        NhanVien nv = nhanVienRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mã nhân viên: " + id));

        model.addAttribute("nhanVien", nv);
        model.addAttribute("list", nhanVienRepository.findAll());
        return "nhanvien/index";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id) {
        nhanVienRepository.deleteById(id);
        return "redirect:/nhan-vien";
    }
}
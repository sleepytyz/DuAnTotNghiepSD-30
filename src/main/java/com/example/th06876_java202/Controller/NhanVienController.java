package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.NhanVien;
import com.example.th06876_java202.Repository.NhanVienRepository;
import com.example.th06876_java202.Service.NhanVienService;
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

    @Autowired
    private NhanVienService nhanVienService;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean status,
            Model model) {

        List<NhanVien> dsNhanVien =
                nhanVienService.search(keyword, role, status);

        model.addAttribute("list", dsNhanVien);
        model.addAttribute("activeMenu", "nhanvien");
        model.addAttribute("nhanVien", new NhanVien());

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
package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.NhanVien;
import com.example.th06876_java202.Entity.TaiKhoan; // BẮT BUỘC PHẢI CÓ DÒNG NÀY
import com.example.th06876_java202.Repository.NhanVienRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/nhan-vien")
public class NhanVienController {

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @GetMapping
    public String index(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "status", required = false) String status,
            Model model) {

        List<NhanVien> dsNhanVien = nhanVienRepository.filter(keyword, role, status);

        model.addAttribute("list", dsNhanVien);
        model.addAttribute("activeMenu", "nhanvien");

        // TRUYỀN CÁC GIÁ TRỊ LỌC VÀO MODEL ĐỂ HIỂN THỊ TRÊN GIAO DIỆN
        model.addAttribute("keyword", keyword);
        model.addAttribute("role", role);
        model.addAttribute("status", status);

        if (!model.containsAttribute("nhanVien")) {
            NhanVien nv = new NhanVien();
            nv.setTaiKhoan(new TaiKhoan());
            model.addAttribute("nhanVien", nv);
        }

        return "nhanvien/index";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("nhanVien") NhanVien nv,
                       BindingResult result,
                       Model model) {

        String defaultBcrypt = "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG";

        // Validate lỗi
        if (result.hasErrors()) {

            if (nv.getTaiKhoan() == null) {
                nv.setTaiKhoan(new TaiKhoan());
            }

            model.addAttribute("list", nhanVienRepository.findAll());
            model.addAttribute("showModal", true);
            model.addAttribute("nhanVien", nv);

            return "nhanvien/index";
        }

        if (nv.getMaNhanVien() != null) {

            NhanVien existingNv = nhanVienRepository.findById(nv.getMaNhanVien())
                    .orElse(null);

            if (existingNv != null) {

                existingNv.setHoTen(nv.getHoTen());
                existingNv.setSoDienThoai(nv.getSoDienThoai());
                existingNv.setEmail(nv.getEmail());
                existingNv.setNgaySinh(nv.getNgaySinh());
                existingNv.setDiaChi(nv.getDiaChi());
                existingNv.setGioiTinh(nv.getGioiTinh());
                existingNv.setChucVu(nv.getChucVu());
                existingNv.setLuongCoBan(nv.getLuongCoBan());
                existingNv.setNgayVaoLam(nv.getNgayVaoLam());
                existingNv.setTrangThai(nv.getTrangThai());
                existingNv.setGhiChu(nv.getGhiChu());

                // Tài khoản
                if (nv.getTaiKhoan() != null
                        && nv.getTaiKhoan().getTenDangNhap() != null
                        && !nv.getTaiKhoan().getTenDangNhap().trim().isEmpty()) {

                    if (existingNv.getTaiKhoan() != null) {

                        existingNv.getTaiKhoan()
                                .setTenDangNhap(nv.getTaiKhoan().getTenDangNhap());

                    } else {

                        TaiKhoan tk = new TaiKhoan();
                        tk.setTenDangNhap(nv.getTaiKhoan().getTenDangNhap());
                        tk.setMatKhau(defaultBcrypt);
                        tk.setVaiTro("STAFF");
                        tk.setTrangThai(true);

                        existingNv.setTaiKhoan(tk);
                    }
                }

                nhanVienRepository.save(existingNv);
            }

        } else {


            if (nv.getNgayVaoLam() == null) {
                nv.setNgayVaoLam(LocalDate.now());
            }

            if (nv.getTaiKhoan() != null
                    && nv.getTaiKhoan().getTenDangNhap() != null
                    && !nv.getTaiKhoan().getTenDangNhap().trim().isEmpty()) {

                nv.getTaiKhoan().setMatKhau(defaultBcrypt);
                nv.getTaiKhoan().setVaiTro("STAFF");
                nv.getTaiKhoan().setTrangThai(true);

            } else {
                nv.setTaiKhoan(null);
            }

            nhanVienRepository.save(nv);
        }

        return "redirect:/nhan-vien";
    }
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, Model model) {
        NhanVien nv = nhanVienRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mã nhân viên: " + id));

        if (nv.getTaiKhoan() == null) {
            nv.setTaiKhoan(new TaiKhoan());
        }
        model.addAttribute("nhanVien", nv);
        model.addAttribute("list", nhanVienRepository.findAll());
        return "nhanvien/index";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id) {
        nhanVienRepository.updateTrangThai(id);
        return "redirect:/nhan-vien";
    }
}
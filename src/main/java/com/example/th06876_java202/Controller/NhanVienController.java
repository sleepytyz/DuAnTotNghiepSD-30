package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.NhanVien;
import com.example.th06876_java202.Entity.TaiKhoan; // BẮT BUỘC PHẢI CÓ DÒNG NÀY
import com.example.th06876_java202.Repository.NhanVienRepository;
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
        model.addAttribute("activeMenu", "nhanvien");

        // Khởi tạo thực thể lồng để tránh lỗi Null ở giao diện Form Thymeleaf
        if (!model.containsAttribute("nhanVien")) {
            NhanVien nv = new NhanVien();
            nv.setTaiKhoan(new TaiKhoan());
            model.addAttribute("nhanVien", nv);
        }
        return "nhanvien/index";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("nhanVien") NhanVien nv) {
        String defaultBcrypt = "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG";

        if (nv.getMaNhanVien() != null) {
            // XỬ LÝ CẬP NHẬT (SỬA)
            NhanVien existingNv = nhanVienRepository.findById(nv.getMaNhanVien()).orElse(null);
            if (existingNv != null) {
                existingNv.setHoTen(nv.getHoTen());
                existingNv.setSoDienThoai(nv.getSoDienThoai());
                existingNv.setEmail(nv.getEmail());
                existingNv.setDiaChi(nv.getDiaChi());
                existingNv.setNgaySinh(nv.getNgaySinh());
                existingNv.setGioiTinh(nv.getGioiTinh());
                existingNv.setChucVu(nv.getChucVu());
                existingNv.setLuongCoBan(nv.getLuongCoBan());
                existingNv.setNgayVaoLam(nv.getNgayVaoLam());
                existingNv.setTrangThai(nv.getTrangThai());
                existingNv.setGhiChu(nv.getGhiChu());

                if (nv.getTaiKhoan() != null && nv.getTaiKhoan().getTenDangNhap() != null && !nv.getTaiKhoan().getTenDangNhap().trim().isEmpty()) {
                    if (existingNv.getTaiKhoan() != null) {
                        existingNv.getTaiKhoan().setTenDangNhap(nv.getTaiKhoan().getTenDangNhap());
                    } else {
                        TaiKhoan tkNew = nv.getTaiKhoan();
                        tkNew.setMatKhau(defaultBcrypt);
                        tkNew.setVaiTro("STAFF");
                        tkNew.setTrangThai(true);
                        existingNv.setTaiKhoan(tkNew);
                    }
                }
                nhanVienRepository.save(existingNv);
            }
        } else {
            // XỬ LÝ THÊM MỚI
            if (nv.getTaiKhoan() != null && nv.getTaiKhoan().getTenDangNhap() != null && !nv.getTaiKhoan().getTenDangNhap().trim().isEmpty()) {
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
        nhanVienRepository.deleteById(id);
        return "redirect:/nhan-vien";
    }
}
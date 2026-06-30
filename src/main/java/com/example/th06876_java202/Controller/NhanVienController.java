package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.NhanVien;
import com.example.th06876_java202.Entity.TaiKhoan; // BẮT BUỘC PHẢI CÓ DÒNG NÀY
import com.example.th06876_java202.Repository.NhanVienRepository;
import com.example.th06876_java202.Service.EmailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @Autowired
    private EmailService emailService;

    @Autowired
    private org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder;

    @GetMapping
    public String index(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(page, 10, Sort.by("MaNhanVien").descending());

        Page<NhanVien> pageNhanVien = nhanVienRepository.filter(keyword, role, status, pageable);

        model.addAttribute("list", pageNhanVien.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageNhanVien.getTotalPages());

        model.addAttribute("activeMenu", "nhanvien");

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

        if (result.hasErrors()) {
            if (nv.getTaiKhoan() == null) nv.setTaiKhoan(new TaiKhoan());
            model.addAttribute("list", nhanVienRepository.findAll());
            model.addAttribute("showModal", true);
            model.addAttribute("nhanVien", nv);
            return "nhanvien/index";
        }

        boolean isUpdate = (nv.getMaNhanVien() != null);
        boolean emailExists = isUpdate ?
                nhanVienRepository.existsByEmailAndMaNhanVienNot(nv.getEmail(), nv.getMaNhanVien()) :
                nhanVienRepository.existsByEmail(nv.getEmail());

        boolean sdtExists = isUpdate ?
                nhanVienRepository.existsBySoDienThoaiAndMaNhanVienNot(nv.getSoDienThoai(), nv.getMaNhanVien()) :
                nhanVienRepository.existsBySoDienThoai(nv.getSoDienThoai());

        if (emailExists) {
            result.rejectValue("email", "error.nv", "Email này đã tồn tại trong hệ thống!");
        }
        if (sdtExists) {
            result.rejectValue("soDienThoai", "error.nv", "Số điện thoại này đã được sử dụng!");
        }

        if (result.hasErrors()) {
            model.addAttribute("list", nhanVienRepository.findAll());
            model.addAttribute("showModal", true);
            return "nhanvien/index";
        }

        if (isUpdate) {
            NhanVien existingNv = nhanVienRepository.findById(nv.getMaNhanVien()).orElse(null);
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

                if (nv.getTaiKhoan() != null && nv.getTaiKhoan().getTenDangNhap() != null && !nv.getTaiKhoan().getTenDangNhap().trim().isEmpty()) {
                    if (existingNv.getTaiKhoan() == null) {
                        String rawPass = "Pass" + (int)(Math.random() * 900000 + 100000);
                        TaiKhoan tk = new TaiKhoan();
                        tk.setTenDangNhap(nv.getTaiKhoan().getTenDangNhap());
                        tk.setMatKhau(passwordEncoder.encode(rawPass));
                        tk.setVaiTro("STAFF");
                        tk.setTrangThai(true);
                        existingNv.setTaiKhoan(tk);
                        // Gọi gửi mail
                        emailService.sendAccountDetails(nv.getEmail(), nv.getHoTen(), tk.getTenDangNhap(), rawPass);
                    }
                }
                nhanVienRepository.save(existingNv);
            }
        } else {

            if (nv.getNgayVaoLam() == null) nv.setNgayVaoLam(LocalDate.now());
            nv.setTrangThai(true);
            String hoTen = nv.getHoTen().trim();
            String tenChinh = hoTen.substring(hoTen.lastIndexOf(" ") + 1);

            String autoUsername = tenChinh.toLowerCase() + (int)(Math.random() * 900 + 100);

            String rawPassword = "Pass" + (int)(Math.random() * 900000 + 100000);

            TaiKhoan tk = new TaiKhoan();
            tk.setTenDangNhap(autoUsername);
            tk.setMatKhau(passwordEncoder.encode(rawPassword));
            tk.setVaiTro("STAFF");
            tk.setTrangThai(true);
            nv.setTaiKhoan(tk);

            nhanVienRepository.save(nv);

            try {
                emailService.sendAccountDetails(nv.getEmail(), nv.getHoTen(), autoUsername, rawPassword);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "redirect:/nhan-vien?page=0";
    }

    @GetMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable("id") Integer id, @RequestParam(value = "page", defaultValue = "0") int page) {
        NhanVien nv = nhanVienRepository.findById(id).orElse(null);
        if (nv != null) {
            nv.setTrangThai(!nv.getTrangThai());
            nhanVienRepository.save(nv);
        }
        return "redirect:/nhan-vien?page=" + page;
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
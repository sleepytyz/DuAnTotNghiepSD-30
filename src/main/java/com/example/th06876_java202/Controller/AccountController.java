package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.model.Account;
import com.example.duantotnghiep.repository.NhanVienRepository;
import com.example.duantotnghiep.service.AccountService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tai-khoan")
public class AccountController {

    private final AccountService service;
    private final NhanVienRepository nhanVienRepo;

    public AccountController(AccountService service, NhanVienRepository nhanVienRepo) {
        this.service = service;
        this.nhanVienRepo = nhanVienRepo;
    }

    @GetMapping
    public String index(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("list", service.search(keyword));
        model.addAttribute("account", new Account());
        model.addAttribute("listNhanVien", nhanVienRepo.findAll());
        return "account/index";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("account") Account account, RedirectAttributes redirectAttributes) {
        try {
            account.setTrangThai(true);
            service.save(account);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm tài khoản mới thành công!");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Nhân viên được chọn đã có tài khoản hoặc không hợp lệ!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Hệ thống gặp sự cố khi lưu dữ liệu!");
        }
        return "redirect:/tai-khoan";
    }

    @GetMapping("/lock/{id}")
    public String lock(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        service.lock(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã khóa tài khoản thành công!");
        return "redirect:/tai-khoan";
    }

    @GetMapping("/unlock/{id}")
    public String unlock(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        service.unlock(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã mở khóa tài khoản thành công!");
        return "redirect:/tai-khoan";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            service.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa tài khoản đã phát sinh dữ liệu hóa đơn/lịch sử!");
        }
        return "redirect:/tai-khoan";
    }
}
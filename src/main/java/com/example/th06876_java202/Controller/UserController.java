package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.TaiKhoan;
import com.example.th06876_java202.Service.TaiKhoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final TaiKhoanService taiKhoanService;

    @GetMapping("/")
    public String index(Authentication authentication) {
        if(authentication != null && authentication.isAuthenticated()){
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isStaff = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));

            if (isAdmin) {
                return "account/admin/home";
            } else if (isStaff) {
                return "account/staff/home";
            }
        }
        return "account/user/home";
    }

    @GetMapping("/login")
    public String login() {
        return "account/user/login";
    }

    @GetMapping("/register")
    public String resigter(Model model) {
        model.addAttribute("taiKhoan", new TaiKhoan());
        return "account/user/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute TaiKhoan taiKhoan,
                           @RequestParam String xnmatKhau,
                           Model model) {
        if (!taiKhoan.getMatKhau().equals(xnmatKhau)){
            model.addAttribute("error", "Mật khẩu không khớp");
            return "account/user/register";
        }
        if (taiKhoanService.isTenDangNhapExist(taiKhoan.getTenDangNhap())) {
            model.addAttribute("error", "Tên đăng nhập đã tồn tại");
            return "account/user/register";
        }
        taiKhoanService.createUser(taiKhoan);
        return "redirect:/login?registered";
    }

    @GetMapping("/accessDenied")
    public String deny() {
        return "account/user/deny";
    }
}

package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.Account;
import com.example.th06876_java202.Entity.NhanVien;
import com.example.th06876_java202.Repository.NhanVienRepository;
import com.example.th06876_java202.Service.AccountServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    private final AccountServiceImpl accountService;
    private final NhanVienRepository nhanVienRepo;

    public LoginController(AccountServiceImpl accountService,
                           NhanVienRepository nhanVienRepo) {
        this.accountService = accountService;
        this.nhanVienRepo = nhanVienRepo;
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        @RequestParam(required = false) String remember,
                        HttpSession session,
                        HttpServletResponse response,
                        Model model) {

        Account acc = accountService.findByTenDangNhap(username);

        if (acc == null || !acc.getMatKhau().equals(password)) {
            model.addAttribute("error", "Sai tài khoản hoặc mật khẩu");
            return "account/login";
        }

        session.setAttribute("user", acc);

        if (remember != null) {
            Cookie cUser = new Cookie("username", username);
            Cookie cPass = new Cookie("password", password);

            cUser.setMaxAge(7 * 24 * 60 * 60); // 7 ngày
            cPass.setMaxAge(7 * 24 * 60 * 60);

            cUser.setPath("/");
            cPass.setPath("/");

            response.addCookie(cUser);
            response.addCookie(cPass);
        }

        return "redirect:/sanpham/index";
    }

    @GetMapping("/login")
    public String showLogin(
            @CookieValue(value = "username", required = false) String username,
            @CookieValue(value = "password", required = false) String password,
            HttpSession session,
            Model model) {

        if (session.getAttribute("user") != null) {
            return "redirect:/sanpham/index";
        }

        if (username != null && password != null) {

            Account acc = accountService.findByTenDangNhap(username);

            if (acc != null && acc.getMatKhau().equals(password)) {

                session.setAttribute("user", acc);

                return "redirect:/sanpham/index";
            }

            model.addAttribute("username", username);
            model.addAttribute("password", password);
        }

        return "account/login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response, HttpSession session) {

        session.invalidate();

        Cookie c1 = new Cookie("username", null);
        Cookie c2 = new Cookie("password", null);

        c1.setMaxAge(0);
        c2.setMaxAge(0);

        c1.setPath("/");
        c2.setPath("/");

        response.addCookie(c1);
        response.addCookie(c2);

        return "redirect:/login";
    }


}
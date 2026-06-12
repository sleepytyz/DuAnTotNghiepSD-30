package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.Account;
import com.example.th06876_java202.Repository.Repo_Account;
import com.example.th06876_java202.Service.EmailService;
import com.example.th06876_java202.Service.OtpService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/forgot")
public class QuenMatKhauController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private Repo_Account accountRepo;

    // hiển thị form
    @GetMapping
    public String form(Model model) {
        model.addAttribute("step", "email");
        return "account/email";
    }

    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam String email,
                          HttpSession session,
                          Model model) {

        Account acc = accountRepo.findByEmail(email);

        if (acc == null) {
            model.addAttribute("error", "Email không tồn tại!");
            return "account/email";
        }

        String otp = otpService.generateOtp();
        emailService.sendOtp(email, otp);

        session.setAttribute("resetEmail", email);
        session.setAttribute("otp", otp);
        session.setAttribute("otpTime", System.currentTimeMillis());

        model.addAttribute("step", "otp"); // 👈 QUAN TRỌNG
        return "account/email";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String otp,
                            HttpSession session,
                            Model model) {

        String sessionOtp = (String) session.getAttribute("otp");
        Long otpTime = (Long) session.getAttribute("otpTime");

        if (sessionOtp == null || otpTime == null) {
            model.addAttribute("error", "OTP không tồn tại");
            model.addAttribute("step", "otp");
            return "account/email";
        }

        if (System.currentTimeMillis() - otpTime > 5 * 60 * 1000) {
            model.addAttribute("error", "OTP đã hết hạn");
            model.addAttribute("step", "otp");
            return "account/email";
        }

        if (!sessionOtp.equals(otp)) {
            model.addAttribute("error", "OTP không đúng");
            model.addAttribute("step", "otp");
            return "account/email";
        }

        model.addAttribute("step", "reset");
        return "account/email";
    }

    // reset password
    @PostMapping("/reset")
    public String reset(@RequestParam String newPassword,
                        HttpSession session) {

        String email = (String) session.getAttribute("resetEmail");

        Account acc = accountRepo.findByEmail(email);
        if (acc != null) {
            acc.setMatKhau(newPassword);
            accountRepo.save(acc);
        }

        session.invalidate();

        return "redirect:/login";
    }
}
package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.GiamGia;
import com.example.th06876_java202.Entity.VoucherEmailDTO;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.thymeleaf.context.Context;
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    @Autowired private org.thymeleaf.TemplateEngine templateEngine;

    @Async
    public void sendOtp(String to, String otp) {

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("OTP đặt lại mật khẩu");
        msg.setText("Mã OTP của bạn là: " + otp + "\nCó hiệu lực trong 5 phút");

        mailSender.send(msg);
    }

    @Async
    public void sendAccountDetails(String to, String hoTen, String username, String password) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
            context.setVariable("hoTen", hoTen);
            context.setVariable("username", username);
            context.setVariable("password", password);

            String htmlContent = templateEngine.process("email/email-template", context);

            helper.setTo(to);
            helper.setSubject("Thông tin tài khoản hệ thống");
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async
    public void sendVoucherEmail(String toEmail, VoucherEmailDTO dto) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariable("voucher", dto);

            String html = templateEngine.process("email/email-voucher", context);

            helper.setTo(toEmail);
            helper.setSubject("Voucher giảm giá mới");
            helper.setText(html, true);

            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("Email lỗi: " + e.getMessage());
        }
    }

    @Async
    public void sendVoucherStopEmail(String toEmail, VoucherEmailDTO dto) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariable("voucher", dto);

            String html = templateEngine.process("email/email-voucher-stop", context);

            helper.setTo(toEmail);
            helper.setSubject("❌ Thông báo ngừng chương trình giảm giá");
            helper.setText(html, true);

            mailSender.send(message);
            System.out.println("Stop voucher email sent to: " + toEmail);

        } catch (Exception e) {
            System.err.println("Email stop voucher lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void sendSimpleEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            System.out.println("Simple email sent to: " + to);
        } catch (Exception e) {
            System.err.println("Failed to send simple email to: " + to);
            e.printStackTrace();
        }
    }

    @Async
    public void sendVoucherActivationEmail(String toEmail, VoucherEmailDTO dto) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariable("voucher", dto);

            String html = templateEngine.process("email/email-voucher-activated", context);

            helper.setTo(toEmail);
            helper.setSubject("🎉 Voucher giảm giá đã được kích hoạt!");
            helper.setText(html, true);

            mailSender.send(message);
            System.out.println("Activation email sent to: " + toEmail);

        } catch (Exception e) {
            System.err.println("Email activation lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
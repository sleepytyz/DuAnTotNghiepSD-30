package com.example.th06876_java202.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtp(String to, String otp) {

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("OTP đặt lại mật khẩu");
        msg.setText("Mã OTP của bạn là: " + otp + "\nCó hiệu lực trong 5 phút");

        mailSender.send(msg);
    }
}
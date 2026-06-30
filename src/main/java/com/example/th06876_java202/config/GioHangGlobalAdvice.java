package com.example.th06876_java202.config;

import com.example.th06876_java202.Storefront.GioHang;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GioHangGlobalAdvice {

    private final GioHang gioHang;

    @ModelAttribute("gioHangSoLuong")
    public int gioHangSoLuong() {
        try {
            return gioHang.tongSoLuong();
        } catch (Exception e) {
            return 0;
        }
    }
}

package com.example.th06876_java202.config;

import com.example.th06876_java202.Entity.TaiKhoan;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.util.Collections;

public class CustomUserDetails extends User {

    // 1. Thêm thuộc tính taiKhoan vào class để lưu trữ thực thể này
    private final TaiKhoan taiKhoan;
    private final String hoTenNhanVien;

    public CustomUserDetails(TaiKhoan taiKhoan) {
        // Thay đổi phần SimpleGrantedAuthority tùy thuộc vào logic xử lý Quyền (Role) thực tế của bạn
        super(taiKhoan.getTenDangNhap(),
                taiKhoan.getMatKhau(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        // 2. Gán giá trị được truyền vào
        this.taiKhoan = taiKhoan;
        this.hoTenNhanVien = (taiKhoan.getNhanVien() != null) ? taiKhoan.getNhanVien().getHoTen() : "Chưa gán NV";
    }

    // 3. THÊM METHOD NÀY ĐỂ HẾT BÁO LỖI ĐỎ Ở CONTROLLER
    public TaiKhoan getTaiKhoan() {
        return this.taiKhoan;
    }

    public String getHoTenNhanVien() {
        return hoTenNhanVien;
    }
}
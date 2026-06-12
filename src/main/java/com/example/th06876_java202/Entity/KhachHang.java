package com.example.th06876_java202.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Table(name = "KhachHang")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class KhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaKhachHang")
    private Integer maKH;

    @Column(name = "HoTen")
    private String hoTen;

    @Column(name = "SoDienThoai")
    private String sdt;

    @Column(name = "Email")
    private String email;

    @Column(name = "DiaChi")
    private String diaChi;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "NgaySinh")
    private LocalDate ngaySinh;

    @Column(name = "GioiTinh")
    private Boolean gioiTinh;

    @Column(name = "DiemTichLuy")
    private Integer diemTichLuy;

    @Column(name = "HangKhachHang")
    private String hangKhachHang;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "NgayDangKy")
    private LocalDate ngayDangKy;

    @Column(name = "GhiChu")
    private String ghiChu;




}

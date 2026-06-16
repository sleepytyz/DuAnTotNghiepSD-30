package com.example.th06876_java202.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "NhanVien")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NhanVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaNhanVien")
    private Integer maNhanVien;

    @Column(name = "HoTen", nullable = false, length = 100)
    private String hoTen;

    @Column(name = "SoDienThoai", unique = true, length = 15)
    private String soDienThoai;

    @Column(name = "Email", unique = true, length = 100)
    private String email;

    @Column(name = "DiaChi", length = 255)
    private String diaChi;

    @Column(name = "NgaySinh")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date ngaySinh;

    @Column(name = "GioiTinh")
    private Boolean gioiTinh;

    @Column(name = "ChucVu", length = 50)
    private String chucVu;

    @Column(name = "LuongCoBan")
    private BigDecimal luongCoBan;

    @Column(name = "NgayVaoLam")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date ngayVaoLam;

    @Column(name = "TrangThai")
    private Boolean trangThai = true;

    @Column(name = "GhiChu", length = 500)
    private String ghiChu;

    @OneToOne
    @JoinColumn(name = "MaTaiKhoan")
    private TaiKhoan taiKhoan;
}
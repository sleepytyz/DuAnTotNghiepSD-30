package com.example.th06876_java202.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "HoaDon")
public class HoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maHoaDon;

    @Column(name = "MaKhachHang")
    private Integer maKhachHang;

    @Column(name = "MaNhanVien")
    private Integer maNhanVien;

    @Column(name = "MaGiamGia")
    private Integer maGiamGia;

    @Column(name = "TongTien")
    private BigDecimal tongTien;

    @Column(name = "TienKhachDua")
    private BigDecimal tienKhachDua;

    @Column(name = "TienThua")
    private BigDecimal tienThua;

    @Column(name = "TienShip")
    private BigDecimal tienShip;

    @Column(name = "PhuongThucThanhToan")
    private String phuongThucThanhToan;

    @Column(name = "TrangThai")
    private String trangThai;

    @Column(name = "GhiChu")
    private String ghiChu;
}

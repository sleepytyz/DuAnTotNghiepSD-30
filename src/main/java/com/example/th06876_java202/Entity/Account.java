package com.example.th06876_java202.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "[TaiKhoan]", schema = "dbo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaTaiKhoan")
    private Integer maTaiKhoan;

    @ManyToOne
    @JoinColumn(name = "MaNhanVien")
    private NhanVien maNhanVien;

    @Column(name = "TenDangNhap")
    private String tenDangNhap;

    @Column(name = "MatKhau")
    private String matKhau;

    @Column(name = "VaiTro")
    private String vaiTro;

    @Column(name = "TrangThai")
    private Boolean trangThai;

    @Column(name = "Email")
    private String email;
}
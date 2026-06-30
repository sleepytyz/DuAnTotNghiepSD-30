package com.example.th06876_java202.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*; // Thêm import này
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    @Pattern(
            regexp = "^[\\p{L} ]+$",
            message = "Họ tên chỉ được chứa chữ cái và khoảng trắng"
    )

    @Column(name = "HoTen")
    private String hoTen;

    @Column(name = "SoDienThoai")
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(
            regexp = "^(0(3|5|7|8|9))[0-9]{8}$",
            message = "Số điện thoại không đúng định dạng Việt Nam (10 số)"
    )
    private String sdt;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    @Column(name = "Email")
    private String email;

    @OneToMany(mappedBy = "khachHang",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<DiaChi> danhSachDiaChi;

    @Past(message = "Ngày sinh phải là một ngày trong quá khứ")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "NgaySinh")
    private LocalDate ngaySinh;

    @Column(name = "GioiTinh")
    private Boolean gioiTinh;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "NgayDangKy")
    private LocalDate ngayDangKy = LocalDate.now();

    @Column(name = "GhiChu")
    private String ghiChu;

    @Column(name = "TrangThai")
    private boolean trangThai = true;

    @OneToOne
    @JoinColumn(name = "MaTaiKhoan", referencedColumnName = "MaTaiKhoan")
    private TaiKhoan taiKhoan;
}
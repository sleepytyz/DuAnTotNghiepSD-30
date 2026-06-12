package com.example.th06876_java202.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table( name = "SanPhamChiTiet")
public class SanPhamChiTiet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaSanPhamChiTiet")
    private Integer maSanPhamChiTiet;

    @ManyToOne
    @JoinColumn (name = "MaSanPham")
    private SanPham sanPham;

    @NotBlank( message = "Không được bỏ trống kích thước")
    @Column(name = "Size")
    private String size;

    @NotBlank( message = "Không được bỏ trống màu sắc")
    @Column(name = "MauSac")
    private String mauSac;

    @NotNull(message = "Giá nhập không được để trống")
    @Column(name = "GiaNhap")
    private BigDecimal giaNhap;

    @NotNull(message = "Giá bán không được để trống")
    @Column(name = "GiaBan")
    private BigDecimal giaBan;

    @NotNull(message = "Số lượng tồn không được để trống")
    @Column(name = "SoLuongTon")
    private Integer soLuongTon;

    @Column(name = "NgayTao")
    private LocalDate ngayTao;

    @Column(name = "NgayCapNhat")
    private LocalDate ngayCapNhat;

    @Column(name = "TrangThai")
    private String trangThai;

    @OneToMany(mappedBy = "sanPhamChiTiet")
    private List<SanPhamHinhAnh> danhSachAnh;
}

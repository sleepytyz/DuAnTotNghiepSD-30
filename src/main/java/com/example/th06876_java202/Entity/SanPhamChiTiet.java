package com.example.th06876_java202.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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
@JsonIgnoreProperties({"sanPham", "kichThuoc", "mauSac"})
public class SanPhamChiTiet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaSanPhamChiTiet")
    private Integer maSanPhamChiTiet;

    @ManyToOne
    @JoinColumn (name = "MaSanPham")
    private SanPham sanPham;

    @ManyToOne
    @JoinColumn(name = "MaKichThuoc")
    private KichThuoc kichThuoc;

    @ManyToOne
    @JoinColumn(name = "MaMauSac")
    private MauSac mauSac;

    @NotNull(message = "Giá nhập không được để trống")
    @DecimalMin(value = "0", message = "Giá nhập phải lớn hơn 0")
    @Column(name = "GiaNhap")
    private BigDecimal giaNhap;

    @NotNull(message = "Giá bán không được để trống")
    @DecimalMin(value = "0", message = "Giá bán phải lớn hơn 0")
    @Column(name = "GiaBan")
    private BigDecimal giaBan;

    @NotNull(message = "Số lượng tồn không được để trống")
    @Min(value = 0, message = "Số lượng không được âm")
    @Column(name = "SoLuongTon")
    private Integer soLuongTon;

    @Column(name = "NgayTao")
    private LocalDate ngayTao;

    @Column(name = "NgayCapNhat")
    private LocalDate ngayCapNhat;

    @Column(name = "TrangThai")
    private String trangThai;

    @Column(name = "DuongDanAnh")
    private String duongDanAnh;

}

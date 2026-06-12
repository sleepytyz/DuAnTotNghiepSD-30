package com.example.th06876_java202.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "SanPham")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaSanPham")
    private Integer maSanPham;

    @ManyToOne
    @JoinColumn(name = "MaDanhMuc")
    private DanhMucSanPham danhMucSanPham;

    @NotBlank(message = "Không bỏ trống tên sản phẩm")
    @Pattern( regexp = "^[\\p{L}\\d\\s]*$", message = "Tên sản phẩm chỉ được chứa chữ cái và khoảng trắng")
    @Column(name = "TenSanPham")
    private String tenSanPham;

    @NotBlank(message = "Không bỏ trống mô tả")
    @Pattern(regexp = "^[\\p{L}\\s]*$", message = "Mô tả sản phẩm chỉ được chứa chữ cái và khoảng trắng")
    @Column(name = "MoTa")
    private String moTa;

    @NotBlank(message = "Không bỏ trống chất liệu")
    @Pattern(regexp = "^[\\p{L}\\s]*$", message = "Chất liệu chỉ được chứa chữ cái và khoảng trắng")
    @Column(name = "ChatLieu")
    private String chatLieu;

    @NotNull(message = "Vui lòng chọn trạng thái")
    @Column(name = "TrangThai")
    private Boolean trangThai;

    @Column(name = "NgayTao")
    private LocalDate ngayTao;

    @Column(name = "NgayCapNhat")
    private LocalDate ngayCapNhat;
}
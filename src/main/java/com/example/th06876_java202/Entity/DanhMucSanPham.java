package com.example.th06876_java202.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table( name = "DanhMucSanPham")
public class DanhMucSanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDanhMuc")
    private int maDanhMuc;

    @NotBlank(message = "Tên danh mục không để trống")
    @Column( name = "TenDanhMuc")
    private String tenDanhMuc;

    @Column(name = "TrangThai")
    private Boolean trangThai;

    @NotBlank(message = "Mô tả danh mục không để trống")
    @Column( name = "MoTa")
    private String moTa;

    @Column(name = "TrangThai")
    private Boolean trangThai;
}

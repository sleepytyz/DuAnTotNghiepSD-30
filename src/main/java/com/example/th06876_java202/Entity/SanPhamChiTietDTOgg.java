package com.example.th06876_java202.Entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SanPhamChiTietDTOgg {

    private Integer maSanPhamChiTiet;
    private String tenSanPham;
    private String tenKichThuoc;
    private String tenMauSac;
    private BigDecimal giaNhap;
    private BigDecimal giaBan;
    private Integer soLuongTon;
    private String trangThai;
    private String duongDanAnh;
}



package com.example.th06876_java202.Entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SanPhamDTO {
    private String maSanPham;
    private String tenSanPham;
    private String moTa;
    private String maDanhMuc;
    private String maThuongHieu;
    private String maKieuGiay;
    private String maChatLieu;

    // ===== THÊM CÁC TRƯỜNG MỚI =====
    private Boolean trangThai;
    private Integer tongTon;
    private String giaBanDisplay;
    private BigDecimal giaMin;
    private BigDecimal giaMax;
    private String tenThuongHieu;
}
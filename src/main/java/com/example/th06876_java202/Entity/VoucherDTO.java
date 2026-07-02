package com.example.th06876_java202.Entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
// Trong VoucherDTO.java - Thêm trường

public class VoucherDTO {
    private String maGiamGia;
    private String tenGiamGia;
    private String loaiGiamGia;
    private BigDecimal giaTriGiam;
    private BigDecimal donToiThieu;
    private BigDecimal giamToiDa;
    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;
    private Integer loaiApDung;
    private Integer trangThaiSuDung;  // 0: chưa dùng, 1: đã dùng
    private Boolean daSuDung;

    // Getters and Setters
}
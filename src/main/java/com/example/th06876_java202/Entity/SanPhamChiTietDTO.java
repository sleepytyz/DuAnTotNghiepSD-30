package com.example.th06876_java202.Entity;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
public class SanPhamChiTietDTO {

    private Integer maMauSac;
    private Integer maKichThuoc;

    private BigDecimal giaNhap;
    private BigDecimal giaBan;

    private Integer soLuongTon;

    private String duongDanAnh;


}
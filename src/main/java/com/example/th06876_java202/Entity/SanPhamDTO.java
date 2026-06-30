package com.example.th06876_java202.Entity;

import com.example.th06876_java202.Entity.SanPham;
import com.example.th06876_java202.Entity.SanPhamChiTietDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SanPhamDTO {
    private String tenSanPham;
    private String moTa;
    private Integer maDanhMuc;
    private Integer maThuongHieu;
    private Integer maKieuGiay;
    private Integer maChatLieu;
}
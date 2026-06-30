package com.example.th06876_java202.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LichNhanVienDTO {
    private Integer maNhanVien;
    private Integer maCa;
    private String ngay; // Ngày cụ thể (format: yyyy-MM-dd)
}

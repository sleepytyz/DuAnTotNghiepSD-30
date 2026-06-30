package com.example.th06876_java202.Storefront;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Thông tin 1 biến thể (màu sắc + kích thước) trên trang chi tiết sản phẩm.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BienTheVM {
    private Integer maSanPhamChiTiet;
    private Integer maMauSac;
    private String tenMauSac;
    private Integer maKichThuoc;
    private String tenKichThuoc;
    private BigDecimal giaGoc;
    private BigDecimal giaSauGiam;
    private Integer phanTramGiam;
    private Integer soLuongTon;
    private String anh;
}

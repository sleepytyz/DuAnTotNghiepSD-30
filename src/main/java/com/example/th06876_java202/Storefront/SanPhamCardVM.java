package com.example.th06876_java202.Storefront;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Thông tin hiển thị 1 sản phẩm trên trang danh sách / trang chủ / liên quan.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamCardVM {
    private String maSanPham;
    private String tenSanPham;
    private String tenThuongHieu;
    private String anh;
    private BigDecimal giaGoc;       // giá thấp nhất trong các biến thể còn hàng
    private BigDecimal giaSauGiam;   // giá sau khi áp khuyến mãi (nếu có), = giaGoc nếu không có KM
    private Integer phanTramGiam;    // % giảm cao nhất hiện có (0 nếu không có khuyến mãi)
    private boolean conHang;
}

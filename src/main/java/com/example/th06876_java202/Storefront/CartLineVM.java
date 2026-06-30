package com.example.th06876_java202.Storefront;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Thông tin 1 dòng giỏ hàng để hiển thị ở trang giỏ hàng / thanh toán.
 * Được build lại mỗi lần hiển thị từ GioHangItem (session) + dữ liệu mới nhất trong CSDL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartLineVM {
    private String maSanPhamChiTiet;
    private String maSanPham;
    private String tenSanPham;
    private String tenMauSac;
    private String tenKichThuoc;
    private String anh;
    private BigDecimal donGia;       // giá đã áp khuyến mãi (đơn giá thực tế tính tiền)
    private BigDecimal donGiaGoc;    // giá gốc trước khuyến mãi (để hiển thị gạch ngang)
    private Integer soLuong;
    private Integer soLuongTon;      // tồn kho hiện tại, dùng để cảnh báo vượt tồn
    private BigDecimal thanhTien;
    private boolean conHopLe;        // false nếu sản phẩm đã ngừng bán / hết hàng hoàn toàn
}

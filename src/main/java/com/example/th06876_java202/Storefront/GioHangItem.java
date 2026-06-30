package com.example.th06876_java202.Storefront;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Một dòng trong giỏ hàng (lưu trong session của khách).
 * Chỉ lưu ID biến thể sản phẩm + số lượng, mọi thông tin giá/tồn kho luôn được
 * lấy mới từ CSDL khi hiển thị để đảm bảo chính xác (giá/khuyến mãi/tồn kho có thể thay đổi).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GioHangItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer maSanPhamChiTiet;
    private Integer soLuong;
}

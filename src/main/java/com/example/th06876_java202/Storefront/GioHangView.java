package com.example.th06876_java202.Storefront;

import com.example.th06876_java202.Entity.GiamGia;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class GioHangView {
    private List<CartLineVM> dongHang = new ArrayList<>();
    private List<String> canhBao = new ArrayList<>();
    private BigDecimal tongTienHang = BigDecimal.ZERO;   // tổng tiền hàng (đã áp KM theo sản phẩm)
    private GiamGia voucherApDung;
    private BigDecimal soTienGiamVoucher = BigDecimal.ZERO;
    private BigDecimal tienShip = BigDecimal.ZERO;
    private BigDecimal tongThanhToan = BigDecimal.ZERO;
    private int tongSoLuong;
}

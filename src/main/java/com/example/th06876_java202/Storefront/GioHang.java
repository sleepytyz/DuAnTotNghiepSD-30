package com.example.th06876_java202.Storefront;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Giỏ hàng của khách, được Spring quản lý theo phạm vi session (mỗi khách truy cập
 * website sẽ có một instance riêng, không cần đăng nhập mới dùng được).
 * Vì ứng dụng đang dùng spring-session-jdbc (lưu session vào CSDL) nên bean này
 * phải implement Serializable và CHỈ lưu các kiểu dữ liệu nguyên thuỷ/đơn giản,
 * không lưu trực tiếp các Entity (tránh lỗi serialize proxy Hibernate).
 */
@Component
@SessionScope
public class GioHang implements Serializable {

    private static final long serialVersionUID = 1L;

    // key = maSanPhamChiTiet, value = dòng giỏ hàng
    private final Map<Integer, GioHangItem> danhSach = new LinkedHashMap<>();

    // Mã voucher (GiamGia) đang được áp dụng cho giỏ hàng này, null nếu không áp dụng
    private Integer maGiamGiaApDung;

    // Ghi chú khách nhập ở bước thanh toán (lưu tạm trước khi đặt hàng thành công)
    private String ghiChu;

    public synchronized void themSanPham(Integer maSPCT, int soLuong) {
        if (maSPCT == null || soLuong <= 0) return;
        GioHangItem item = danhSach.get(maSPCT);
        if (item == null) {
            danhSach.put(maSPCT, new GioHangItem(maSPCT, soLuong));
        } else {
            item.setSoLuong(item.getSoLuong() + soLuong);
        }
    }

    public synchronized void capNhatSoLuong(Integer maSPCT, int soLuong) {
        if (maSPCT == null) return;
        if (soLuong <= 0) {
            danhSach.remove(maSPCT);
            return;
        }
        GioHangItem item = danhSach.get(maSPCT);
        if (item != null) {
            item.setSoLuong(soLuong);
        }
    }

    public synchronized void xoaSanPham(Integer maSPCT) {
        danhSach.remove(maSPCT);
    }

    public synchronized void xoaTatCa() {
        danhSach.clear();
        maGiamGiaApDung = null;
        ghiChu = null;
    }

    public Map<Integer, GioHangItem> getDanhSach() {
        return danhSach;
    }

    public int tongSoLuong() {
        return danhSach.values().stream().mapToInt(GioHangItem::getSoLuong).sum();
    }

    public boolean isEmpty() {
        return danhSach.isEmpty();
    }

    public Integer getMaGiamGiaApDung() {
        return maGiamGiaApDung;
    }

    public void setMaGiamGiaApDung(Integer maGiamGiaApDung) {
        this.maGiamGiaApDung = maGiamGiaApDung;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}

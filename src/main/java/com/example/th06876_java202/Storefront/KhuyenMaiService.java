package com.example.th06876_java202.Storefront;

import com.example.th06876_java202.Entity.SanPham;
import com.example.th06876_java202.Entity.SanPhamChiTiet;
import com.example.th06876_java202.Repository.ChiTietDotGiamGiaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Tính giá khuyến mãi hiện tại (nếu có) cho sản phẩm/biến thể, dựa trên các
 * "Đợt giảm giá" (DotGiamGia/ChiTietDotGiamGia) admin đã thiết lập sẵn và còn hiệu lực theo ngày.
 */
@Service
@RequiredArgsConstructor
public class KhuyenMaiService {

    private final ChiTietDotGiamGiaRepo chiTietDotGiamGiaRepo;

    /** % giảm cao nhất đang áp dụng cho 1 biến thể cụ thể, 0 nếu không có khuyến mãi nào. */
    public int phanTramGiamBienThe(Integer maSanPhamChiTiet) {
        if (maSanPhamChiTiet == null) return 0;
        List<BigDecimal> list = chiTietDotGiamGiaRepo.findActiveDiscountPercentBySanPhamChiTiet(maSanPhamChiTiet, LocalDate.now());
        if (list == null || list.isEmpty()) return 0;
        return list.get(0).setScale(0, RoundingMode.HALF_UP).intValue();
    }

    /** % giảm cao nhất đang áp dụng cho 1 sản phẩm (bất kỳ biến thể nào), 0 nếu không có. */
    public int phanTramGiamSanPham(Integer maSanPham) {
        if (maSanPham == null) return 0;
        List<BigDecimal> list = chiTietDotGiamGiaRepo.findActiveDiscountPercentBySanPham(maSanPham, LocalDate.now());
        if (list == null || list.isEmpty()) return 0;
        return list.get(0).setScale(0, RoundingMode.HALF_UP).intValue();
    }

    /** % giảm thực tế áp dụng cho 1 biến thể: lấy max giữa KM gán riêng cho biến thể (nếu có) và KM gán cho sản phẩm cha (cách admin đang dùng). */
    public int phanTramGiamChoBienThe(SanPham sanPham, Integer maSanPhamChiTiet) {
        int ptBienThe = phanTramGiamBienThe(maSanPhamChiTiet);
        int ptSanPham = sanPham != null ? phanTramGiamSanPham(sanPham.getMaSanPham()) : 0;
        return Math.max(ptBienThe, ptSanPham);
    }

    public BigDecimal giaSauGiam(BigDecimal giaGoc, int phanTramGiam) {
        if (giaGoc == null) return BigDecimal.ZERO;
        if (phanTramGiam <= 0) return giaGoc;
        BigDecimal heSo = BigDecimal.valueOf(100 - phanTramGiam).divide(BigDecimal.valueOf(100));
        return giaGoc.multiply(heSo).setScale(0, RoundingMode.HALF_UP);
    }

    /** Giá thực tế (đã áp khuyến mãi nếu có) của 1 biến thể. */
    public BigDecimal giaThucTe(SanPhamChiTiet spct) {
        int pt = phanTramGiamChoBienThe(spct.getSanPham(), spct.getMaSanPhamChiTiet());
        return giaSauGiam(spct.getGiaBan(), pt);
    }
}

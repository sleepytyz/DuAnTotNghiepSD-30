package com.example.th06876_java202.Storefront;

import com.example.th06876_java202.Entity.SanPham;
import com.example.th06876_java202.Entity.SanPhamChiTiet;
import com.example.th06876_java202.Service.SanPhamChiTietService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Xây dựng dữ liệu hiển thị (card sản phẩm, danh sách biến thể) cho website bán hàng
 * dựa trên dữ liệu thật trong CSDL (giá, tồn kho, khuyến mãi).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SanPhamHienThiService {

    private final SanPhamChiTietService sanPhamChiTietService;
    private final KhuyenMaiService khuyenMaiService;

    public static final String ANH_MAC_DINH = "/storefront/img/no-image.svg";

    public SanPhamCardVM taoCard(SanPham sp) {
        List<SanPhamChiTiet> bienThe = sanPhamChiTietService.getallsp(sp.getMaSanPham());

        SanPhamCardVM vm = new SanPhamCardVM();
        vm.setMaSanPham(sp.getMaSanPham());
        vm.setTenSanPham(sp.getTenSanPham());
        vm.setTenThuongHieu(sp.getThuongHieu() != null ? sp.getThuongHieu().getTenThuongHieu() : "");

        if (bienThe == null || bienThe.isEmpty()) {
            vm.setAnh(ANH_MAC_DINH);
            vm.setGiaGoc(BigDecimal.ZERO);
            vm.setGiaSauGiam(BigDecimal.ZERO);
            vm.setPhanTramGiam(0);
            vm.setConHang(false);
            return vm;
        }

        // Ưu tiên lấy biến thể còn hàng để tính giá thấp nhất, nếu hết hàng hết thì lấy toàn bộ
        List<SanPhamChiTiet> conHang = bienThe.stream()
                .filter(b -> b.getSoLuongTon() != null && b.getSoLuongTon() > 0)
                .toList();
        List<SanPhamChiTiet> nguon = conHang.isEmpty() ? bienThe : conHang;

        SanPhamChiTiet reGoc = nguon.stream()
                .filter(b -> b.getGiaBan() != null)
                .min(Comparator.comparing(SanPhamChiTiet::getGiaBan))
                .orElse(nguon.get(0));

        String anh = bienThe.stream()
                .map(SanPhamChiTiet::getDuongDanAnh)
                .filter(a -> a != null && !a.isBlank())
                .findFirst()
                .orElse(null);

        int phanTram = khuyenMaiService.phanTramGiamSanPham(sp.getMaSanPham());

        vm.setAnh(anh != null ? "/images/" + anh : ANH_MAC_DINH);
        vm.setGiaGoc(reGoc.getGiaBan() != null ? reGoc.getGiaBan() : BigDecimal.ZERO);
        vm.setPhanTramGiam(phanTram);
        vm.setGiaSauGiam(khuyenMaiService.giaSauGiam(vm.getGiaGoc(), phanTram));
        vm.setConHang(!conHang.isEmpty());
        return vm;
    }

    public List<SanPhamCardVM> taoDanhSachCard(List<SanPham> sanPhams) {
        List<SanPhamCardVM> list = new ArrayList<>();
        if (sanPhams == null) return list;
        for (SanPham sp : sanPhams) {
            list.add(taoCard(sp));
        }
        return list;
    }

    /** Danh sách biến thể (màu/size) còn dùng được, kèm giá khuyến mãi, dùng cho trang chi tiết sản phẩm. */
    public List<BienTheVM> taoDanhSachBienThe(String maSanPham) {
        List<SanPhamChiTiet> bienThe = sanPhamChiTietService.getallsp(maSanPham);
        List<BienTheVM> list = new ArrayList<>();
        if (bienThe == null) return list;
        for (SanPhamChiTiet b : bienThe) {
            int phanTram = khuyenMaiService.phanTramGiamChoBienThe(b.getSanPham(), b.getMaSanPhamChiTiet());
            BigDecimal giaGoc = b.getGiaBan() != null ? b.getGiaBan() : BigDecimal.ZERO;
            BienTheVM vm = new BienTheVM(
                    b.getMaSanPhamChiTiet(),
                    b.getMauSac() != null ? b.getMauSac().getMaMauSac() : null,
                    b.getMauSac() != null ? b.getMauSac().getTenMauSac() : "",
                    b.getKichThuoc() != null ? b.getKichThuoc().getMaKichThuoc() : null,
                    b.getKichThuoc() != null ? b.getKichThuoc().getTenKichThuoc() : "",
                    giaGoc,
                    khuyenMaiService.giaSauGiam(giaGoc, phanTram),
                    phanTram,
                    b.getSoLuongTon() != null ? b.getSoLuongTon() : 0,
                    (b.getDuongDanAnh() != null && !b.getDuongDanAnh().isBlank()) ? "/images/" + b.getDuongDanAnh() : ANH_MAC_DINH
            );
            list.add(vm);
        }
        return list;
    }
}

package com.example.th06876_java202.Storefront;

import com.example.th06876_java202.Entity.GiamGia;
import com.example.th06876_java202.Entity.SanPhamChiTiet;
import com.example.th06876_java202.Service.GiamGiaService;
import com.example.th06876_java202.Service.SanPhamChiTietService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GioHangService {

    public static final BigDecimal NGUONG_FREESHIP = BigDecimal.valueOf(500_000);
    public static final BigDecimal PHI_SHIP_MAC_DINH = BigDecimal.valueOf(30_000);
    public static final int SO_LUONG_TOI_DA_MOI_LAN = 10;

    private final SanPhamChiTietService sanPhamChiTietService;
    private final KhuyenMaiService khuyenMaiService;
    private final GiamGiaService giamGiaService;

    /** Thêm sản phẩm vào giỏ, có kiểm tra tồn kho. Trả về null nếu OK, hoặc thông báo lỗi. */
    public String themVaoGio(GioHang gioHang, Integer maSPCT, int soLuong) {
        if (soLuong <= 0) return "Số lượng không hợp lệ.";
        SanPhamChiTiet spct = sanPhamChiTietService.findbyId(maSPCT).orElse(null);
        if (spct == null) return "Sản phẩm không tồn tại.";
        if (spct.getSoLuongTon() == null || spct.getSoLuongTon() <= 0) return "Sản phẩm đã hết hàng.";

        int daCo = gioHang.getDanhSach().containsKey(maSPCT) ? gioHang.getDanhSach().get(maSPCT).getSoLuong() : 0;
        int soLuongMong = Math.min(soLuong, SO_LUONG_TOI_DA_MOI_LAN);
        if (daCo + soLuongMong > spct.getSoLuongTon()) {
            int conLai = spct.getSoLuongTon() - daCo;
            if (conLai <= 0) return "Bạn đã thêm tối đa số lượng còn lại của sản phẩm này vào giỏ.";
            gioHang.themSanPham(maSPCT, conLai);
            return "Chỉ còn " + conLai + " sản phẩm, đã thêm tối đa có thể vào giỏ hàng.";
        }
        gioHang.themSanPham(maSPCT, soLuongMong);
        return null;
    }

    public String capNhatSoLuong(GioHang gioHang, Integer maSPCT, int soLuong) {
        if (soLuong <= 0) {
            gioHang.xoaSanPham(maSPCT);
            return null;
        }
        SanPhamChiTiet spct = sanPhamChiTietService.findbyId(maSPCT).orElse(null);
        if (spct == null) {
            gioHang.xoaSanPham(maSPCT);
            return "Sản phẩm không còn tồn tại, đã xoá khỏi giỏ hàng.";
        }
        int soLuongTon = spct.getSoLuongTon() != null ? spct.getSoLuongTon() : 0;
        if (soLuong > soLuongTon) {
            gioHang.capNhatSoLuong(maSPCT, soLuongTon);
            return soLuongTon == 0 ? "Sản phẩm đã hết hàng." : "Chỉ còn " + soLuongTon + " sản phẩm trong kho.";
        }
        gioHang.capNhatSoLuong(maSPCT, soLuong);
        return null;
    }

    /** Build dữ liệu hiển thị đầy đủ của giỏ hàng (đã tính KM, voucher, ship) từ dữ liệu mới nhất trong CSDL. */
    public GioHangView xemGioHang(GioHang gioHang, Integer maKhachHangDangNhap) {
        GioHangView view = new GioHangView();

        for (Map.Entry<Integer, GioHangItem> e : new java.util.LinkedHashMap<>(gioHang.getDanhSach()).entrySet()) {
            Integer maSPCT = e.getKey();
            int soLuongTrongGio = e.getValue().getSoLuong();

            SanPhamChiTiet spct = sanPhamChiTietService.findbyId(maSPCT).orElse(null);
            CartLineVM line = new CartLineVM();
            line.setMaSanPhamChiTiet(maSPCT);
            line.setSoLuong(soLuongTrongGio);

            if (spct == null) {
                line.setConHopLe(false);
                line.setTenSanPham("(Sản phẩm không còn tồn tại)");
                line.setThanhTien(BigDecimal.ZERO);
                view.getCanhBao().add("Một sản phẩm trong giỏ hàng không còn tồn tại và sẽ được loại bỏ khi đặt hàng.");
                view.getDongHang().add(line);
                continue;
            }

            int soLuongTon = spct.getSoLuongTon() != null ? spct.getSoLuongTon() : 0;
            int soLuongTinh = Math.min(soLuongTrongGio, soLuongTon);
            if (soLuongTinh != soLuongTrongGio) {
                gioHang.capNhatSoLuong(maSPCT, soLuongTinh);
                view.getCanhBao().add("Sản phẩm \"" + spct.getSanPham().getTenSanPham() + "\" chỉ còn " + soLuongTon + ", đã tự điều chỉnh số lượng trong giỏ.");
            }

            int phanTram = khuyenMaiService.phanTramGiamChoBienThe(spct.getSanPham(), maSPCT);
            BigDecimal donGiaGoc = spct.getGiaBan() != null ? spct.getGiaBan() : BigDecimal.ZERO;
            BigDecimal donGia = khuyenMaiService.giaSauGiam(donGiaGoc, phanTram);

            line.setMaSanPham(spct.getSanPham() != null ? spct.getSanPham().getMaSanPham() : null);
            line.setTenSanPham(spct.getSanPham() != null ? spct.getSanPham().getTenSanPham() : "");
            line.setTenMauSac(spct.getMauSac() != null ? spct.getMauSac().getTenMauSac() : "");
            line.setTenKichThuoc(spct.getKichThuoc() != null ? spct.getKichThuoc().getTenKichThuoc() : "");
            line.setAnh((spct.getDuongDanAnh() != null && !spct.getDuongDanAnh().isBlank())
                    ? "/images/" + spct.getDuongDanAnh() : SanPhamHienThiService.ANH_MAC_DINH);
            line.setDonGia(donGia);
            line.setDonGiaGoc(donGiaGoc);
            line.setSoLuong(soLuongTinh);
            line.setSoLuongTon(soLuongTon);
            line.setConHopLe(soLuongTinh > 0);
            line.setThanhTien(donGia.multiply(BigDecimal.valueOf(soLuongTinh)));

            if (soLuongTinh <= 0) {
                view.getCanhBao().add("Sản phẩm \"" + spct.getSanPham().getTenSanPham() + "\" đã hết hàng và sẽ không được tính khi đặt hàng.");
            }

            view.getDongHang().add(line);
        }

        BigDecimal tongTienHang = view.getDongHang().stream()
                .filter(CartLineVM::isConHopLe)
                .map(CartLineVM::getThanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        view.setTongTienHang(tongTienHang);
        view.setTongSoLuong(view.getDongHang().stream().filter(CartLineVM::isConHopLe).mapToInt(CartLineVM::getSoLuong).sum());

        // Voucher
        if (gioHang.getMaGiamGiaApDung() != null) {
            GiamGia gg = giamGiaService.getGiamGiaById(gioHang.getMaGiamGiaApDung()).orElse(null);
            String loi = giamGiaService.kiemTraVoucherHopLe(gg, maKhachHangDangNhap, tongTienHang);
            if (loi != null) {
                gioHang.setMaGiamGiaApDung(null);
                view.getCanhBao().add("Mã giảm giá đã áp dụng không còn hợp lệ: " + loi);
            } else {
                view.setVoucherApDung(gg);
                view.setSoTienGiamVoucher(giamGiaService.tinhSoTienGiam(gg, tongTienHang));
            }
        }

        // Phí ship: miễn phí từ NGUONG_FREESHIP, áp dụng trên tổng tiền hàng sau khi trừ voucher
        BigDecimal tienSauVoucher = tongTienHang.subtract(view.getSoTienGiamVoucher());
        if (!view.getDongHang().isEmpty() && tienSauVoucher.compareTo(NGUONG_FREESHIP) < 0) {
            view.setTienShip(PHI_SHIP_MAC_DINH);
        } else {
            view.setTienShip(BigDecimal.ZERO);
        }

        BigDecimal tongThanhToan = tienSauVoucher.add(view.getTienShip());
        if (tongThanhToan.compareTo(BigDecimal.ZERO) < 0) tongThanhToan = BigDecimal.ZERO;
        view.setTongThanhToan(tongThanhToan);

        return view;
    }

    public String apDungVoucher(GioHang gioHang, Integer maKhachHangDangNhap, String tenVoucher) {
        if (tenVoucher == null || tenVoucher.isBlank()) return "Vui lòng nhập mã giảm giá.";
        GiamGia gg = giamGiaService.findByTen(tenVoucher.trim()).orElse(null);
        if (gg == null) return "Mã giảm giá không tồn tại.";

        GioHangView tam = xemGioHang(gioHang, maKhachHangDangNhap);
        String loi = giamGiaService.kiemTraVoucherHopLe(gg, maKhachHangDangNhap, tam.getTongTienHang());
        if (loi != null) return loi;

        gioHang.setMaGiamGiaApDung(gg.getMaGiamGia());
        return null;
    }

    public void boVoucher(GioHang gioHang) {
        gioHang.setMaGiamGiaApDung(null);
    }
}

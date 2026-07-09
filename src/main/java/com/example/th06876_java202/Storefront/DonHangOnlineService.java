package com.example.th06876_java202.Storefront;

import com.example.th06876_java202.Entity.*;
import com.example.th06876_java202.Repository.HoaDonRepo;
import com.example.th06876_java202.Repository.SanPhamChiTietRepository;
import com.example.th06876_java202.Service.GiamGiaService;
import com.example.th06876_java202.Service.HoaDonChiTietService;
import com.example.th06876_java202.Service.SanPhamChiTietService;
import com.example.th06876_java202.realtime.ThongBaoRealtimeService;
import com.example.th06876_java202.realtime.TrangThaiModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Xử lý đặt hàng online — TRÁI TIM của kết nối Website bán hàng ⇄ Quản lý bán hàng:
 *  1. Kiểm tra lại tồn kho & giá tại đúng thời điểm đặt (khoá dòng biến thể bằng
 *     PESSIMISTIC_WRITE để 2 khách đặt cùng lúc không thể bán vượt tồn).
 *  2. Tạo HoaDon (LoaiBan = "Online", TrangThai = "Chờ xác nhận") + chi tiết, trừ tồn kho giữ hàng.
 *  3. Phát thông báo THỜI GIAN THỰC (sau khi commit) để màn Quản lý đơn hàng nhận
 *     "hoá đơn chờ xác nhận" ngay lập tức kèm số lượng, tổng tiền; đồng thời cảnh báo
 *     tồn kho và cập nhật bảng trạng thái module.
 */
@Service
@RequiredArgsConstructor
public class DonHangOnlineService {

    private final HoaDonRepo hoaDonRepo;
    private final HoaDonChiTietService hoaDonChiTietService;
    private final SanPhamChiTietService sanPhamChiTietService;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final KhuyenMaiService khuyenMaiService;
    private final GiamGiaService giamGiaService;
    private final ThongBaoRealtimeService thongBaoRealtimeService;
    private final TrangThaiModuleService trangThaiModuleService;

    @Transactional
    public HoaDon datHang(KhachHang khachHang, GioHang gioHang, String diaChiGiaoHang,
                          String phuongThucThanhToan, String ghiChu) {

        if (gioHang == null || gioHang.isEmpty()) {
            throw new DatHangException("Giỏ hàng của bạn đang trống.");
        }
        if (diaChiGiaoHang == null || diaChiGiaoHang.isBlank()) {
            throw new DatHangException("Vui lòng chọn hoặc nhập địa chỉ giao hàng.");
        }

        List<HoaDonChiTiet> dsChiTiet = new ArrayList<>();
        List<SanPhamChiTiet> dsCanLuuLaiTon = new ArrayList<>();
        BigDecimal tongTienHang = BigDecimal.ZERO;

        // Bản sao để tránh ConcurrentModification khi đọc qua session bean
        Map<String, GioHangItem> banSao = new LinkedHashMap<>(gioHang.getDanhSach());

        for (Map.Entry<String, GioHangItem> entry : banSao.entrySet()) {
            String maSPCT = entry.getKey();
            int soLuong = entry.getValue().getSoLuong();

            // KHOÁ dòng biến thể tới khi giao dịch kết thúc — chống 2 đơn cùng lúc bán vượt tồn
            SanPhamChiTiet spct = sanPhamChiTietRepository.khoaBienTheDeDatHang(maSPCT)
                    .orElseThrow(() -> new DatHangException(
                            "Một sản phẩm trong giỏ hàng không còn tồn tại, vui lòng kiểm tra lại giỏ hàng."));

            String tenSp = (spct.getSanPham() != null ? spct.getSanPham().getTenSanPham() : "Sản phẩm");
            String ttBienThe = spct.getTrangThai();
            if ("Ngừng bán".equals(ttBienThe) || "Ngừng kinh doanh".equals(ttBienThe)) {
                throw new DatHangException("Sản phẩm \"" + tenSp + "\" đã ngừng kinh doanh. Vui lòng xoá khỏi giỏ hàng.");
            }

            int soLuongTon = spct.getSoLuongTon() != null ? spct.getSoLuongTon() : 0;
            if (soLuongTon < soLuong) {
                throw new DatHangException("Rất tiếc, sản phẩm \"" + tenSp + "\" hiện chỉ còn " + soLuongTon
                        + " sản phẩm. Vui lòng cập nhật lại giỏ hàng.");
            }

            int phanTramGiam = khuyenMaiService.phanTramGiamChoBienThe(spct.getSanPham(), maSPCT);
            BigDecimal donGiaGoc = spct.getGiaBan() != null ? spct.getGiaBan() : BigDecimal.ZERO;
            BigDecimal donGiaSauGiam = khuyenMaiService.giaSauGiam(donGiaGoc, phanTramGiam);
            BigDecimal tienGiamDong = donGiaGoc.subtract(donGiaSauGiam).multiply(BigDecimal.valueOf(soLuong));
            BigDecimal thanhTienDong = donGiaSauGiam.multiply(BigDecimal.valueOf(soLuong));

            HoaDonChiTiet ct = new HoaDonChiTiet();
            ct.setSanPhamChiTiet(spct);
            ct.setSoLuong(soLuong);
            ct.setDonGia(donGiaGoc);
            ct.setTienGiam(tienGiamDong);
            ct.setThanhTien(thanhTienDong);
            dsChiTiet.add(ct);

            tongTienHang = tongTienHang.add(thanhTienDong);

            // Trừ tồn kho ngay để GIỮ HÀNG cho khách (hoàn lại nếu đơn bị huỷ)
            spct.setSoLuongTon(soLuongTon - soLuong);
            sanPhamChiTietService.capNhatTrangThaii(spct);
            dsCanLuuLaiTon.add(spct);
        }

        // Áp dụng voucher (nếu có) — kiểm tra lại lần cuối cho chắc chắn
        GiamGia voucher = null;
        BigDecimal soTienGiamVoucher = BigDecimal.ZERO;
        String maKhachHang = khachHang != null ? khachHang.getMaKH() : null;
        if (gioHang.getMaGiamGiaApDung() != null) {
            GiamGia gg = giamGiaService.getGiamGiaById(gioHang.getMaGiamGiaApDung()).orElse(null);
            String loi = giamGiaService.kiemTraVoucherHopLe(gg, maKhachHang, tongTienHang);
            if (loi == null) {
                voucher = gg;
                soTienGiamVoucher = giamGiaService.tinhSoTienGiam(gg, tongTienHang);
            }
        }

        BigDecimal tienSauVoucher = tongTienHang.subtract(soTienGiamVoucher);
        BigDecimal tienShip = tienSauVoucher.compareTo(GioHangService.NGUONG_FREESHIP) < 0
                ? GioHangService.PHI_SHIP_MAC_DINH : BigDecimal.ZERO;
        BigDecimal tongThanhToan = tienSauVoucher.add(tienShip);
        if (tongThanhToan.compareTo(BigDecimal.ZERO) < 0) tongThanhToan = BigDecimal.ZERO;

        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHoaDon(taoMaHoaDon());
        hoaDon.setMaKhachHang(khachHang);
        hoaDon.setMaNhanVien(null); // khách tự đặt online, chưa có nhân viên xử lý
        hoaDon.setMaGiamGia(voucher);
        hoaDon.setTongTien(tongThanhToan);
        hoaDon.setTienShip(tienShip);
        hoaDon.setPhuongThucThanhToan(phuongThucThanhToan);
        hoaDon.setTrangThai("Chờ xác nhận");
        hoaDon.setGhiChu(ghiChu);
        hoaDon.setNgayTao(LocalDateTime.now());
        hoaDon.setLoaiBan("Online");
        hoaDon.setDiaChiGiaoHang(diaChiGiaoHang);

        HoaDon hoaDonDaLuu = hoaDonRepo.save(hoaDon);

        for (HoaDonChiTiet ct : dsChiTiet) {
            ct.setMaHoaDon(hoaDonDaLuu);
        }
        hoaDonChiTietService.luuTatCa(dsChiTiet);

        for (SanPhamChiTiet spct : dsCanLuuLaiTon) {
            sanPhamChiTietService.them(spct);
        }

        if (voucher != null) {
            giamGiaService.giamSoLuongVoucher(voucher.getMaGiamGia());
            giamGiaService.danhDauDaSuDungChoKhachHang(maKhachHang, voucher.getMaGiamGia());
        }

        gioHang.xoaTatCa();

        // ===== THỜI GIAN THỰC: đẩy "hoá đơn chờ xác nhận" + cảnh báo tồn kho sang Quản lý =====
        // (Các thông điệp chỉ được gửi SAU KHI transaction này commit thành công)
        thongBaoRealtimeService.donHangOnlineMoi(hoaDonDaLuu, dsChiTiet);
        for (SanPhamChiTiet spct : dsCanLuuLaiTon) {
            thongBaoRealtimeService.kiemTraVaCanhBaoTonKho(spct);
        }
        trangThaiModuleService.phatNgay();

        return hoaDonDaLuu;
    }

    /**
     * Sinh mã hoá đơn duy nhất cho đơn online, dạng "HDyyMMddHHmmss" + hậu tố nếu trùng,
     * vừa khít cột MaHoaDon VARCHAR(20).
     */
    private String taoMaHoaDon() {
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyMMddHHmmss");
        String base = "HD" + LocalDateTime.now().format(fmt);
        String ma = base;
        int suffix = 0;
        while (hoaDonRepo.existsById(ma)) {
            suffix++;
            ma = base + suffix;
            if (ma.length() > 20) {
                ma = "HD" + Long.toString(System.currentTimeMillis(), 36).toUpperCase();
                if (ma.length() > 20) ma = ma.substring(0, 20);
            }
        }
        return ma;
    }

    /**
     * Khách tự huỷ đơn khi đơn còn ở trạng thái "Chờ xác nhận" (chưa được nhân viên xử lý).
     * Hoàn lại tồn kho và voucher (nếu có) đã trừ khi đặt hàng, đồng thời báo realtime cho Quản lý.
     */
    @Transactional
    public void khachHuyDon(HoaDon hoaDon) {
        if (hoaDon == null || !"Chờ xác nhận".equals(hoaDon.getTrangThai())) {
            throw new DatHangException("Đơn hàng này không thể huỷ ở trạng thái hiện tại. Vui lòng liên hệ FS Shoes để được hỗ trợ.");
        }
        String trangThaiCu = hoaDon.getTrangThai();

        hoanTonKhoVaVoucher(hoaDon);

        hoaDon.setTrangThai("Đã huỷ");
        hoaDonRepo.save(hoaDon);

        thongBaoRealtimeService.trangThaiDonThayDoi(hoaDon, trangThaiCu, "Khách hàng");
        trangThaiModuleService.phatNgay();
    }

    /**
     * Nhân viên/Quản lý huỷ đơn online khi đơn CHƯA giao xong
     * (Chờ xác nhận / Đã xác nhận / Đang giao). Hoàn lại tồn kho và voucher đã trừ.
     * Không cho huỷ đơn đã ở trạng thái kết thúc (Đã giao / Đã huỷ / Đã trả hàng).
     */
    @Transactional
    public void huyDonAdmin(HoaDon hoaDon) {
        if (hoaDon == null) {
            throw new DatHangException("Không tìm thấy đơn hàng.");
        }
        String tt = hoaDon.getTrangThai();
        boolean coTheHuy = "Chờ xác nhận".equals(tt) || "Đã xác nhận".equals(tt) || "Đang giao".equals(tt);
        if (!coTheHuy) {
            throw new DatHangException("Đơn ở trạng thái \"" + tt + "\" không thể huỷ.");
        }

        hoanTonKhoVaVoucher(hoaDon);

        hoaDon.setTrangThai("Đã huỷ");
        hoaDonRepo.save(hoaDon);

        thongBaoRealtimeService.trangThaiDonThayDoi(hoaDon, tt, "Quản lý bán hàng");
        trangThaiModuleService.phatNgay();
    }

    /** Hoàn tồn kho toàn bộ dòng hàng + hoàn voucher của một đơn sắp huỷ. */
    private void hoanTonKhoVaVoucher(HoaDon hoaDon) {
        List<HoaDonChiTiet> dsChiTiet = hoaDonChiTietService.findByHoaDOn(hoaDon);
        for (HoaDonChiTiet ct : dsChiTiet) {
            SanPhamChiTiet spct = ct.getSanPhamChiTiet();
            if (spct != null) {
                int tonHienTai = spct.getSoLuongTon() != null ? spct.getSoLuongTon() : 0;
                spct.setSoLuongTon(tonHienTai + (ct.getSoLuong() != null ? ct.getSoLuong() : 0));
                sanPhamChiTietService.capNhatTrangThaii(spct);
                sanPhamChiTietService.them(spct);
            }
        }
        if (hoaDon.getMaGiamGia() != null) {
            String maKH = hoaDon.getMaKhachHang() != null ? hoaDon.getMaKhachHang().getMaKH() : null;
            giamGiaService.hoanLaiVoucher(hoaDon.getMaGiamGia().getMaGiamGia(), maKH);
        }
    }
}

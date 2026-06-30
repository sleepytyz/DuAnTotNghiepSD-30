package com.example.th06876_java202.Storefront;

import com.example.th06876_java202.Entity.*;
import com.example.th06876_java202.Repository.HoaDonRepo;
import com.example.th06876_java202.Service.GiamGiaService;
import com.example.th06876_java202.Service.HoaDonChiTietService;
import com.example.th06876_java202.Service.SanPhamChiTietService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Xử lý đặt hàng online: kiểm tra lại tồn kho/giá tại thời điểm đặt hàng (không tin dữ liệu cũ trong session),
 * tạo HoaDon (loại bán = Online, trạng thái = Chờ xác nhận) + ChiTietHoaDon, trừ tồn kho ngay để giữ hàng,
 * và đẩy vào đúng luồng xử lý mà bên Quản lý bán hàng (admin) đã có sẵn (màn "Đơn hàng" /donhang/index).
 */
@Service
@RequiredArgsConstructor
public class DonHangOnlineService {

    private final HoaDonRepo hoaDonRepo;
    private final HoaDonChiTietService hoaDonChiTietService;
    private final SanPhamChiTietService sanPhamChiTietService;
    private final KhuyenMaiService khuyenMaiService;
    private final GiamGiaService giamGiaService;

    @Transactional
    public HoaDon datHang(KhachHang khachHang, GioHang gioHang, String diaChiGiaoHang, String phuongThucThanhToan, String ghiChu) {

        if (gioHang == null || gioHang.isEmpty()) {
            throw new DatHangException("Giỏ hàng của bạn đang trống.");
        }
        if (diaChiGiaoHang == null || diaChiGiaoHang.isBlank()) {
            throw new DatHangException("Vui lòng chọn hoặc nhập địa chỉ giao hàng.");
        }

        List<HoaDonChiTiet> dsChiTiet = new ArrayList<>();
        List<SanPhamChiTiet> dsCanLuuLaiTon = new ArrayList<>();
        BigDecimal tongTienHang = BigDecimal.ZERO;

        // Dùng bản sao để tránh ConcurrentModification khi đọc qua session bean
        Map<Integer, GioHangItem> banSao = new LinkedHashMap<>(gioHang.getDanhSach());

        for (Map.Entry<Integer, GioHangItem> entry : banSao.entrySet()) {
            Integer maSPCT = entry.getKey();
            int soLuong = entry.getValue().getSoLuong();

            SanPhamChiTiet spct = sanPhamChiTietService.findbyId(maSPCT)
                    .orElseThrow(() -> new DatHangException("Một sản phẩm trong giỏ hàng không còn tồn tại, vui lòng kiểm tra lại giỏ hàng."));

            int soLuongTon = spct.getSoLuongTon() != null ? spct.getSoLuongTon() : 0;
            String tenSp = (spct.getSanPham() != null ? spct.getSanPham().getTenSanPham() : "Sản phẩm");
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

            spct.setSoLuongTon(soLuongTon - soLuong);
            sanPhamChiTietService.capNhatTrangThaii(spct);
            dsCanLuuLaiTon.add(spct);
        }

        // Áp dụng voucher (nếu có) - kiểm tra lại lần cuối cho chắc chắn
        GiamGia voucher = null;
        BigDecimal soTienGiamVoucher = BigDecimal.ZERO;
        Integer maKhachHang = khachHang != null ? khachHang.getMaKH() : null;
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
        hoaDon.setMaKhachHang(khachHang);
        hoaDon.setMaNhanVien(null); // khách tự đặt online, chưa có nhân viên xử lý
        hoaDon.setMaGiamGia(voucher);
        hoaDon.setTongTien(tongThanhToan);
        hoaDon.setTienShip(tienShip);
        hoaDon.setPhuongThucThanhToan(phuongThucThanhToan);
        hoaDon.setTrangThai("Chờ xác nhận");
        hoaDon.setGhiChu(ghiChu);
        hoaDon.setNgayTao(LocalDate.now());
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

        return hoaDonDaLuu;
    }

    /**
     * Khách tự hủy đơn khi đơn còn ở trạng thái "Chờ xác nhận" (chưa được nhân viên xử lý).
     * Hoàn lại tồn kho và voucher (nếu có) đã trừ khi đặt hàng.
     */
    @Transactional
    public void khachHuyDon(HoaDon hoaDon) {
        if (hoaDon == null || !"Chờ xác nhận".equals(hoaDon.getTrangThai())) {
            throw new DatHangException("Đơn hàng này không thể huỷ ở trạng thái hiện tại. Vui lòng liên hệ FS Shoes để được hỗ trợ.");
        }

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
            Integer maKH = hoaDon.getMaKhachHang() != null ? hoaDon.getMaKhachHang().getMaKH() : null;
            giamGiaService.hoanLaiVoucher(hoaDon.getMaGiamGia().getMaGiamGia(), maKH);
        }

        hoaDon.setTrangThai("Đã huỷ");
        hoaDonRepo.save(hoaDon);
    }
}

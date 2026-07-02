package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.GiamGia;
import com.example.th06876_java202.Entity.GiamGiaChiTiet;
import com.example.th06876_java202.Entity.GiamGiaChiTietId;
import com.example.th06876_java202.Repository.GiamGiaChiTietRepo;
import com.example.th06876_java202.Repository.GiamGiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class GiamGiaService {

    @Autowired
    GiamGiaRepository giamGiaRepository;

    @Autowired
    GiamGiaChiTietRepo giamGiaChiTietRepo;

    private final Random random = new Random();

    // Trong GiamGiaService.java - Thêm method

    @Transactional
    public void markVoucherAsUsed(String maKhachHang, String maGiamGia) {
        GiamGiaChiTietId id = new GiamGiaChiTietId(maKhachHang, maGiamGia);
        GiamGiaChiTiet ct = giamGiaChiTietRepo.findById(id).orElse(null);

        if (ct != null) {
            ct.setTrangThaiSuDung(1);  // Đánh dấu đã sử dụng
            giamGiaChiTietRepo.save(ct);
        }
    }

    // ===== TẠO MÃ GIẢM GIÁ TỰ ĐỘNG =====
    public String generateMaGiamGia() {
        String code;
        boolean exists;
        int attempts = 0;
        int maxAttempts = 100;

        do {
            int randomNumber = 1000 + random.nextInt(9000);
            code = "GG" + randomNumber;
            exists = giamGiaRepository.existsById(code);
            attempts++;

            if (attempts > maxAttempts) {
                code = "GG" + System.currentTimeMillis();
                break;
            }
        } while (exists);

        return code;
    }

    // ===== LƯU - TỰ ĐỘNG SET NGÀY TẠO =====
    public GiamGia save(GiamGia giamGia) {
        if (giamGia.getNgayTao() == null) {
            giamGia.setNgayTao(LocalDateTime.now());
        }
        return giamGiaRepository.save(giamGia);
    }

    // ===== LẤY TẤT CẢ SẮP XẾP THEO NGÀY TẠO GIẢM DẦN =====
    public List<GiamGia> getGiamGia() {
        return giamGiaRepository.findAllOrderByNgayTaoDesc();
    }

    // ===== CÁC METHOD KHÁC =====
    public List<GiamGia> getGiamGia3() {
        return giamGiaRepository.findDanhSachCanCapNhat();
    }

    // ⭐ SỬA: Lấy TẤT CẢ voucher (không filter)
    public List<GiamGia> getGiamGia1() {
        return giamGiaRepository.findSoLuongVoucher();
    }

    // ⭐ THÊM: Lấy voucher đang hoạt động
    public List<GiamGia> getVoucherDangHoatDong() {
        return giamGiaRepository.findVoucherDangHoatDong();
    }

    public void giamSoLuongVoucher(String id){
        giamGiaRepository.giamSoLuongVoucher(id);
    }

    public Optional<GiamGia> getGiamGiaById(String id) {
        return giamGiaRepository.findById(id);
    }

    public void suattt(String magg){
        giamGiaRepository.updateGiamGiaaa(magg);
    }

    public List<GiamGia> timkiem(String keyword) {
        return giamGiaRepository.timkiem(keyword);
    }

    public List<GiamGia> loclg(String keyword) {
        return giamGiaRepository.getGiamGia(keyword);
    }

    public List<GiamGia> loctt(String keyword) {
        return giamGiaRepository.loctt(keyword);
    }

    public List<GiamGia> locng(LocalDateTime date, LocalDateTime time) {
        return giamGiaRepository.timkiemngay(date,time);
    }

    public boolean existsTenGiamGia(String tenChuongTrinh) {
        return giamGiaRepository.existsByTenGiamGia(tenChuongTrinh);
    }

    @Transactional
    public void activateVoucher(String id) {
        giamGiaRepository.activateVoucher(id);
    }

    // ===== FILTER WITH LOAI_AP_DUNG =====
    public Page<GiamGia> getFilteredGiamGia(String kw, String tt, String lg, Integer loaiApDung,
                                            LocalDateTime start, LocalDateTime end, int page) {
        Pageable pageable = PageRequest.of(page, 5);
        return giamGiaRepository.filterAll(kw, tt, lg, loaiApDung, start, end, pageable);
    }

    public List<GiamGia> findAllFiltered(String kw, String tt, String lg, Integer loaiApDung,
                                         LocalDateTime start, LocalDateTime end) {
        return giamGiaRepository.findAllFiltered(kw, tt, lg, loaiApDung, start, end);
    }

    @Transactional
    public void xoaMem(String id) {
        giamGiaRepository.updateTrangThai("Ngừng hoạt động", id);

        // 2. Cập nhật chi tiết
        giamGiaChiTietRepo.updateTrangThaiByMaGiamGia(id, 0);
    }

    public String tinhToanTrangThai(GiamGia gg) {
        if (gg == null) return "Ngừng hoạt động";

        LocalDateTime now = LocalDateTime.now();
        if (gg.getNgayBatDau() == null || gg.getNgayKetThuc() == null) return "Ngừng hoạt động";

        // ⭐ KIỂM TRA VÔ HẠN TRƯỚC - NẾU VÔ HẠN THÌ KHÔNG CẦN KIỂM TRA SỐ LƯỢNG
        if (gg.getIsVoHan() != null && gg.getIsVoHan()) {
            // Vô hạn: chỉ cần kiểm tra ngày tháng
            if (gg.getNgayBatDau() != null && gg.getNgayKetThuc() != null) {
                if (now.isBefore(gg.getNgayBatDau())) {
                    return "Sắp hoạt động";
                } else if (now.isAfter(gg.getNgayKetThuc())) {
                    return "Ngừng hoạt động";
                } else {
                    return "Hoạt động";
                }
            }
            return "Hoạt động"; // Nếu không có ngày tháng, mặc định hoạt động
        }

        // ⭐ KHÔNG VÔ HẠN: KIỂM TRA SỐ LƯỢNG + NGÀY THÁNG
        if (gg.getSoLuong() == null || gg.getSoLuong() <= 0) {
            return "Hết lượt";
        }

        if (gg.getNgayBatDau() != null && gg.getNgayKetThuc() != null) {
            if (now.isBefore(gg.getNgayBatDau())) {
                return "Sắp hoạt động";
            } else if (now.isAfter(gg.getNgayKetThuc())) {
                return "Ngừng hoạt động";
            } else {
                return "Hoạt động";
            }
        }

        return "Hoạt động";
    }

    @Transactional
    public void capNhatTrangThaiChoScheduler(String trangThai, String id) {
        giamGiaRepository.updateTrangThai(trangThai, id);
    }

    @Transactional
    public void updateTrangThaiToStop(String id) {
        giamGiaRepository.updateTrangThaiToStop(id);
    }
    public Optional<GiamGia> findByTen(String tenGiamGia) {
        return giamGiaRepository.findByTenGiamGiaIgnoreCase(tenGiamGia == null ? "" : tenGiamGia.trim());
    }

    /**
     * Kiểm tra voucher có dùng được cho khách hàng (đang đăng nhập hoặc khách lẻ) với tổng tiền đơn hàng hiện tại không.
     * Trả về null nếu hợp lệ, hoặc chuỗi lý do không hợp lệ.
     */
    public String kiemTraVoucherHopLe(GiamGia gg, String maKhachHang, BigDecimal tongTienHang) {
        if (gg == null) return "Mã giảm giá không tồn tại.";
        if (!"Hoạt động".equals(gg.getTrangThai())) return "Mã giảm giá hiện không khả dụng.";
        if (gg.getSoLuong() == null || gg.getSoLuong() <= 0) return "Mã giảm giá đã hết lượt sử dụng.";
        if (gg.getDonToiThieu() != null && tongTienHang.compareTo(gg.getDonToiThieu()) < 0) {
            return "Đơn hàng chưa đạt giá trị tối thiểu " + gg.getDonToiThieu().toBigInteger() + " đ để áp dụng mã này.";
        }
        if (gg.getLoaiApDung() != null && gg.getLoaiApDung() == 2) {
            if (maKhachHang == null) return "Bạn cần đăng nhập để sử dụng mã giảm giá này.";
            GiamGiaChiTiet ct = giamGiaChiTietRepo
                    .findByKhachHang_MaKHAndGiamGia_MaGiamGia(maKhachHang, gg.getMaGiamGia())
                    .orElse(null);
            if (ct == null) return "Mã giảm giá này không áp dụng cho tài khoản của bạn.";
            if (ct.getTrangThaiSuDung() != null && ct.getTrangThaiSuDung() == 1) return "Bạn đã sử dụng mã giảm giá này.";
        }
        return null;
    }

    public BigDecimal tinhSoTienGiam(GiamGia gg, BigDecimal tongTienHang) {
        if (gg == null) return BigDecimal.ZERO;
        BigDecimal soTienGiam;
        if ("PhanTram".equals(gg.getLoaiGiamGia())) {
            soTienGiam = tongTienHang.multiply(gg.getGiaTriGiam()).divide(BigDecimal.valueOf(100));
        } else {
            soTienGiam = gg.getGiaTriGiam();
        }
        if (gg.getGiamToiDa() != null && gg.getGiamToiDa().compareTo(BigDecimal.ZERO) > 0
                && soTienGiam.compareTo(gg.getGiamToiDa()) > 0) {
            soTienGiam = gg.getGiamToiDa();
        }
        if (soTienGiam.compareTo(tongTienHang) > 0) soTienGiam = tongTienHang;
        return soTienGiam;
    }

    /**
     * Danh sách voucher khách hàng (đăng nhập) có thể dùng: voucher công khai còn lượt + voucher riêng được gán cho khách và chưa dùng.
     */
    public List<GiamGia> getVoucherKhaDungChoKhachHang(String maKhachHang) {
        List<GiamGia> ketQua = new java.util.ArrayList<>();
        for (GiamGia gg : giamGiaRepository.findAll()) {
            if (!"Hoạt động".equals(gg.getTrangThai())) continue;
            if (gg.getSoLuong() == null || gg.getSoLuong() <= 0) continue;
            if (gg.getLoaiApDung() != null && gg.getLoaiApDung() == 1) {
                ketQua.add(gg);
            } else if (maKhachHang != null) {
                GiamGiaChiTiet ct = giamGiaChiTietRepo
                        .findByKhachHang_MaKHAndGiamGia_MaGiamGia(maKhachHang, gg.getMaGiamGia())
                        .orElse(null);
                if (ct != null && (ct.getTrangThaiSuDung() == null || ct.getTrangThaiSuDung() == 0)) {
                    ketQua.add(gg);
                }
            }
        }
        return ketQua;
    }

    @Transactional
    public void danhDauDaSuDungChoKhachHang(String maKhachHang, String maGiamGia) {
        if (maKhachHang == null) return;
        giamGiaChiTietRepo.findByKhachHang_MaKHAndGiamGia_MaGiamGia(maKhachHang, maGiamGia)
                .ifPresent(ct -> {
                    ct.setTrangThaiSuDung(1);
                    giamGiaChiTietRepo.save(ct);
                });
    }

    @Transactional
    public void hoanLaiVoucher(String maGiamGia, String maKhachHang) {
        giamGiaRepository.findById(maGiamGia).ifPresent(gg -> {
            gg.setSoLuong(gg.getSoLuong() == null ? 1 : gg.getSoLuong() + 1);
            giamGiaRepository.save(gg);
        });
        if (maKhachHang != null) {
            giamGiaChiTietRepo.findByKhachHang_MaKHAndGiamGia_MaGiamGia(maKhachHang, maGiamGia)
                    .ifPresent(ct -> {
                        ct.setTrangThaiSuDung(0);
                        giamGiaChiTietRepo.save(ct);
                    });
        }
    }

}
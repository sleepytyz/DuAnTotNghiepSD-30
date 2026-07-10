package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.CaLamViec;
import com.example.th06876_java202.Entity.ChamCong;
import com.example.th06876_java202.Entity.NhanVien;
import com.example.th06876_java202.Repository.ChamCongRepository;
import com.example.th06876_java202.Repository.NhanVienRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChamCongService {

    private final ChamCongRepository chamCongRepository;
    private final NhanVienRepository nhanVienRepository;

    /**
     * Lấy danh sách chấm công của nhân viên theo khoảng thời gian
     */
    public List<ChamCong> getChamCongByNhanVienAndDateRange(String maNhanVien, LocalDate tuNgay, LocalDate denNgay) {
        return chamCongRepository.findByNhanVien_MaNhanVienAndNgayChamCongBetween(maNhanVien, tuNgay, denNgay);
    }

    /**
     * Lấy danh sách chấm công của nhân viên trong ngày
     */
    public List<ChamCong> getChamCongByNhanVienAndDate(String maNhanVien, LocalDate ngay) {
        return chamCongRepository.findByNhanVien_MaNhanVienAndNgayChamCong(maNhanVien, ngay);
    }

    /**
     * Chấm công vào (Check-in)
     */
    @Transactional
    public ChamCong checkin(Integer maChamCong, String maNhanVien) {
        ChamCong chamCong = chamCongRepository.findById(maChamCong)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch chấm công"));

        // Kiểm tra nhân viên có đúng không
        if (!chamCong.getNhanVien().getMaNhanVien().equals(maNhanVien)) {
            throw new RuntimeException("Bạn không có quyền chấm công cho lịch này");
        }

        // Kiểm tra đã chấm công vào chưa
        if (chamCong.getGioVao() != null) {
            throw new RuntimeException("Bạn đã chấm công vào lúc " + chamCong.getGioVao());
        }

        // Kiểm tra ngày chấm công
        LocalDate today = LocalDate.now();
        if (!chamCong.getNgayChamCong().equals(today)) {
            throw new RuntimeException("Chỉ có thể chấm công vào ngày hôm nay (" + today + ")");
        }

        // Kiểm tra giờ chấm công có nằm trong ca không
        LocalTime now = LocalTime.now();
        CaLamViec ca = chamCong.getCaLamViec();
        if (now.isBefore(ca.getGioBatDau().minusMinutes(30))) {
            throw new RuntimeException("Chưa đến giờ bắt đầu ca làm việc (" + ca.getGioBatDau() + ")");
        }

        // Thực hiện check-in
        chamCong.setGioVao(now);
        chamCong.setTrangThai(false); // Đã chấm công
        chamCong.setGhiChu("Đã check-in lúc " + now);

        return chamCongRepository.save(chamCong);
    }

    /**
     * Chấm công ra (Check-out)
     */
    @Transactional
    public ChamCong checkout(Integer maChamCong, String maNhanVien) {
        ChamCong chamCong = chamCongRepository.findById(maChamCong)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch chấm công"));

        // Kiểm tra nhân viên có đúng không
        if (!chamCong.getNhanVien().getMaNhanVien().equals(maNhanVien)) {
            throw new RuntimeException("Bạn không có quyền chấm công cho lịch này");
        }

        // Kiểm tra đã chấm công vào chưa
        if (chamCong.getGioVao() == null) {
            throw new RuntimeException("Bạn chưa chấm công vào");
        }

        // Kiểm tra đã chấm công ra chưa
        if (chamCong.getGioRa() != null) {
            throw new RuntimeException("Bạn đã chấm công ra lúc " + chamCong.getGioRa());
        }

        // Kiểm tra ngày chấm công
        LocalDate today = LocalDate.now();
        if (!chamCong.getNgayChamCong().equals(today)) {
            throw new RuntimeException("Chỉ có thể chấm công ra vào ngày hôm nay (" + today + ")");
        }

        // Thực hiện check-out
        LocalTime now = LocalTime.now();
        chamCong.setGioRa(now);

        // Tính số giờ làm (lưu 2 chữ số thập phân để ca ngắn không bị làm tròn sai,
        // ví dụ 3 phút = 0.05h chứ không phọt thành 0.1h).
        Duration duration = Duration.between(chamCong.getGioVao(), now);
        double hours = duration.toMinutes() / 60.0;
        if (hours > 0) {
            BigDecimal soGioLam = BigDecimal.valueOf(hours).setScale(2, RoundingMode.HALF_UP);
            chamCong.setSoGioLam(soGioLam);
        }

        long tongPhut = duration.toMinutes();
        String moTaThoiGian = (tongPhut >= 60)
                ? (tongPhut / 60) + "h " + (tongPhut % 60) + "p"
                : tongPhut + " phút";
        chamCong.setGhiChu("Đã check-out lúc " + now + " - Tổng thời gian: " + moTaThoiGian);

        return chamCongRepository.save(chamCong);
    }

    /**
     * Lấy tổng số giờ làm trong ngày của nhân viên
     */
    public double getTongGioLamTrongNgay(String maNhanVien, LocalDate ngay) {
        List<ChamCong> list = getChamCongByNhanVienAndDate(maNhanVien, ngay);
        return list.stream()
                .filter(cc -> cc.getSoGioLam() != null)
                .mapToDouble(cc -> cc.getSoGioLam().doubleValue())
                .sum();
    }

    /**
     * Kiểm tra nhân viên đã check-in trong ngày chưa
     */
    public boolean isCheckedIn(String maNhanVien, LocalDate ngay) {
        List<ChamCong> list = getChamCongByNhanVienAndDate(maNhanVien, ngay);
        return list.stream().anyMatch(cc -> cc.getGioVao() != null && cc.getGioRa() == null);
    }

    /**
     * Kiểm tra nhân viên đã check-out trong ngày chưa
     */
    public boolean isCheckedOut(String maNhanVien, LocalDate ngay) {
        List<ChamCong> list = getChamCongByNhanVienAndDate(maNhanVien, ngay);
        return list.stream().anyMatch(cc -> cc.getGioRa() != null);
    }
}
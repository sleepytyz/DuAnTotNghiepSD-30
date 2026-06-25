package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.GiamGia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GiamGiaSchedulerService {

    @Autowired
    private GiamGiaService giamGiaService;

    @Scheduled(fixedRate = 60000)
    public void tuDongCapNhatTrangThai() {
        System.out.println("Scheduler đang chạy kiểm tra trạng thái lúc: " + LocalDateTime.now());
        List<GiamGia> danhSach = giamGiaService.getGiamGia3();
        LocalDateTime now = LocalDateTime.now();

        for (GiamGia gg : danhSach) {
            String trangThaiMoi = tinhToanTrangThai(gg, now);


            if (!trangThaiMoi.equals(gg.getTrangThai())) {
                giamGiaService.capNhatTrangThaiChoScheduler(trangThaiMoi, gg.getMaGiamGia());
            }
        }
    }

    private String tinhToanTrangThai(GiamGia gg, LocalDateTime now) {
        if (now.isBefore(gg.getNgayBatDau())) {
            return "Sắp hoạt động";
        }
        if (now.isAfter(gg.getNgayKetThuc())) {
            return "Ngừng hoạt động";
        }
        return "Hoạt động";
    }
}
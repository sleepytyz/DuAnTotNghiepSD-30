package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.HoaDon;
import com.example.th06876_java202.Repository.HoaDonRepo;
import com.example.th06876_java202.realtime.ThongBaoRealtimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HoaDonService {

    @Autowired
    private HoaDonRepo repo;

    @Autowired
    private ThongBaoRealtimeService thongBaoRealtimeService;

    // ===== PHƯƠNG THỨC MỚI =====

    /**
     * Tìm hóa đơn theo danh sách trạng thái cho phép (Phân trang)
     */
    public Page<HoaDon> findByTrangThaiIn(List<String> trangThaiList, Pageable pageable) {
        return repo.findByTrangThaiIn(trangThaiList, pageable);
    }

    /**
     * Tìm hóa đơn theo danh sách trạng thái cho phép (Không phân trang)
     */
    public List<HoaDon> findByTrangThaiInList(List<String> trangThaiList) {
        return repo.findByTrangThaiIn(trangThaiList);
    }

    /**
     * Tìm hóa đơn theo ngày và danh sách trạng thái cho phép (Phân trang)
     */
    public Page<HoaDon> searchByNgayTaodhAndStatus(LocalDateTime ngay, LocalDateTime ngay2,
                                                   List<String> allowedStatuses, Pageable pageable) {
        if (ngay != null && ngay2 != null) {
            return repo.findByNgayTaoBetweenAndTrangThaiIn(ngay, ngay2, allowedStatuses, pageable);
        } else if (ngay != null) {
            return repo.findByNgayTaoAfterAndTrangThaiIn(ngay, allowedStatuses, pageable);
        } else if (ngay2 != null) {
            return repo.findByNgayTaoBeforeAndTrangThaiIn(ngay2, allowedStatuses, pageable);
        }
        return repo.findByTrangThaiIn(allowedStatuses, pageable);
    }

    /**
     * Tìm hóa đơn theo ngày và danh sách trạng thái cho phép (Không phân trang)
     */
    public List<HoaDon> searchByNgayTaodhAndStatusList(LocalDateTime ngay, LocalDateTime ngay2,
                                                       List<String> allowedStatuses) {
        if (ngay != null && ngay2 != null) {
            return repo.findByNgayTaoBetweenAndTrangThaiIn(ngay, ngay2, allowedStatuses);
        } else if (ngay != null) {
            return repo.findByNgayTaoAfterAndTrangThaiIn(ngay, allowedStatuses);
        } else if (ngay2 != null) {
            return repo.findByNgayTaoBeforeAndTrangThaiIn(ngay2, allowedStatuses);
        }
        return repo.findByTrangThaiIn(allowedStatuses);
    }

    /**
     * Tìm hóa đơn theo mã và danh sách trạng thái cho phép
     */
    public Page<HoaDon> searchByMaAndStatus(String maHoaDon, List<String> allowedStatuses, Pageable pageable) {
        return repo.findByMaHoaDonAndTrangThaiIn(maHoaDon, allowedStatuses, pageable);
    }

    // ===== PHƯƠNG THỨC CŨ (GIỮ NGUYÊN) =====

    public List<HoaDon> findByTrangThai(String trangThai) {
        return repo.findByTrangThai(trangThai);
    }

    public Page<HoaDon> getALLDH(Pageable pageable) {
        return repo.findAll(pageable);
    }

    public List<HoaDon> getAllDH() {
        return repo.findAll(Sort.by(Sort.Direction.DESC, "ngayTao"));
    }

    public HoaDon findById(String id) {
        return repo.findById(id).orElse(null);
    }

    public List<HoaDon> getALLDHHUY() {
        return repo.findByTrangThai("Đã huỷ");
    }

    public Page<HoaDon> findByTrangThai(String trangThai, Pageable pageable) {
        return repo.findByTrangThai(trangThai, pageable);
    }

    public List<HoaDon> findAllByTrangThai(String trangThai) {
        return repo.findByTrangThai(trangThai);
    }

    public List<HoaDon> findByTrangThaiAndLoaiBan(String trangThai, String loaiBan) {
        return repo.findByTrangThaiAndLoaiBan(trangThai, loaiBan);
    }

    public List<HoaDon> getAll() {
        return repo.findAll();
    }


    public Page<HoaDon> searchByNgayTaodh(LocalDateTime ngay, LocalDateTime ngay2, Pageable pageable) {
        if (ngay != null && ngay2 != null) {
            return repo.findByNgayTaoBetween(ngay, ngay2, pageable);
        } else if (ngay != null) {
            return repo.findByNgayTaoAfter(ngay, pageable);
        } else if (ngay2 != null) {
            return repo.findByNgayTaoBefore(ngay2, pageable);
        }
        return repo.findAll(pageable);
    }

    public List<HoaDon> searchByNgayTaodh(LocalDateTime ngay, LocalDateTime ngay2) {
        if (ngay != null && ngay2 != null) {
            return repo.findByNgayTaoBetween(ngay, ngay2);
        } else if (ngay != null) {
            return repo.findByNgayTaoAfter(ngay);
        } else if (ngay2 != null) {
            return repo.findByNgayTaoBefore(ngay2);
        }
        return getAllDH();
    }

    public long countByTrangThai(String trangThai) {
        return repo.countByTrangThai(trangThai);
    }

    /**
     * Chờ xác nhận -> Đã xác nhận (nhân viên bấm "Xác nhận" bên quản lý).
     * Sau khi lưu sẽ phát thông báo thời gian thực cho cả 2 phía (quản lý + khách).
     */
    public void suatt(String mahd) {
        HoaDon hd = repo.findById(mahd).orElse(null);
        if (hd != null && "Chờ xác nhận".equals(hd.getTrangThai())) {
            String cu = hd.getTrangThai();
            hd.setTrangThai("Đã xác nhận");
            repo.save(hd);
            thongBaoRealtimeService.trangThaiDonThayDoi(hd, cu, "Quản lý bán hàng");
        }
    }

    public HoaDon save(HoaDon hoaDon) {
        return repo.save(hoaDon);
    }

    /** Đã xác nhận -> Đang giao. */
    public void suattdg(String mahd) {
        HoaDon hd = repo.findById(mahd).orElse(null);
        if (hd != null && "Đã xác nhận".equals(hd.getTrangThai())) {
            String cu = hd.getTrangThai();
            hd.setTrangThai("Đang giao");
            repo.save(hd);
            thongBaoRealtimeService.trangThaiDonThayDoi(hd, cu, "Quản lý bán hàng");
        }
    }

    /** Đang giao -> Đã giao (hoàn tất; COD ghi nhận thanh toán khi giao). */
    public void suattdgg(String mahd) {
        HoaDon hd = repo.findById(mahd).orElse(null);
        if (hd != null && "Đang giao".equals(hd.getTrangThai())) {
            String cu = hd.getTrangThai();
            hd.setTrangThai("Đã giao");
            // Giao thành công = hoàn tất thanh toán (COD nhận tiền khi giao)
            if (hd.getNgayThanhToan() == null) {
                hd.setNgayThanhToan(LocalDateTime.now());
            }
            repo.save(hd);
            thongBaoRealtimeService.trangThaiDonThayDoi(hd, cu, "Quản lý bán hàng");
        }
    }

    public Page<HoaDon> findByKhachHang(String maKH, Pageable pageable) {
        return repo.findByMaKhachHang_MaKHOrderByMaHoaDonDesc(maKH, pageable);
    }
}

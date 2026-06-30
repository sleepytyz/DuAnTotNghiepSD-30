package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.HoaDon;
import com.example.th06876_java202.Repository.HoaDonRepo;
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


    public List<HoaDon> findByTrangThai(String trangThai) {
        return repo.findByTrangThai(trangThai);
    }

    public Page<HoaDon> getALLDH(Pageable pageable) {
        return hoaDonRepo.findAll(pageable);
    }

    public List<HoaDon> getAllDH() {
        return hoaDonRepo.findAll(Sort.by(Sort.Direction.DESC, "ngayTao"));
    }

    public HoaDon findById(String id) {
        return hoaDonRepo.findById(id).orElse(null);
    }

    public List<HoaDon> getALLDHHUY() {
        return hoaDonRepo.findByTrangThai("Đã huỷ");
    }

    public Page<HoaDon> findByTrangThai(String trangThai, Pageable pageable) {
        return hoaDonRepo.findByTrangThai(trangThai, pageable);
    }

    public List<HoaDon> findAllByTrangThai(String trangThai) {
        return hoaDonRepo.findByTrangThai(trangThai);
    }

    // Thêm method mới: lấy hóa đơn theo trạng thái và loại bán
    public List<HoaDon> findByTrangThaiAndLoaiBan(String trangThai, String loaiBan) {
        return hoaDonRepo.findByTrangThaiAndLoaiBan(trangThai, loaiBan);
    }

    public List<HoaDon> getAll(){
        return hoaDonRepo.findAll();
    }




    public Page<HoaDon> searchByNgayTaodh(LocalDateTime ngay, LocalDateTime ngay2, Pageable pageable) {
        if (ngay != null && ngay2 != null) {
            return hoaDonRepo.findByNgayTaoBetween(ngay, ngay2, pageable);
        } else if (ngay != null) {
            return hoaDonRepo.findByNgayTaoAfter(ngay, pageable);
        } else if (ngay2 != null) {
            return hoaDonRepo.findByNgayTaoBefore(ngay2, pageable);
        }
        return hoaDonRepo.findAll(pageable);
    }

    public List<HoaDon> searchByNgayTaodh(LocalDateTime ngay, LocalDateTime ngay2) {
        if (ngay != null && ngay2 != null) {
            return hoaDonRepo.findByNgayTaoBetween(ngay, ngay2);
        } else if (ngay != null) {
            return hoaDonRepo.findByNgayTaoAfter(ngay);
        } else if (ngay2 != null) {
            return hoaDonRepo.findByNgayTaoBefore(ngay2);
        }
        return getAllDH();
    }

    public long countByTrangThai(String trangThai) {
        return hoaDonRepo.countByTrangThai(trangThai);
    }

    public void suatt(String mahd) {
        HoaDon hd = hoaDonRepo.findById(mahd).orElse(null);
        if (hd != null && "Chờ xác nhận".equals(hd.getTrangThai())) {
            hd.setTrangThai("Đã xác nhận");
            hoaDonRepo.save(hd);
        }
    }

    public HoaDon save(HoaDon hoaDon) {
        return hoaDonRepo.save(hoaDon);
    }

    public void suattdg(String mahd) {
        HoaDon hd = hoaDonRepo.findById(mahd).orElse(null);
        if (hd != null && "Đã xác nhận".equals(hd.getTrangThai())) {
            hd.setTrangThai("Đang giao");
            hoaDonRepo.save(hd);
        }
    }
    public Page<HoaDon> findByKhachHang(Integer maKH, Pageable pageable) {
        return repo.findByMaKhachHang_MaKHOrderByMaHoaDonDesc(maKH, pageable);
    }

}

    public void suattdgg(String mahd) {
        HoaDon hd = hoaDonRepo.findById(mahd).orElse(null);
        if (hd != null && "Đang giao".equals(hd.getTrangThai())) {
            hd.setTrangThai("Hoàn thành");
            hoaDonRepo.save(hd);
        }
    }
}
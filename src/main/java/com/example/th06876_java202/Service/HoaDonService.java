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

    public List<HoaDon> getAll(){
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

    public void suatt(String mahd) {
        HoaDon hd = repo.findById(mahd).orElse(null);
        if (hd != null && "Chờ xác nhận".equals(hd.getTrangThai())) {
            hd.setTrangThai("Đã xác nhận");
            repo.save(hd);
        }
    }

    public HoaDon save(HoaDon hoaDon) {
        return repo.save(hoaDon);
    }

    public void suattdg(String mahd) {
        HoaDon hd = repo.findById(mahd).orElse(null);
        if (hd != null && "Đã xác nhận".equals(hd.getTrangThai())) {
            hd.setTrangThai("Đang giao");
            repo.save(hd);
        }
    }

    public void suattdgg(String mahd) {
        HoaDon hd = repo.findById(mahd).orElse(null);
        if (hd != null && "Đã xác nhận".equals(hd.getTrangThai())) {
            hd.setTrangThai("Đã giao");
            repo.save(hd);
        }
    }
    public Page<HoaDon> findByKhachHang(String maKH, Pageable pageable) {
        return repo.findByMaKhachHang_MaKHOrderByMaHoaDonDesc(maKH, pageable);
    }

}



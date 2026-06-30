package com.example.th06876_java202.Service;

import com.example.th06876_java202.Repository.HoaDonRepo;
import com.example.th06876_java202.Entity.HoaDon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class HoaDonService {

    @Autowired
    private HoaDonRepo repo;

    public List<HoaDon> getAll() {
        return repo.getallHD();
    }

    public Page<HoaDon> getHoaDonKhac(Pageable pageable) {
        List<String> ds = List.of(
                "Chờ xác nhận",
                "Đã xác nhận",
                "Đang giao",
                "Đang xử lý"
        );
        return repo.findByTrangThaiNotIn(ds, pageable);
    }

    public HoaDon save(HoaDon hoaDon) {
        return repo.save(hoaDon);
    }

    public void delete(Integer id) {
        repo.deleteById(id);
    }

    public Optional<HoaDon> findById(Integer id) {
        return repo.findById(id);
    }

    public List<HoaDon> getALLDHHUY(){
        return repo.findByTrangThai();
    }

    public Page<HoaDon> searchByMa(Integer maHoaDon, Pageable pageable) {

        return repo.searchByMa(maHoaDon, pageable);
    }

    public Page<HoaDon> searchByNgayTao(LocalDate ngayTao, LocalDate ngayTao2, Pageable pageable) {
        return repo.findByNgayTao(ngayTao, ngayTao2, pageable);
    }

    public Page<HoaDon> findByTrangThai(String trangThai, Pageable pageable ){
        return repo.findByTrangThai(trangThai, pageable);
    }

    public int suatt(Integer mahd) {
        return repo.suatt(mahd);
    }

    public int suattdg(Integer mahd) {
        return repo.suattdg(mahd);
    }

    public int suattdgg(Integer mahd) {
        return repo.suattdgg(mahd);
    }

    public int huy(Integer mahd) {
        return repo.huy(mahd);
    }

    public Page<HoaDon> getALLDH(Pageable pageable) {
        return repo.getaddDH(pageable);
    }

    public Page<HoaDon> searchByMadh(Integer maHoaDon, Pageable pageable) {
        return repo.searchByMadh(maHoaDon, pageable);
    }

    public Page<HoaDon> searchByNgayTaodh(LocalDate ngayTao, LocalDate ngayTao2, Pageable pageable) {
        return repo.findByNgayTaodh(ngayTao, ngayTao2, pageable);
    }

    public Page<HoaDon> getallpage(Pageable pageable) {
        return repo.findAll(pageable);
    }

    public Page<HoaDon> findByKhachHang(Integer maKH, Pageable pageable) {
        return repo.findByMaKhachHang_MaKHOrderByMaHoaDonDesc(maKH, pageable);
    }

}

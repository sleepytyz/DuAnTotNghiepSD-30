package com.example.th06876_java202.Service;

import com.example.th06876_java202.Repository.HoaDonRepo;
import com.example.th06876_java202.Entity.HoaDon;
import org.springframework.beans.factory.annotation.Autowired;
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

    public HoaDon save(HoaDon hoaDon) {
        return repo.save(hoaDon);
    }

    public void delete(Integer id) {
        repo.deleteById(id);
    }

    public Optional<HoaDon> findById(Integer id) {
        return repo.findById(id);
    }

    public List<HoaDon> getALLDH(){
        return repo.getaddDH();
    }

    public List<HoaDon> getALLDHHUY(){
        return repo.findByTrangThai();
    }

    public List<HoaDon> searchByMa(Integer maHoaDon) {
        return repo.searchByMa(maHoaDon);
    }

    public List<HoaDon> searchByNgayTao(LocalDate ngayTao, LocalDate ngayTao2) {
        return repo.findByNgayTao(ngayTao, ngayTao2);
    }

    public List<HoaDon> findByTrangThai(String trangThai){
        return repo.findByTrangThai(trangThai);
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

    public List<HoaDon> searchByMadh(Integer maHoaDon) {
        return repo.searchByMadh(maHoaDon);
    }

    public List<HoaDon> searchByNgayTaodh(LocalDate ngayTao, LocalDate ngayTao2) {
        return repo.findByNgayTaodh(ngayTao, ngayTao2);
    }
}

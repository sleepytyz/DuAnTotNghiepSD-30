package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.GiamGia;
import com.example.th06876_java202.Repository.GiamGiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class GiamGiaService {

    @Autowired
    GiamGiaRepository giamGiaRepository;

    public List<GiamGia> getGiamGia() {
        return giamGiaRepository.findAll();
    }

    public List<GiamGia> getGiamGia1() {
        return giamGiaRepository.findSoLuongVoucher();
    }

    public void giamSoLuongVoucher(Integer id){
        giamGiaRepository.giamSoLuongVoucher(id);
    }

    public GiamGia save(GiamGia giamGia) {
        return giamGiaRepository.save(giamGia);
    }

    public Optional<GiamGia> getGiamGiaById(int id) {
        return giamGiaRepository.findById(id);
    }

    public void suatt(Integer magg){
        giamGiaRepository.updateGiamGia(magg);
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
}

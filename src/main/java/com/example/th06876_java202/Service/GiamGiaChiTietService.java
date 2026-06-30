package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.GiamGia;
import com.example.th06876_java202.Entity.GiamGiaChiTiet;
import com.example.th06876_java202.Entity.GiamGiaChiTietId;
import com.example.th06876_java202.Entity.KhachHang;
import com.example.th06876_java202.Repository.GiamGiaChiTietRepo;
import com.example.th06876_java202.Repository.GiamGiaRepository;
import com.example.th06876_java202.Repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class GiamGiaChiTietService {

    @Autowired
    GiamGiaRepository giamGiaRepository;

    @Autowired
    KhachHangRepository khachHangRepository;

    @Autowired
    GiamGiaChiTietRepo giamGiaChiTietRepo;

    public boolean existsById(GiamGiaChiTietId id) {
        return giamGiaChiTietRepo.existsById(id);
    }

    @Transactional
    public void updateTrangThaiToNgungHoatDong(Integer id) {
        GiamGia giamGia = giamGiaRepository.findById(id).orElse(null);
        if (giamGia != null) {
            giamGia.setTrangThai("Ngừng hoạt động");
            giamGiaRepository   .save(giamGia);

            giamGiaChiTietRepo.updateTrangThaiSuDungByMaGiamGia(id, 2);
        }
    }

    @Transactional
    public void ganVoucher(Integer maKhachHang, Integer maGiamGia) {
        GiamGiaChiTietId id = new GiamGiaChiTietId(maKhachHang, maGiamGia);

        if (giamGiaChiTietRepo.existsById(id)) return;
        KhachHang kh = khachHangRepository.getReferenceById(maKhachHang);
        GiamGia gg = giamGiaRepository.getReferenceById(maGiamGia);

        GiamGiaChiTiet chiTiet = new GiamGiaChiTiet();
        chiTiet.setId(id);
        chiTiet.setKhachHang(kh);
        chiTiet.setGiamGia(gg);
        chiTiet.setNgayNhan(LocalDateTime.now());
        chiTiet.setTrangThaiSuDung(0);

        giamGiaChiTietRepo.save(chiTiet);
        giamGiaChiTietRepo.flush();
    }
}

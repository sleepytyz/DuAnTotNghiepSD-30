package com.example.th06876_java202.Service;


import com.example.th06876_java202.Entity.HoaDon;
import com.example.th06876_java202.Entity.HoaDonChiTiet;
import com.example.th06876_java202.Repository.HoaDonChiTietRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HoaDonChiTietService {

    private final HoaDonChiTietRepository hoaDonChiTietRepository;

    public HoaDonChiTietService(HoaDonChiTietRepository hoaDonChiTietRepository) {
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
    }

    public HoaDonChiTiet findAll( Integer MaHoaDon, Integer MaSanPhamChiTiet) {
       return hoaDonChiTietRepository.getallsphd(MaHoaDon, MaSanPhamChiTiet);
    }

    public HoaDonChiTiet luu(HoaDonChiTiet hoaDonChiTiet) {
        return hoaDonChiTietRepository.save(hoaDonChiTiet);
    }

    public List<HoaDonChiTiet> luuTatCa(List<HoaDonChiTiet> list) {
        return hoaDonChiTietRepository.saveAll(list);
    }

    public List<HoaDonChiTiet> findById(Integer id) {
        return hoaDonChiTietRepository.getallsphd(id);
    }

    public void xoa(HoaDonChiTiet hdct) {
        hoaDonChiTietRepository.delete(hdct);
    }

    public List<HoaDonChiTiet> findByHoaDOn(HoaDon hoaDon) {
        return hoaDonChiTietRepository.findByMaHoaDon(hoaDon);
    }

}

package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.KhachHang;
import com.example.th06876_java202.Repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KhachHangService {
    @Autowired
    private KhachHangRepository khachHangRepo;

    public List<KhachHang> getKhachHang() {
        return khachHangRepo.findAll();
    }

    public KhachHang getKhachHangById(Integer maKH) {
        return khachHangRepo.findById(maKH).orElse(null);
    }

    public void save(KhachHang khachHang) {
        khachHangRepo.save(khachHang);
    }

    public void delete(Integer maKH) {
        khachHangRepo.deleteById(maKH);
    }

    public List<KhachHang> findBySdt( String sdt ) {
        return khachHangRepo.findBySdt(sdt);
    }

    public List<KhachHang> findByHangKH( String hang) {
        return khachHangRepo.findByHangKhachHang(hang);
    }

    public boolean existsBySoDienThoai(String soDienThoai) {
        return khachHangRepo.existsBySdt(soDienThoai);
    }

    public void updatett(Integer makh){
         khachHangRepo.updateTrangThai(makh);
    }

    public void them(KhachHang kh) {
        khachHangRepo.saveee(
                kh.getHoTen(),
                kh.getSdt(),
                kh.getDiaChi()
        );
    }


}

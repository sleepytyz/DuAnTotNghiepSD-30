package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.DiaChi;
import com.example.th06876_java202.Entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaChiRepo extends JpaRepository<com.example.th06876_java202.Entity.DiaChi, Integer> {

    List<DiaChi> findByKhachHang_MaKH(String maKH);

    List<DiaChi> findAllByMaDiaChi(Integer maDiaChi);

    List<DiaChi> findByKhachHang(KhachHang khachHang);

    DiaChi findByKhachHangAndDiaChiMacDinh(KhachHang khachHang, Boolean macDinh);
}

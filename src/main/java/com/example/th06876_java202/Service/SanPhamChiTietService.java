package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.*;
import com.example.th06876_java202.Repository.SanPhamChiTietRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class SanPhamChiTietService {

    @Autowired
    private MauSacService mauSacService;

    @Autowired
    private KichThuocService kichThuocService;

    private final SanPhamChiTietRepository sanPhamChiTietRepository;

    public SanPhamChiTietService(SanPhamChiTietRepository sanPhamChiTietRepository) {
        this.sanPhamChiTietRepository = sanPhamChiTietRepository;
    }

    public Page<SanPhamChiTiet> getall(Pageable pageable) {
        return sanPhamChiTietRepository.findAll(pageable);
    }

    public List<SanPhamChiTiet> getalll() {
        return sanPhamChiTietRepository.findAll();
    }

    public SanPhamChiTiet them(SanPhamChiTiet sanPhamChiTiet) {
        return sanPhamChiTietRepository.save(sanPhamChiTiet);
    }

    public Optional<SanPhamChiTiet> findbyId(Integer id) {
        return sanPhamChiTietRepository.findById(id);
    }


    public Double gia(){
        return sanPhamChiTietRepository.findMaxGiaBan();
    }

    public Integer gi1a(){
        return sanPhamChiTietRepository.sluong();
    }
    public Page<SanPhamChiTiet> getByMauSac(String maSac, Pageable pageable) {
        return sanPhamChiTietRepository.findByMauSac_MaMauSac(maSac, pageable);
    }

    public Page<SanPhamChiTiet> getBySize(String size, Pageable pageable) {
        return sanPhamChiTietRepository.findByKichThuoc_MaKichThuoc(size, pageable);
    }

    public Page<SanPhamChiTiet> getByTT(String tt, Pageable pageable) {
        return sanPhamChiTietRepository.locTheoTrangThaiHienThi(tt, pageable);
    }

    public Page<SanPhamChiTiet> getBygia(BigDecimal gm, BigDecimal gm2, Pageable pageable) {
        return sanPhamChiTietRepository.findByGiaBanAndGiaBan(gm, gm2, pageable);
    }

        public int suaSanPham2(int maSanPham) {
            return sanPhamChiTietRepository.updateTrangThai(maSanPham);
        }

    public int updateTrangThai(int id, String trangThai) {
        return sanPhamChiTietRepository.updateTrangThaii(id, trangThai);
    }

    public int suaSanPham3(int maSanPham) {
        return sanPhamChiTietRepository.updateTrangThaiii(maSanPham);
    }

    public List<String> getSize() {
        return sanPhamChiTietRepository.findAllSize();
    }

    public List<String> getMsac() {
        return sanPhamChiTietRepository.findAllMauSac();
    }

    public List<SanPhamChiTiet> getByMauSac(String ms) {
        return sanPhamChiTietRepository.findByMauSac(ms);
    }

    public List<SanPhamChiTiet> getBySize(String ms) {
        return sanPhamChiTietRepository.findBySize(ms);
    }

    public List<SanPhamChiTiet> getByTT(String ms) {
        return sanPhamChiTietRepository.findByTT(ms);
    }

    public List<SanPhamChiTiet> getallsp(Integer maSanPham) {
        return sanPhamChiTietRepository.findByMaSanPham(maSanPham);
    }

    public void capNhatTrangThaii(SanPhamChiTiet spct) {
        Integer soLuong = spct.getSoLuongTon();
        if (soLuong == null || soLuong <= 0) {
            spct.setTrangThai("Hết hàng");
        } else if (soLuong < 10) {
            spct.setTrangThai("Sắp hết");
        } else {
            spct.setTrangThai("Còn hàng");
        }
        spct.setNgayCapNhat(LocalDate.now());
    }

    public List<SanPhamChiTiet> findsp(List<Integer> listMaSanPham) {
        return sanPhamChiTietRepository.findByidmasp(listMaSanPham);
    }
}

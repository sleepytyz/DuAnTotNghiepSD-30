package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.SanPhamChiTiet;
import com.example.th06876_java202.Repository.SanPhamChiTietRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class SanPhamChiTietService {

    private final SanPhamChiTietRepository sanPhamChiTietRepository;

    public SanPhamChiTietService(SanPhamChiTietRepository sanPhamChiTietRepository) {
        this.sanPhamChiTietRepository = sanPhamChiTietRepository;
    }

    public List<SanPhamChiTiet> getall(){
        return sanPhamChiTietRepository.findAll();
    }

    public SanPhamChiTiet them(SanPhamChiTiet sanPhamChiTiet) {
        return sanPhamChiTietRepository.save(sanPhamChiTiet);
    }

    public Optional<SanPhamChiTiet> findbyId(Integer id) {
        return sanPhamChiTietRepository.findById(id);
    }


    public List<SanPhamChiTiet> getBySanPhamVaMau(
            Integer maSP,
            String mauSac){

        return sanPhamChiTietRepository
                .findBySanPham_MaSanPhamAndMauSac_TenMauSac(
                        maSP,
                        mauSac);
    }

    public List<SanPhamChiTiet> getByHinhAnh(){
        return sanPhamChiTietRepository.findAllSanPham();
    }

    public List<SanPhamChiTiet> getDistinctSanPhamMau() {

        List<SanPhamChiTiet> all = sanPhamChiTietRepository.findAll();

        Map<String, SanPhamChiTiet> map = new LinkedHashMap<>();

        for (SanPhamChiTiet spct : all) {

            String key =
                    spct.getSanPham().getMaSanPham()
                            + "_"
                            + spct.getMauSac();

            if (!map.containsKey(key)) {
                map.put(key, spct);
            }
        }

        return new ArrayList<>(map.values());
    }

    public List<SanPhamChiTiet> getByMauSac(String maSac) {
        return sanPhamChiTietRepository.findByMauSac(maSac);
    }

    public List<SanPhamChiTiet> getBySize(String size) {
        return sanPhamChiTietRepository.findBySize(size);
    }

    public List<SanPhamChiTiet> getByTT(String tt) {
        return sanPhamChiTietRepository.locTheoTrangThaiHienThi(tt);
    }

    public List<SanPhamChiTiet> getBygia(BigDecimal gm, BigDecimal gm2) {
        return sanPhamChiTietRepository.findByGiaBanAndGiaBan(gm,gm2);
    }

    public int suaSanPham2(int maSanPham) {
        return sanPhamChiTietRepository.updateTrangThai(maSanPham);
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


    public void capNhatTrangThaii(SanPhamChiTiet spct) {
        Integer soLuong = spct.getSoLuongTon();

        if (soLuong == null || soLuong <= 0) {
            spct.setTrangThai("Hết hàng");
        } else if (soLuong <= 10) {
            spct.setTrangThai("Sắp hết");
        } else {
            spct.setTrangThai("Còn hàng");
        }

        spct.setNgayCapNhat(LocalDate.now());
    }

}

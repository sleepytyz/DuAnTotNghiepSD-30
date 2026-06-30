package com.example.th06876_java202.Service;
import com.example.th06876_java202.Entity.ChiTietDotGiamGia;
import com.example.th06876_java202.Entity.DotGiamGia;
import com.example.th06876_java202.Entity.SanPham;
import com.example.th06876_java202.Entity.SanPhamChiTiet;
import com.example.th06876_java202.Repository.ChiTietDotGiamGiaRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChiTietDotGiamGiaService {

    @Autowired
    private ChiTietDotGiamGiaRepo repo;

    @Autowired
    private SanPhamChiTietService sanPhamChiTietRepo;

    @Autowired
    private DotGiamGiaService dotGiamGiaRepo;

    @Autowired
    private SanPhamService sanPhamRepo;

    public List<ChiTietDotGiamGia> getAll() {
        return repo.findAll();
    }
    public ChiTietDotGiamGia getById(Integer id) {
        return repo.getById(id);
    }

    public void save(ChiTietDotGiamGia ct) {
        repo.save(ct);
    }
    public void delete(Integer id){
        repo.deleteById(id);
    }

    public boolean exists(
            Integer maGiamGia,
            Integer maSanPham) {

        return repo
                .existsByDotGiamGia_MaGiamGiaAndSanPham_MaSanPham(
                        maGiamGia,
                        maSanPham
                );
    }

    public Page<ChiTietDotGiamGia> getAllPage(Pageable pageable) {
        return repo.findAll(pageable);
    }

    public List<ChiTietDotGiamGia> filterByMaGiamGia(String maGiamGia) {
        return repo.filterByDotGiamGia(maGiamGia);
    }

    public void saveAllDetails(Integer maGiamGia, Integer maSanPham, List<Integer> listMaSanPhamChiTiet) {
        if (maGiamGia == null || maSanPham == null || listMaSanPhamChiTiet == null) {
            System.err.println("Lỗi: Dữ liệu đầu vào (maGiamGia, maSanPham, hoặc list) bị null!");
            return;
        }
        var dotGiamGia = dotGiamGiaRepo.findById(maGiamGia).orElse(null);
        var sanPham = sanPhamRepo.findById(maSanPham).orElse(null);

        if (dotGiamGia == null || sanPham == null) {
            System.err.println("Lỗi: Không tìm thấy DotGiamGia hoặc SanPham trong CSDL");
            return;
        }
        for (Integer maCT : listMaSanPhamChiTiet) {
            if (maCT == null) continue;
            boolean exists = repo.existsByDotGiamGia_MaGiamGiaAndSanPhamChiTiet_MaSanPhamChiTiet(maGiamGia, maCT);
            if (!exists) {
                var chiTietSP = sanPhamChiTietRepo.findbyId(maCT).orElse(null);

                if (chiTietSP != null) {
                    ChiTietDotGiamGia ct = new ChiTietDotGiamGia();
                    ct.setDotGiamGia(dotGiamGia);
                    ct.setSanPham(sanPham);
                    ct.setSanPhamChiTiet(chiTietSP);
                    repo.save(ct);
                } else {
                    System.err.println("Cảnh báo: Không tìm thấy SanPhamChiTiet với ID: " + maCT);
                }
            }
        }
    }

    public List<Integer> getSanPhamByDot(Integer idDot) {
        return repo.findSanPhamByDot(idDot);
    }

    public List<Integer> getSanPhamChiTietByDot(Integer idDot) {
        return repo.findSanPhamChiTietByDot(idDot);
    }

}
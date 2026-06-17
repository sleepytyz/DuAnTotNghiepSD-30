package com.example.th06876_java202.Service;
import com.example.th06876_java202.Entity.ChiTietDotGiamGia;
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
}
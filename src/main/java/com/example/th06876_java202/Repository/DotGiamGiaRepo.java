package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.DotGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DotGiamGiaRepo extends JpaRepository<DotGiamGia, Integer> {

    @Query(value = "select * from DotGiamGia dgg inner join ChiTietDotGiamGia ctdgg on dgg.MaGiamGia = ctdgg.MaGiamGia where ctdgg.MaSanPham = ? and dgg.TrangThai = 1", nativeQuery = true)
    List<DotGiamGia> findBySanPham(Integer maSanPham);

}

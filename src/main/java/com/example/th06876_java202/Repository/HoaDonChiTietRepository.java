package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Integer> {

    @Query(value = "select * from HoaDonChiTiet where MaHoaDon = ?1 and MaSanPhamChiTiet = ?2", nativeQuery = true)
    HoaDonChiTiet getallsphd(Integer MaHoaDon, Integer MaSanPhamChiTiet);

    @Query( value = "select * from HoaDonChiTiet where MaHoaDon = ?", nativeQuery = true)
    List<HoaDonChiTiet> getallsphd(Integer MaHoaDon);

}

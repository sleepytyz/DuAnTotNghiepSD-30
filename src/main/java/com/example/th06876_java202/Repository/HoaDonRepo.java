package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoaDonRepo extends JpaRepository<HoaDon, Integer> {
    // Tìm kiếm gần đúng bằng cách cast mã hóa đơn sang chuỗi kí tự
    @Query("SELECT h FROM HoaDon h WHERE CAST(h.maHoaDon AS string) LIKE %:keyword%")
    List<HoaDon> searchByMa(@Param("keyword") String keyword);
}

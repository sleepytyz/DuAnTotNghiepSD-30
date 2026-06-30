package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.ChamCong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ChamCongRepository extends JpaRepository<ChamCong, Integer> {
    
    ChamCong findByMaChamCong(int maChamCong);

    List<ChamCong> findByNhanVien_HoTenContaining(String hoTen);
    
    List<ChamCong> findByNgayChamCongBetween(LocalDate tuNgay, LocalDate denNgay);
    
    List<ChamCong> findByNhanVien_MaNhanVienAndNgayChamCong(Integer maNhanVien, LocalDate ngay);
    
    List<ChamCong> findByNhanVien_MaNhanVienAndNgayChamCongBetween(Integer maNhanVien, LocalDate tuNgay, LocalDate denNgay);
    
    @Query("SELECT DISTINCT cc.nhanVien.maNhanVien FROM ChamCong cc WHERE cc.ngayChamCong BETWEEN :tuNgay AND :denNgay")
    List<Integer> findMaNhanVienCoLichTrongKhoang(@Param("tuNgay") LocalDate tuNgay, @Param("denNgay") LocalDate denNgay);
    
    @Query("SELECT cc FROM ChamCong cc WHERE cc.ngayChamCong BETWEEN :tuNgay AND :denNgay AND cc.trangThai = true")
    List<ChamCong> findLichSapToiTrongKhoang(@Param("tuNgay") LocalDate tuNgay, @Param("denNgay") LocalDate denNgay);
    
    @Query("SELECT cc FROM ChamCong cc WHERE cc.ngayChamCong BETWEEN :tuNgay AND :denNgay AND cc.trangThai = false")
    List<ChamCong> findLichDaQuaTrongKhoang(@Param("tuNgay") LocalDate tuNgay, @Param("denNgay") LocalDate denNgay);
}
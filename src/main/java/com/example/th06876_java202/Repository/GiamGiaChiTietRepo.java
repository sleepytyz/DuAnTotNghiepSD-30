package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.GiamGiaChiTiet;
import com.example.th06876_java202.Entity.GiamGiaChiTietId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GiamGiaChiTietRepo extends JpaRepository<GiamGiaChiTiet, GiamGiaChiTietId> {

    @Query("SELECT ct.id.maKhachHang FROM GiamGiaChiTiet ct WHERE ct.id.maGiamGia = :maGiamGia")
    List<Integer> findMaKhachHangByMaGiamGia(@Param("maGiamGia") Integer maGiamGia);

    @Modifying
    @Query("DELETE FROM GiamGiaChiTiet ct WHERE ct.id.maGiamGia = :maGiamGia")
    void deleteByMaGiamGia(@Param("maGiamGia") Integer maGiamGia);

    @Modifying
    @Query("UPDATE GiamGiaChiTiet ct SET ct.trangThaiSuDung = :trangThai WHERE ct.id.maGiamGia = :maGiamGia")
    void updateTrangThaiSuDungByMaGiamGia(@Param("maGiamGia") Integer maGiamGia, @Param("trangThai") int trangThai);

    @Modifying
    @Query("UPDATE GiamGiaChiTiet ct SET ct.trangThaiSuDung = :trangThai WHERE ct.id.maGiamGia = :maGiamGia")
    void updateTrangThaiByMaGiamGia(@Param("maGiamGia") Integer maGiamGia, @Param("trangThai") int trangThai);

    long countByGiamGia_MaGiamGia(Integer maGiamGia);

}

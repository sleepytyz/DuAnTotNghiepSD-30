package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.DanhMucSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface DanhMucSanPhamRepository extends JpaRepository<DanhMucSanPham, Integer> {

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE DanhMucSanPham d SET d.trangThai = false WHERE d.maDanhMuc = ?1")
    int updateTrangThai(int maDanhMuc);

    boolean existsByTenDanhMuc(String tenDanhMuc);

}

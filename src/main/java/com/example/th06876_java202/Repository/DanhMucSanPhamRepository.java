package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.DanhMucSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DanhMucSanPhamRepository extends JpaRepository<DanhMucSanPham, Integer> {

    @Modifying
    @Query(value = "update DanhMucSanPham set TrangThai = 0 where MaDanhMuc = ?")
    int updateTrangThai(int maDanhMuc);

    boolean ktraten( String tendanhmuc );

}

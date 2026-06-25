package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.KichThuoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KichThuocRepository extends JpaRepository<KichThuoc, Integer> {

    boolean existsByTenKichThuoc(String tenKichThuoc);

    Page<KichThuoc> findAllByOrderByMaKichThuocDesc(Pageable pageable);

    List<KichThuoc> findAllByOrderByTenKichThuocAsc();


}

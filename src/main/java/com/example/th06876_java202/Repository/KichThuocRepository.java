package com.example.th06876_java202.Repository;

import com.example.th06876_java202.Entity.KichThuoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KichThuocRepository extends JpaRepository<KichThuoc, Integer> {

    boolean existsByTenKichThuoc(String tenKichThuoc);

}

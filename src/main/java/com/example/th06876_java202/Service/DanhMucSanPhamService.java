package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.DanhMucSanPham;
import com.example.th06876_java202.Repository.DanhMucSanPhamRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DanhMucSanPhamService {
    private final DanhMucSanPhamRepository danhMucSanPhamRepository;

    public DanhMucSanPhamService(DanhMucSanPhamRepository danhMucSanPhamRepository) {
        this.danhMucSanPhamRepository = danhMucSanPhamRepository;
    }

    public List<DanhMucSanPham> getAll() {
        return danhMucSanPhamRepository.findAll();
    }

    public DanhMucSanPham them ( DanhMucSanPham danhMucSanPham ) {
        return danhMucSanPhamRepository.save(danhMucSanPham);
    }
    public Optional<DanhMucSanPham> findById (int id ) {
        return danhMucSanPhamRepository.findById(id);
    }

    public int updatett(int id){
        return danhMucSanPhamRepository.updateTrangThai(id);
    }

    public boolean ktraten( String tendanhmuc ){
        return danhMucSanPhamRepository.existsByTenDanhMuc(tendanhmuc);
    }

}

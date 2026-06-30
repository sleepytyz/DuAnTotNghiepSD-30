package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.DanhMucSanPham;
import com.example.th06876_java202.Entity.ThuongHieu;
import com.example.th06876_java202.Repository.DanhMucSanPhamRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public void updatett(int id){
        danhMucSanPhamRepository.updateTrangThai(id);
    }

    public boolean ktraten(String tendanhmuc){
        return danhMucSanPhamRepository.existsByTenDanhMuc(tendanhmuc);
    }

    public DanhMucSanPham doiTrangThai(Integer id) {
        Optional<DanhMucSanPham> optional = danhMucSanPhamRepository.findById(id);
        if (optional.isPresent()) {
            DanhMucSanPham dm = optional.get();
            dm.setTrangThai(!dm.isTrangThai());
            return danhMucSanPhamRepository.save(dm);
        }
        return null;
    }

    public Page<DanhMucSanPham> getallpage(Pageable pageable) {
        return danhMucSanPhamRepository.findAllByOrderByMaDanhMucDesc(pageable);
    }

}

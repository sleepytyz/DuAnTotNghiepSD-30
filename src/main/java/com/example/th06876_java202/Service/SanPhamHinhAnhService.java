package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.SanPhamHinhAnh;
import com.example.th06876_java202.Repository.SanPhamHinhAnhRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SanPhamHinhAnhService {

    private final SanPhamHinhAnhRepository repository;

    public SanPhamHinhAnhService(SanPhamHinhAnhRepository repository) {
        this.repository = repository;
    }

    public List<SanPhamHinhAnh> getAll() {
        return repository.findAll();
    }

    public SanPhamHinhAnh save(SanPhamHinhAnh anh) {
        return repository.save(anh);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }

    public Optional<SanPhamHinhAnh> findById(Integer id) {
        return repository.findById(id);
    }

    public List<SanPhamHinhAnh> getBySPCT(Integer maSPCT) {
        return repository.findBySanPhamChiTiet_MaSanPhamChiTiet(maSPCT);
    }

    public List<SanPhamHinhAnh> getDistinctSanPhamMau(){
        return repository.findDistinctSanPhamMau();
    }

    @Transactional
    public void setAnhChinh(Integer maHinhAnh){

        SanPhamHinhAnh anhMoi =
                repository.findById(maHinhAnh)
                        .orElseThrow();

        Integer maSPCT =
                anhMoi.getSanPhamChiTiet()
                        .getMaSanPhamChiTiet();

        List<SanPhamHinhAnh> dsAnh =
                repository.findBySanPhamChiTiet_MaSanPhamChiTiet(maSPCT);

        for(SanPhamHinhAnh anh : dsAnh){
            anh.setLaAnhChinh(false);
        }

        repository.saveAll(dsAnh);

        anhMoi.setLaAnhChinh(true);
        repository.save(anhMoi);
    }
}

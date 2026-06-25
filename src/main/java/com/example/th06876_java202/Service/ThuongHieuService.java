package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.ChatLieu;
import com.example.th06876_java202.Entity.DanhMucSanPham;
import com.example.th06876_java202.Entity.KieuGiay;
import com.example.th06876_java202.Entity.ThuongHieu;
import com.example.th06876_java202.Repository.ThuongHieuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ThuongHieuService {

    @Autowired
    private ThuongHieuRepository thuongHieuRepository;

    public List<ThuongHieu> findAll() {
        return thuongHieuRepository.findAll();
    }

    public ThuongHieu them(ThuongHieu thuongHieu) {
        return thuongHieuRepository.save(thuongHieu);
    }

    public Optional<ThuongHieu> findById(Integer id) {
        return thuongHieuRepository.findById(id);
    }

    public boolean ktraten(String ten){
        return thuongHieuRepository.existsByTenThuongHieu(ten);
    }

    public ThuongHieu doiTrangThai(Integer id) {
        Optional<ThuongHieu> optional = thuongHieuRepository.findById(id);
        if (optional.isPresent()) {
            ThuongHieu dm = optional.get();
            dm.setTrangThai(!dm.isTrangThai());
            return thuongHieuRepository.save(dm);
        }
        return null;
    }

    public Page<ThuongHieu> getallpage(Pageable pageable) {
        return thuongHieuRepository.findAllByOrderByMaThuongHieuDesc(pageable);
    }

}

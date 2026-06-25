package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.DanhMucSanPham;
import com.example.th06876_java202.Entity.KieuGiay;
import com.example.th06876_java202.Repository.KieuGiayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class KieuGiayService {

    @Autowired
    private KieuGiayRepository kieuGiayRepository;

    public List<KieuGiay> findAll() {
        return kieuGiayRepository.findAll();
    }

    public KieuGiay them(KieuGiay kieuGiay) {
        return kieuGiayRepository.save(kieuGiay);
    }

    public KieuGiay doiTrangThai(Integer id) {
        Optional<KieuGiay> optional = kieuGiayRepository.findById(id);
        if (optional.isPresent()) {
            KieuGiay dm = optional.get();
            dm.setTrangThai(!dm.isTrangThai());
            return kieuGiayRepository.save(dm);
        }
        return null;
    }

    public Optional<KieuGiay> findById(Integer id) {
        return kieuGiayRepository.findById(id);
    }

    public Page<KieuGiay> getallpage(Pageable pageable) {
        return kieuGiayRepository.findAllByOrderByMaKieuGiayDesc(pageable);
    }

}

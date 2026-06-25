package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.DanhMucSanPham;
import com.example.th06876_java202.Entity.MauSac;
import com.example.th06876_java202.Entity.ThuongHieu;
import com.example.th06876_java202.Repository.MauSacRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MauSacService {

    private final MauSacRepository mauSacRepository;

    public MauSacService(MauSacRepository mauSacRepository) {
        this.mauSacRepository = mauSacRepository;
    }

    public List<MauSac> findAll() {
        return mauSacRepository.findAll();
    }

    public MauSac add(MauSac mauSac) {
        return mauSacRepository.save(mauSac);
    }

    public boolean existbyten(String ten) {
        return mauSacRepository.existsByTenMauSac(ten);
    }

    public Optional<MauSac> findById(int id) {
        return mauSacRepository.findById(id);
    }

    public MauSac doiTrangThai(Integer id) {
        Optional<MauSac> optional = mauSacRepository.findById(id);
        if (optional.isPresent()) {
            MauSac dm = optional.get();
            dm.setTrangThai(!dm.isTrangThai());
            return mauSacRepository.save(dm);
        }
        return null;
    }

    public Page<MauSac> getallpage(Pageable pageable) {
        return mauSacRepository.findAllByOrderByMaMauSacDesc(pageable);
    }

}

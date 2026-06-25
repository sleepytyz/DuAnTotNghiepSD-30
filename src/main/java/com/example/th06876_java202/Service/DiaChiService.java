package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.DiaChi;
import com.example.th06876_java202.Repository.DiaChiRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DiaChiService {

    @Autowired
    private DiaChiRepo diaChiRepo;

    @Transactional
    public void save(DiaChi diaChi) {
        if (Boolean.TRUE.equals(diaChi.getDiaChiMacDinh())) {
            List<DiaChi> list = diaChiRepo.findByKhachHang_MaKH(diaChi.getKhachHang().getMaKH());
            for (DiaChi dc : list) {
                dc.setDiaChiMacDinh(false);
            }
            diaChiRepo.saveAll(list);
        }
        diaChiRepo.save(diaChi);
    }

    public List<DiaChi> findByKhachHang(Integer maKH) {
        return diaChiRepo.findByKhachHang_MaKH(maKH);
    }

    public void delete(Integer id) {
        diaChiRepo.deleteById(id);
    }

    public Optional<DiaChi> findById(Integer id) {
        return diaChiRepo.findById(id);
    }

}
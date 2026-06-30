package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.DotGiamGia;
import com.example.th06876_java202.Repository.DotGiamGiaRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class DotGiamGiaService {
    @Autowired
    private DotGiamGiaRepo dotGiamGiaRepo;

    public List<DotGiamGia> getAll() {
        return dotGiamGiaRepo.findAll();
    }

    public DotGiamGia getById(Integer id) {
        return dotGiamGiaRepo.getById(id);
    }

    public DotGiamGia save(DotGiamGia dotGiamGia) {
         return dotGiamGiaRepo.save(dotGiamGia);
    }
    public void delete(Integer id) {
        dotGiamGiaRepo.delete(getById(id));
    }
    public void update(DotGiamGia dotGiamGia) {
        dotGiamGiaRepo.save(dotGiamGia);
    }

    public List<DotGiamGia> getBymasp(Integer masp) {
        return dotGiamGiaRepo.findBySanPham(masp);
    }

    public void suaa(Integer id) {
        dotGiamGiaRepo.updateTrangThai(id);
    }

    public Page<DotGiamGia> filterPaging(String keyword,
                                         String trangThai,
                                         LocalDate tuNgay,
                                         LocalDate denNgay,
                                         Pageable pageable) {
        return dotGiamGiaRepo.filterPaging(keyword, trangThai, tuNgay, denNgay, pageable);
    }

    public Optional<DotGiamGia> findById(Integer id) {
        return dotGiamGiaRepo.findById(id);
    }

    public Page<DotGiamGia> getAllPaging(Pageable pageable) {
        return dotGiamGiaRepo.findAllByOrderByMaGiamGiaDesc(pageable);
    }

    public void capNhatTrangThai() {

        List<DotGiamGia> list = dotGiamGiaRepo.findAll();
        LocalDate today = LocalDate.now();

        for (DotGiamGia dgg : list) {

            if ("Ngừng hoạt động".equals(dgg.getTrangThai())) {
                continue;
            }

            if (today.isBefore(dgg.getNgayBatDau())) {
                dgg.setTrangThai("Sắp hoạt động");

            } else if (!today.isAfter(dgg.getNgayKetThuc())) {
                dgg.setTrangThai("Hoạt động");

            } else {
                dgg.setTrangThai("Ngừng hoạt động");
            }

            dotGiamGiaRepo.save(dgg);
        }
    }

}

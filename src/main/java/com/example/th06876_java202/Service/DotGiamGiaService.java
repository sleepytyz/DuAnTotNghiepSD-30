package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.DotGiamGia;
import com.example.th06876_java202.Repository.DotGiamGiaRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public void save(DotGiamGia dotGiamGia) {
        dotGiamGiaRepo.save(dotGiamGia);
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

}

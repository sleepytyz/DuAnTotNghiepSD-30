package com.example.duantotnghiep.service;

import com.example.duantotnghiep.model.DotGiamGia;
import com.example.duantotnghiep.repository.DotGiamGiaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DotGiamGiaService {
    private final DotGiamGiaRepository dotGiamGiaRepository;

    public DotGiamGiaService(DotGiamGiaRepository dotGiamGiaRepository) {
        this.dotGiamGiaRepository = dotGiamGiaRepository;
    }

    public List<DotGiamGia> fetchAll() {
        return this.dotGiamGiaRepository.findAll();
    }

    public List<DotGiamGia> searchByTen(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return fetchAll();
        }
        return this.dotGiamGiaRepository.findByTenGiamGiaContainingIgnoreCase(keyword);
    }

    public void create(DotGiamGia dotGiamGia) {
        this.dotGiamGiaRepository.save(dotGiamGia);
    }

    public DotGiamGia findById(int id) {
        Optional<DotGiamGia> optional = this.dotGiamGiaRepository.findById(id);
        return optional.orElse(null);
    }

    public void update(DotGiamGia dotGiamGia) {
        this.dotGiamGiaRepository.save(dotGiamGia);
    }

    public void deleteById(int id) {
        this.dotGiamGiaRepository.deleteById(id);
    }
}

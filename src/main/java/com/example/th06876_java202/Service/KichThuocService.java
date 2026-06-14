package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.KichThuoc;
import com.example.th06876_java202.Repository.KichThuocRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class KichThuocService {

    private final KichThuocRepository kichThuocRepository;

    public KichThuocService(KichThuocRepository kichThuocRepository) {
        this.kichThuocRepository = kichThuocRepository;
    }

    public List<KichThuoc> getAllKichThuoc() {
        return kichThuocRepository.findAll();
    }

    public KichThuoc add(KichThuoc kichThuoc) {
        return kichThuocRepository.save(kichThuoc);
    }

    public Optional<KichThuoc> getKichThuocById(int id) {
        return kichThuocRepository.findById(id);
    }

    public boolean existsKichThuocByTenKichThuoc(String ten) {
        return existsKichThuocByTenKichThuoc(ten);
    }

}

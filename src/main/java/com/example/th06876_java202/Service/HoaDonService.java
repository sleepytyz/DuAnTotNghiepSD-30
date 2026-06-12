package com.example.th06876_java202.Service;

import com.example.th06876_java202.Repository.HoaDonRepo;
import com.example.th06876_java202.Entity.HoaDon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HoaDonService {

    @Autowired
    private HoaDonRepo repo;

    public List<HoaDon> getAll() {
        return repo.findAll();
    }

    public HoaDon save(HoaDon hoaDon) {
        return repo.save(hoaDon);
    }

    public void delete(Integer id) {
        repo.deleteById(id);
    }

    public Optional<HoaDon> findById(Integer id) {
        return repo.findById(id);
    }

    public List<HoaDon> searchByMa(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return repo.searchByMa(keyword);
        }
        return repo.findAll();
    }
}

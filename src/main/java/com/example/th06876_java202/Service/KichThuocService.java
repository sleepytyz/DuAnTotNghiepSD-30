package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.KichThuoc;
import com.example.th06876_java202.Repository.KichThuocRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class KichThuocService {

    private final KichThuocRepository kichThuocRepository;
    private final Random random = new Random();

    public KichThuocService(KichThuocRepository kichThuocRepository) {
        this.kichThuocRepository = kichThuocRepository;
    }

    public List<KichThuoc> getall() {
        return kichThuocRepository.findAllByOrderByTenKichThuocAsc();
    }

    public List<KichThuoc> getAllKichThuoc() {
        return kichThuocRepository.findAll();
    }

    public KichThuoc add(KichThuoc kichThuoc) {
        return kichThuocRepository.save(kichThuoc);
    }

    public Optional<KichThuoc> getKichThuocById(String id) {
        return kichThuocRepository.findById(id);
    }

    public String generateMaKichThuoc() {
        String code;
        boolean exists;
        do {
            int randomNumber = 1000 + random.nextInt(9000); // Tạo số từ 1000-9999
            code = "KT" + randomNumber;
            exists = kichThuocRepository.existsById(code);
        } while (exists);
        return code;
    }

    public String normalizeTenKichThuoc(String ten) {
        if (ten == null) return "";
        // 1. Xóa khoảng trắng đầu và cuối
        ten = ten.trim();
        // 2. Giữa các từ chỉ 1 khoảng trắng
        ten = ten.replaceAll("\\s+", " ");
        return ten;
    }

    private String normalizeForCompare(String ten) {
        if (ten == null) return "";
        // 1. Xóa khoảng trắng đầu và cuối
        ten = ten.trim();
        // 2. Xóa tất cả khoảng trắng
        ten = ten.replaceAll("\\s+", "");
        return ten;
    }

    public boolean existsByTenKichThuoc(String tenKichThuoc) {
        if (tenKichThuoc == null) return false;

        String normalizedInput = normalizeForCompare(tenKichThuoc);

        List<KichThuoc> all = kichThuocRepository.findAll();
        for (KichThuoc kt : all) {
            String existingName = normalizeForCompare(kt.getTenKichThuoc());
            if (existingName.equalsIgnoreCase(normalizedInput)) {
                System.out.println("🔍 Tìm thấy trùng: '" + kt.getTenKichThuoc() + "' với '" + tenKichThuoc + "'");
                return true;
            }
        }
        return false;
    }

    public Page<KichThuoc> getallpage(Pageable pageable) {
        return kichThuocRepository.findAllByOrderByNgayTaoDesc(pageable);
    }

    public KichThuoc doiTrangThai(String id) {
        Optional<KichThuoc> optional = kichThuocRepository.findById(id);
        if (optional.isPresent()) {
            KichThuoc dm = optional.get();
            dm.setTrangThai(!dm.isTrangThai());
            return kichThuocRepository.save(dm);
        }
        return null;
    }
}
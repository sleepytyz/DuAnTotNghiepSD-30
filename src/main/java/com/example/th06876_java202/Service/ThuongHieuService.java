package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.ThuongHieu;
import com.example.th06876_java202.Repository.ThuongHieuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class ThuongHieuService {

    @Autowired
    private ThuongHieuRepository thuongHieuRepository;

    private final Random random = new Random();

    public List<ThuongHieu> findAll() {
        return thuongHieuRepository.findAll();
    }

    public ThuongHieu them(ThuongHieu thuongHieu) {
        return thuongHieuRepository.save(thuongHieu);
    }

    public Optional<ThuongHieu> findById(String id) {
        return thuongHieuRepository.findById(id);
    }

    public String generateMaThuongHieu() {
        String code;
        boolean exists;
        do {
            int randomNumber = 1000 + random.nextInt(9000); // Tạo số từ 1000-9999
            code = "TH" + randomNumber;
            exists = thuongHieuRepository.existsById(code);
        } while (exists);
        return code;
    }

    public String normalizeTenThuongHieu(String ten) {
        if (ten == null) return "";
        ten = ten.trim();
        ten = ten.replaceAll("\\s+", " ");
        String[] words = ten.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return result.toString().trim();
    }

    private String normalizeForCompare(String ten) {
        if (ten == null) return "";
        ten = ten.trim();
        ten = ten.replaceAll("\\s+", " ");
        ten = ten.toLowerCase();
        ten = removeDiacritics(ten);
        return ten;
    }

    private String removeDiacritics(String text) {
        if (text == null) return "";
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }

    public boolean ktraten(String tenThuongHieu) {
        if (tenThuongHieu == null) return false;

        String normalizedInput = normalizeForCompare(tenThuongHieu);

        List<ThuongHieu> all = thuongHieuRepository.findAll();
        for (ThuongHieu th : all) {
            String existingName = normalizeForCompare(th.getTenThuongHieu());
            if (existingName.equals(normalizedInput)) {
                System.out.println("🔍 Tìm thấy trùng: '" + th.getTenThuongHieu() + "' với '" + tenThuongHieu + "'");
                return true;
            }
        }
        return false;
    }

    public ThuongHieu doiTrangThai(String id) {
        Optional<ThuongHieu> optional = thuongHieuRepository.findById(id);
        if (optional.isPresent()) {
            ThuongHieu dm = optional.get();
            dm.setTrangThai(!dm.isTrangThai());
            return thuongHieuRepository.save(dm);
        }
        return null;
    }

    public Page<ThuongHieu> getallpage(Pageable pageable) {
        return thuongHieuRepository.findAllByOrderByNgayTaoDesc(pageable);
    }
}
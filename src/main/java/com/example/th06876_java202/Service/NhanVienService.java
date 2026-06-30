package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.NhanVien;
import com.example.th06876_java202.Repository.NhanVienRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class NhanVienService {
    private final NhanVienRepository nhanVienRepository;
    private final Random random = new Random();

    public List<NhanVien> search(String keyword, String role, Boolean status) {
        List<NhanVien> list = nhanVienRepository.findAll();

        return list.stream()
                .filter(nv -> keyword == null || keyword.isBlank()
                        || nv.getHoTen().toLowerCase().contains(keyword.toLowerCase())
                        || nv.getSoDienThoai().contains(keyword))
                .filter(nv -> role == null || role.isBlank()
                        || nv.getChucVu().equals(role))
                .filter(nv -> status == null
                        || nv.getTrangThai().equals(status))
                .toList();
    }


    public String generateMaNhanVien() {
        String code;
        boolean exists;
        do {
            int randomNumber = 1000 + random.nextInt(9000); // Tạo số từ 1000-9999
            code = "NV" + randomNumber;
            exists = nhanVienRepository.existsById(code);
        } while (exists);
        return code;
    }
}
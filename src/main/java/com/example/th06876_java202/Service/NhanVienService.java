package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.NhanVien;
import com.example.th06876_java202.Repository.NhanVienRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NhanVienService {
    private final NhanVienRepository nhanVienRepository;

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
}

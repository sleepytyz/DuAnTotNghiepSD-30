package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.DanhMucSanPham;
import com.example.th06876_java202.Repository.DanhMucSanPhamRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class DanhMucSanPhamService {

    private final DanhMucSanPhamRepository danhMucSanPhamRepository;
    private final Random random = new Random();

    public DanhMucSanPhamService(DanhMucSanPhamRepository danhMucSanPhamRepository) {
        this.danhMucSanPhamRepository = danhMucSanPhamRepository;
    }

    public List<DanhMucSanPham> getAll() {
        return danhMucSanPhamRepository.findAll();
    }

    public DanhMucSanPham them(DanhMucSanPham danhMucSanPham) {
        return danhMucSanPhamRepository.save(danhMucSanPham);
    }

    public Optional<DanhMucSanPham> findById(String id) {
        return danhMucSanPhamRepository.findById(id);
    }

    public void updatett(String id) {
        danhMucSanPhamRepository.updateTrangThai(id);
    }

    /**
     * Tạo mã danh mục tự động: DM + 4 số ngẫu nhiên
     */
    public String generateMaDanhMuc() {
        String code;
        boolean exists;
        do {
            int randomNumber = 1000 + random.nextInt(9000);
            code = "DM" + randomNumber;
            exists = danhMucSanPhamRepository.existsById(code);
        } while (exists);
        return code;
    }

    /**
     * Chuẩn hóa tên: loại bỏ khoảng trắng thừa, viết hoa chữ cái đầu
     */
    public String normalizeTenDanhMuc(String ten) {
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

    /**
     * Chuẩn hóa tên để so sánh
     */
    private String normalizeForCompare(String ten) {
        if (ten == null) return "";
        ten = ten.trim();
        ten = ten.replaceAll("\\s+", " ");
        return ten;
    }

    /**
     * Kiểm tra tên đã tồn tại
     */
    public boolean existsByTenDanhMuc(String tenDanhMuc) {
        if (tenDanhMuc == null) return false;

        String normalizedInput = normalizeForCompare(tenDanhMuc);

        List<DanhMucSanPham> all = danhMucSanPhamRepository.findAll();
        for (DanhMucSanPham dm : all) {
            String existingName = normalizeForCompare(dm.getTenDanhMuc());
            if (existingName.equalsIgnoreCase(normalizedInput)) {
                return true;
            }
        }
        return false;
    }

    public DanhMucSanPham doiTrangThai(String id) {
        Optional<DanhMucSanPham> optional = danhMucSanPhamRepository.findById(id);
        if (optional.isPresent()) {
            DanhMucSanPham dm = optional.get();
            dm.setTrangThai(!dm.isTrangThai());
            return danhMucSanPhamRepository.save(dm);
        }
        return null;
    }

    public Page<DanhMucSanPham> getallpage(Pageable pageable) {
        return danhMucSanPhamRepository.findAllByOrderByNgayTaoDesc(pageable);
    }

    public boolean ktratenIgnoreCase(String ten) {
        return existsByTenDanhMuc(ten);
    }
}
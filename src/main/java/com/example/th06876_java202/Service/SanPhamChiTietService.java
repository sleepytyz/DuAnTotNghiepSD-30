package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.*;
import com.example.th06876_java202.Repository.SanPhamChiTietRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class SanPhamChiTietService {

    @Autowired
    private MauSacService mauSacService;

    @Autowired
    private KichThuocService kichThuocService;

    private final SanPhamChiTietRepository sanPhamChiTietRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public SanPhamChiTietService(SanPhamChiTietRepository sanPhamChiTietRepository) {
        this.sanPhamChiTietRepository = sanPhamChiTietRepository;
    }

    public Page<SanPhamChiTiet> getall(Pageable pageable) {
        return sanPhamChiTietRepository.findAll(pageable);
    }

    public List<SanPhamChiTiet> getalll() {
        return sanPhamChiTietRepository.findAll();
    }

    public List<SanPhamChiTiet> getallll() {
        return sanPhamChiTietRepository.findAllOrderByNgayTaoDesc();
    }

    public SanPhamChiTiet them(SanPhamChiTiet sanPhamChiTiet) {
        if (sanPhamChiTiet.getNgayTao() == null) {
            sanPhamChiTiet.setNgayTao(LocalDateTime.now());
        }

        return sanPhamChiTietRepository.save(sanPhamChiTiet);
    }

    /**
     * Thêm mới chi tiết sản phẩm với nhiều ảnh
     */
    public SanPhamChiTiet themVoiNhieuAnh(SanPhamChiTiet entity, List<String> danhSachAnh) {
        if (entity.getDuongDanAnh() == null || entity.getDuongDanAnh().isEmpty()) {
            if (danhSachAnh != null && !danhSachAnh.isEmpty()) {
                entity.setDuongDanAnh(danhSachAnh.get(0));
            }
        }

        if (danhSachAnh != null && !danhSachAnh.isEmpty()) {
            try {
                String json = objectMapper.writeValueAsString(danhSachAnh);
                entity.setDanhSachAnh(json);
            } catch (Exception e) {
                entity.setDanhSachAnh(String.join(",", danhSachAnh));
            }
        }

        if (entity.getNgayTao() == null) {
            entity.setNgayTao(LocalDateTime.now());
        }

        capNhatTrangThaii(entity);

        return sanPhamChiTietRepository.save(entity);
    }


    public SanPhamChiTiet capNhatDanhSachAnh(String maBienThe, List<String> danhSachAnhMoi) {
        Optional<SanPhamChiTiet> optional = sanPhamChiTietRepository.findById(maBienThe);
        if (optional.isEmpty()) {
            throw new RuntimeException("Không tìm thấy biến thể với mã: " + maBienThe);
        }

        SanPhamChiTiet entity = optional.get();

        if (danhSachAnhMoi != null && !danhSachAnhMoi.isEmpty()) {
            try {
                String json = objectMapper.writeValueAsString(danhSachAnhMoi);
                entity.setDanhSachAnh(json);
                entity.setDuongDanAnh(danhSachAnhMoi.get(0));
            } catch (Exception e) {
                entity.setDanhSachAnh(String.join(",", danhSachAnhMoi));
                entity.setDuongDanAnh(danhSachAnhMoi.get(0));
            }
        } else {
            entity.setDanhSachAnh(null);
            entity.setDuongDanAnh(null);
        }


        return sanPhamChiTietRepository.save(entity);
    }

    /**
     * Thêm 1 ảnh vào danh sách
     */
    public SanPhamChiTiet themAnh(String maBienThe, String imageUrl) {
        Optional<SanPhamChiTiet> optional = sanPhamChiTietRepository.findById(maBienThe);
        if (optional.isEmpty()) {
            throw new RuntimeException("Không tìm thấy biến thể với mã: " + maBienThe);
        }

        SanPhamChiTiet entity = optional.get();
        List<String> images = entity.getDanhSachAnhList();
        images.add(imageUrl);

        try {
            entity.setDanhSachAnh(objectMapper.writeValueAsString(images));
        } catch (Exception e) {
            entity.setDanhSachAnh(String.join(",", images));
        }

        if (entity.getDuongDanAnh() == null || entity.getDuongDanAnh().isEmpty()) {
            entity.setDuongDanAnh(imageUrl);
        }

        return sanPhamChiTietRepository.save(entity);
    }

    /**
     * Xóa 1 ảnh theo URL
     */
    public SanPhamChiTiet xoaAnh(String maBienThe, String imageUrl) {
        Optional<SanPhamChiTiet> optional = sanPhamChiTietRepository.findById(maBienThe);
        if (optional.isEmpty()) {
            throw new RuntimeException("Không tìm thấy biến thể với mã: " + maBienThe);
        }

        SanPhamChiTiet entity = optional.get();
        List<String> images = entity.getDanhSachAnhList();
        boolean removed = images.remove(imageUrl);

        if (removed) {
            try {
                entity.setDanhSachAnh(objectMapper.writeValueAsString(images));
            } catch (Exception e) {
                entity.setDanhSachAnh(String.join(",", images));
            }

            if (imageUrl.equals(entity.getDuongDanAnh())) {
                entity.setDuongDanAnh(images.isEmpty() ? null : images.get(0));
            }

            return sanPhamChiTietRepository.save(entity);
        }

        return entity;
    }

    public SanPhamChiTiet xoaAnhTheoIndex(String maBienThe, int index) {
        Optional<SanPhamChiTiet> optional = sanPhamChiTietRepository.findById(maBienThe);
        if (optional.isEmpty()) {
            throw new RuntimeException("Không tìm thấy biến thể với mã: " + maBienThe);
        }

        SanPhamChiTiet entity = optional.get();
        List<String> images = entity.getDanhSachAnhList();

        if (index >= 0 && index < images.size()) {
            String removedImage = images.remove(index);

            try {
                entity.setDanhSachAnh(objectMapper.writeValueAsString(images));
            } catch (Exception e) {
                entity.setDanhSachAnh(String.join(",", images));
            }

            if (removedImage.equals(entity.getDuongDanAnh())) {
                entity.setDuongDanAnh(images.isEmpty() ? null : images.get(0));
            }

            return sanPhamChiTietRepository.save(entity);
        }

        return entity;
    }

    public SanPhamChiTiet sapXepAnh(String maBienThe, List<String> newOrder) {
        Optional<SanPhamChiTiet> optional = sanPhamChiTietRepository.findById(maBienThe);
        if (optional.isEmpty()) {
            throw new RuntimeException("Không tìm thấy biến thể với mã: " + maBienThe);
        }

        SanPhamChiTiet entity = optional.get();

        if (newOrder == null || newOrder.isEmpty()) {
            entity.setDanhSachAnh(null);
            entity.setDuongDanAnh(null);
        } else {
            try {
                entity.setDanhSachAnh(objectMapper.writeValueAsString(newOrder));
            } catch (Exception e) {
                entity.setDanhSachAnh(String.join(",", newOrder));
            }
            entity.setDuongDanAnh(newOrder.get(0));
        }


        return sanPhamChiTietRepository.save(entity);
    }

    /**
     * Lấy danh sách ảnh của biến thể
     */
    public List<String> getDanhSachAnh(String maBienThe) {
        Optional<SanPhamChiTiet> optional = sanPhamChiTietRepository.findById(maBienThe);
        if (optional.isEmpty()) {
            return new ArrayList<>();
        }
        return optional.get().getDanhSachAnhList();
    }

    /**
     * Kiểm tra biến thể có ít nhất N ảnh không
     */
    public boolean hasMinimumImages(String maBienThe, int min) {
        Optional<SanPhamChiTiet> optional = sanPhamChiTietRepository.findById(maBienThe);
        if (optional.isEmpty()) {
            return false;
        }
        return optional.get().getDanhSachAnhList().size() >= min;
    }

    public Optional<SanPhamChiTiet> findbyIid(String id) {
        return sanPhamChiTietRepository.findByIdWithSanPham(id);
    }

    public Optional<SanPhamChiTiet> findbyId(String id) {
        return sanPhamChiTietRepository.findById(id);
    }

    public Double gia() {
        return sanPhamChiTietRepository.findMaxGiaBan();
    }

    public Integer gi1a() {
        return sanPhamChiTietRepository.sluong();
    }

    public Page<SanPhamChiTiet> getByMauSac(String maSac, Pageable pageable) {
        return sanPhamChiTietRepository.findByMauSac_MaMauSac(maSac, pageable);
    }

    public Page<SanPhamChiTiet> getBySize(String size, Pageable pageable) {
        return sanPhamChiTietRepository.findByKichThuoc_MaKichThuoc(size, pageable);
    }

    public Page<SanPhamChiTiet> getByTT(String tt, Pageable pageable) {
        return sanPhamChiTietRepository.locTheoTrangThaiHienThi(tt, pageable);
    }

    public Page<SanPhamChiTiet> getBygia(BigDecimal gm, BigDecimal gm2, Pageable pageable) {
        return sanPhamChiTietRepository.findByGiaBanAndGiaBan(gm, gm2, pageable);
    }

    public int suaSanPham2(String maSanPham) {
        return sanPhamChiTietRepository.updateTrangThai(maSanPham);
    }

    public int updateTrangThai(String id, String trangThai) {
        return sanPhamChiTietRepository.updateTrangThaii(id, trangThai);
    }

    public int suaSanPham3(String maSanPham) {
        return sanPhamChiTietRepository.updateTrangThaiii(maSanPham);
    }

    public List<String> getSize() {
        return sanPhamChiTietRepository.findAllSize();
    }

    public List<String> getMsac() {
        return sanPhamChiTietRepository.findAllMauSac();
    }

    public List<SanPhamChiTiet> getByMauSac(String ms) {
        return sanPhamChiTietRepository.findByMauSac(ms);
    }

    public List<SanPhamChiTiet> getBySize(String ms) {
        return sanPhamChiTietRepository.findBySize(ms);
    }

    public List<SanPhamChiTiet> getByTT(String ms) {
        return sanPhamChiTietRepository.findByTT(ms);
    }

    public List<SanPhamChiTiet> getallsp(String maSanPham) {
        return sanPhamChiTietRepository.findByMaSanPham(maSanPham);
    }

    public Page<SanPhamChiTiet> getByTonKho(String tonKho, Pageable pageable) {
        return sanPhamChiTietRepository.findByTonKho(tonKho, pageable);
    }

    public Page<SanPhamChiTiet> findAllWithFilters(
            String size,
            String msac,
            String tt,
            BigDecimal gia,
            BigDecimal gia2,
            String tonKho,
            Pageable pageable) {

        return sanPhamChiTietRepository.findAllWithFilters(
                size, msac, tt, gia, gia2, tonKho, pageable);
    }

    public List<SanPhamChiTiet> findAllWithFilters(String size, String msac, String tt,
                                                   BigDecimal gia, BigDecimal gia2, String tonKho) {
        return sanPhamChiTietRepository.findAllWithFiltersList(size, msac, tt, gia, gia2, tonKho);
    }

    public void capNhatTrangThaii(SanPhamChiTiet spct) {
        // Nếu biến thể đã bị NGƯNG BÁN thủ công thì giữ nguyên, không tự bật lại khi cập nhật tồn kho.
        String hienTai = spct.getTrangThai();
        if ("Ngừng bán".equals(hienTai) || "Ngừng kinh doanh".equals(hienTai)) {
            return;
        }
        Integer soLuong = spct.getSoLuongTon();

        if (soLuong == null || soLuong <= 0) {
            spct.setTrangThai("Hết hàng");
        } else if (soLuong < 10) {
            spct.setTrangThai("Sắp hết");
        } else {
            spct.setTrangThai("Còn hàng");
        }
    }

    public String getGiaBanRange(String maSanPham) {
        List<SanPhamChiTiet> list = sanPhamChiTietRepository.findByMaSanPham(maSanPham);
        if (list == null || list.isEmpty()) {
            return "0₫";
        }

        BigDecimal min = list.stream()
                .map(SanPhamChiTiet::getGiaBan)
                .filter(g -> g != null && g.compareTo(BigDecimal.ZERO) > 0)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal max = list.stream()
                .map(SanPhamChiTiet::getGiaBan)
                .filter(g -> g != null && g.compareTo(BigDecimal.ZERO) > 0)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        if (min.compareTo(max) == 0) {
            return formatPrice(min);
        } else {
            return formatPrice(min) + " - " + formatPrice(max);
        }
    }

    public BigDecimal getGiaMin(String maSanPham) {
        List<SanPhamChiTiet> list = sanPhamChiTietRepository.findByMaSanPham(maSanPham);
        if (list == null || list.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return list.stream()
                .map(SanPhamChiTiet::getGiaBan)
                .filter(g -> g != null && g.compareTo(BigDecimal.ZERO) > 0)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal getGiaMax(String maSanPham) {
        List<SanPhamChiTiet> list = sanPhamChiTietRepository.findByMaSanPham(maSanPham);
        if (list == null || list.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return list.stream()
                .map(SanPhamChiTiet::getGiaBan)
                .filter(g -> g != null && g.compareTo(BigDecimal.ZERO) > 0)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) return "0₫";
        java.text.NumberFormat formatter = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        return formatter.format(price) + "₫";
    }

    public List<SanPhamChiTiet> findsp(List<String> listMaSanPham) {
        if (listMaSanPham == null || listMaSanPham.isEmpty()) {
            return new ArrayList<>();
        }
        return sanPhamChiTietRepository.findBySanPham_MaSanPhamIn(listMaSanPham);
    }

    public SanPhamChiTiet getById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        return sanPhamChiTietRepository.findById(id).orElse(null);
    }

    public void capNhatTrangThaiTuToggle(SanPhamChiTiet spct, boolean active) {
        if (active) {
            // Khi bật: luôn cập nhật dựa trên số lượng tồn
            capNhatTrangThaii(spct);
        } else {
            // Khi tắt: set thành "Ngừng bán"
            spct.setTrangThai("Ngừng bán");
        }
    }
}
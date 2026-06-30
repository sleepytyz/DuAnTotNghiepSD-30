package com.example.th06876_java202.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "SanPhamChiTiet")
@JsonIgnoreProperties({"sanPham", "kichThuoc", "mauSac"})
public class SanPhamChiTiet {

    @Id
    @Column(name = "MaSanPhamChiTiet")
    private String maSanPhamChiTiet;

    @ManyToOne
    @JoinColumn(name = "MaSanPham")
    private SanPham sanPham;

    @ManyToOne
    @JoinColumn(name = "MaKichThuoc")
    private KichThuoc kichThuoc;

    @ManyToOne
    @JoinColumn(name = "MaMauSac")
    private MauSac mauSac;

    @NotNull(message = "Giá bán không được để trống")
    @DecimalMin(value = "0", message = "Giá bán phải lớn hơn 0")
    @Column(name = "GiaBan")
    private BigDecimal giaBan;

    @NotNull(message = "Số lượng tồn không được để trống")
    @Min(value = 0, message = "Số lượng không được âm")
    @Column(name = "SoLuongTon")
    private Integer soLuongTon;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;


    @Column(name = "TrangThai")
    private String trangThai;

    @Column(name = "DuongDanAnh")
    private String duongDanAnh;

    @Column(name = "DanhSachAnh", columnDefinition = "NVARCHAR(MAX)")
    private String danhSachAnh;

    // ================================================================
    // PHƯƠNG THỨC HELPER XỬ LÝ DANH SÁCH ẢNH
    // ================================================================

    /**
     * Lấy danh sách ảnh dưới dạng List<String>
     * Hỗ trợ cả 2 định dạng: JSON và chuỗi phân tách bằng dấu phẩy
     */
    public List<String> getDanhSachAnhList() {
        if (danhSachAnh == null || danhSachAnh.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(danhSachAnh, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            String[] parts = danhSachAnh.split(",");
            List<String> result = new ArrayList<>();
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    result.add(trimmed);
                }
            }
            return result;
        }
    }

    public void setDanhSachAnhList(List<String> images) {
        if (images == null || images.isEmpty()) {
            this.danhSachAnh = null;
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            this.danhSachAnh = mapper.writeValueAsString(images);
        } catch (Exception e) {
            // Fallback: lưu dưới dạng chuỗi phân tách
            this.danhSachAnh = String.join(",", images);
        }
    }

    /**
     * Lấy ảnh đại diện
     * Ưu tiên: DuongDanAnh -> ảnh đầu tiên trong danh sách -> null
     */
    public String getAnhDaiDien() {
        // Nếu có DuongDanAnh thì dùng
        if (duongDanAnh != null && !duongDanAnh.isEmpty()) {
            return duongDanAnh;
        }
        // Nếu không, lấy ảnh đầu tiên trong danh sách
        List<String> images = getDanhSachAnhList();
        return images.isEmpty() ? null : images.get(0);
    }

    /**
     * Thêm 1 ảnh vào danh sách
     */
    public void addImage(String imageUrl) {
        List<String> images = getDanhSachAnhList();
        images.add(imageUrl);
        setDanhSachAnhList(images);

        // Nếu chưa có ảnh đại diện, set ảnh đầu tiên
        if ((duongDanAnh == null || duongDanAnh.isEmpty()) && !images.isEmpty()) {
            this.duongDanAnh = images.get(0);
        }
    }

    /**
     * Thêm nhiều ảnh vào danh sách
     */
    public void addImages(List<String> imageUrls) {
        List<String> images = getDanhSachAnhList();
        images.addAll(imageUrls);
        setDanhSachAnhList(images);

        if ((duongDanAnh == null || duongDanAnh.isEmpty()) && !images.isEmpty()) {
            this.duongDanAnh = images.get(0);
        }
    }

    /**
     * Xóa 1 ảnh theo index
     */
    public void removeImage(int index) {
        List<String> images = getDanhSachAnhList();
        if (index >= 0 && index < images.size()) {
            String removedImage = images.get(index);
            images.remove(index);
            setDanhSachAnhList(images);

            // Nếu ảnh đại diện bị xóa, cập nhật lại
            if (removedImage.equals(this.duongDanAnh)) {
                this.duongDanAnh = images.isEmpty() ? null : images.get(0);
            }
        }
    }

    /**
     * Xóa 1 ảnh theo URL
     */
    public void removeImageByUrl(String imageUrl) {
        List<String> images = getDanhSachAnhList();
        if (images.remove(imageUrl)) {
            setDanhSachAnhList(images);

            if (imageUrl.equals(this.duongDanAnh)) {
                this.duongDanAnh = images.isEmpty() ? null : images.get(0);
            }
        }
    }

    /**
     * Cập nhật thứ tự ảnh (sắp xếp lại)
     */
    public void reorderImages(List<String> newOrder) {
        if (newOrder == null || newOrder.isEmpty()) {
            this.danhSachAnh = null;
            this.duongDanAnh = null;
            return;
        }
        setDanhSachAnhList(newOrder);
        this.duongDanAnh = newOrder.get(0);
    }

    /**
     * Kiểm tra có ít nhất N ảnh không
     */
    public boolean hasMinimumImages(int min) {
        return getDanhSachAnhList().size() >= min;
    }

    /**
     * Lấy số lượng ảnh
     */
    public int getImageCount() {
        return getDanhSachAnhList().size();
    }

    /**
     * Kiểm tra danh sách ảnh có rỗng không
     */
    public boolean hasImages() {
        return !getDanhSachAnhList().isEmpty();
    }

    /**
     * Xóa tất cả ảnh
     */
    public void clearAllImages() {
        this.danhSachAnh = null;
        this.duongDanAnh = null;
    }

    /**
     * Lấy danh sách ảnh dạng mảng String
     */
    public String[] getDanhSachAnhArray() {
        List<String> list = getDanhSachAnhList();
        return list.toArray(new String[0]);
    }

    /**
     * Lấy ảnh thứ i (0-based)
     */
    public String getImageAt(int index) {
        List<String> images = getDanhSachAnhList();
        if (index >= 0 && index < images.size()) {
            return images.get(index);
        }
        return null;
    }
}
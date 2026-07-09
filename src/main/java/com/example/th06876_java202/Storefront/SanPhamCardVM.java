package com.example.th06876_java202.Storefront;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Thông tin hiển thị 1 sản phẩm trên trang chủ / danh sách / liên quan / yêu thích.
 * Được làm giàu bằng: điểm đánh giá, số lượt đánh giá, số đã bán, danh sách màu,
 * nhãn "Mới" — tất cả lấy từ dữ liệu thật trong CSDL.
 */
@Data
@NoArgsConstructor
public class SanPhamCardVM {
    private String maSanPham;
    private String tenSanPham;
    private String tenThuongHieu;
    private String tenDanhMuc;
    private String anh;
    private BigDecimal giaGoc;       // giá thấp nhất trong các biến thể còn hàng
    private BigDecimal giaSauGiam;   // giá sau khuyến mãi (= giaGoc nếu không có KM)
    private Integer phanTramGiam;    // % giảm cao nhất đang áp dụng (0 nếu không)
    private boolean conHang;
    private int tongTon;             // tổng tồn kho các biến thể đang bán
    private long daBan;              // tổng số lượng đã bán (đơn hoàn tất)
    private double diemTrungBinh;    // 0 nếu chưa có đánh giá
    private long soLuotDanhGia;
    private boolean moiVe;           // tạo trong vòng 30 ngày gần đây
    private List<String> tenMauSacs = new ArrayList<>(); // các màu đang có
    private List<String> tenKichThuocs = new ArrayList<>(); // các size đang có (phục vụ bộ lọc)
}

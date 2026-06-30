package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.HoaDonChiTiet;
import com.example.th06876_java202.Entity.SanPhamChiTiet;
import com.example.th06876_java202.Service.HoaDonChiTietService;
import com.example.th06876_java202.Repository.HoaDonChiTietRepository;
import com.example.th06876_java202.Repository.SanPhamChiTietRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/banhang")
public class BanHangApiController {

    @Autowired
    private HoaDonChiTietService hoaDonChiTietService;

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;

    /**
     * API cập nhật giá cho 1 sản phẩm
     */
    @PostMapping("/capnhatgia")
    @ResponseBody
    public ResponseEntity<?> capNhatGiaSanPham(
            @RequestParam("mahd") String maHoaDon,
            @RequestParam("mactsp") String maSPCT) {

        try {
            hoaDonChiTietService.capNhatGiaSanPham(maHoaDon, maSPCT);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật giá thành công!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * API cập nhật giá cho nhiều sản phẩm
     */
    @PostMapping("/capnhatgiatatca")
    @ResponseBody
    public ResponseEntity<?> capNhatGiaTatCa(
            @RequestParam("mahd") String maHoaDon,
            @RequestParam("maSPCTs") String maSPCTsJson) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            List<String> maSPCTs = mapper.readValue(maSPCTsJson, new TypeReference<List<String>>() {});

            hoaDonChiTietService.capNhatGiaTatCa(maHoaDon, maSPCTs);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật " + maSPCTs.size() + " sản phẩm thành công!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/giohang/{maHoaDon}")
    @ResponseBody
    public ResponseEntity<?> getCartWithLatestPrice(@PathVariable String maHoaDon) {
        try {
            System.out.println("=== LẤY GIỎ HÀNG ===");
            System.out.println("maHoaDon: " + maHoaDon);

            List<HoaDonChiTiet> hdctList = hoaDonChiTietRepository.findByMaHoaDon_MaHoaDon(maHoaDon);

            List<Map<String, Object>> cartItems = hdctList.stream().map(hdct -> {
                Map<String, Object> item = new HashMap<>();

                SanPhamChiTiet sp = hdct.getSanPhamChiTiet();

                item.put("maSPCT", sp.getMaSanPhamChiTiet());
                item.put("soLuong", hdct.getSoLuong());
                item.put("giaCu", hdct.getDonGia());
                item.put("giaMoi", sp.getGiaBan());
                item.put("tonKho", sp.getSoLuongTon()); // Tồn kho hiện tại

                // Lấy thông tin tên sản phẩm, màu sắc, kích thước
                String tenSanPham = sp.getSanPham() != null ? sp.getSanPham().getTenSanPham() : "Sản phẩm";
                String mauSac = sp.getMauSac() != null ? sp.getMauSac().getTenMauSac() : "";
                String kichThuoc = sp.getKichThuoc() != null ? sp.getKichThuoc().getTenKichThuoc() : "";

                item.put("tenSanPham", tenSanPham);
                item.put("mauSac", mauSac);
                item.put("kichThuoc", kichThuoc);

                // Kiểm tra nếu giá thay đổi
                BigDecimal giaCu = hdct.getDonGia();
                BigDecimal giaMoi = sp.getGiaBan();
                boolean daThayDoiGia = giaCu != null && giaMoi != null && giaCu.compareTo(giaMoi) != 0;
                item.put("daThayDoiGia", daThayDoiGia);

                // Kiểm tra nếu tồn kho không đủ so với số lượng trong hóa đơn
                int soLuongTrongHoaDon = hdct.getSoLuong();
                int tonKhoHienTai = sp.getSoLuongTon();
                boolean khongDuTonKho = tonKhoHienTai < soLuongTrongHoaDon;
                item.put("khongDuTonKho", khongDuTonKho);
                item.put("soLuongTrongHoaDon", soLuongTrongHoaDon);

                System.out.println("  - SP: " + tenSanPham + " [" + mauSac + " - " + kichThuoc + "]");
                System.out.println("    Số lượng trong hóa đơn: " + soLuongTrongHoaDon + ", Tồn kho: " + tonKhoHienTai);
                System.out.println("    Không đủ tồn kho: " + khongDuTonKho);

                return item;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(cartItems);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/xoatatca")
    @ResponseBody
    public ResponseEntity<?> xoaNhieuSanPham(
            @RequestParam("mahd") String maHoaDon,
            @RequestParam("maSPCTs") String maSPCTsJson) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            List<String> maSPCTs = mapper.readValue(maSPCTsJson, new TypeReference<List<String>>() {});

            System.out.println("=== XOA NHIEU SAN PHAM ===");
            System.out.println("Ma HD: " + maHoaDon);
            System.out.println("So luong SP can xoa: " + maSPCTs.size());

            int count = 0;
            int tongHoanLai = 0;

            for (String maSPCT : maSPCTs) {
                HoaDonChiTiet hdct = hoaDonChiTietRepository.findByMaHoaDon_MaHoaDonAndSanPhamChiTiet_MaSanPhamChiTiet(
                        maHoaDon, maSPCT
                );

                if (hdct != null) {
                    SanPhamChiTiet sp = sanPhamChiTietRepository.findById(maSPCT).orElse(null);
                    if (sp != null) {
                        sp.setSoLuongTon(sp.getSoLuongTon() + hdct.getSoLuong());
                        sanPhamChiTietRepository.save(sp);
                        tongHoanLai += hdct.getSoLuong();
                    }
                    hoaDonChiTietRepository.delete(hdct);
                    count++;
                }
            }

            System.out.println("✅ Da xoa " + count + " san pham, hoan " + tongHoanLai + " SP vao kho");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã xóa " + count + " sản phẩm khỏi hóa đơn, hoàn " + tongHoanLai + " SP vào kho!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/capnhatsoluong")
    @ResponseBody
    public ResponseEntity<?> capNhatSoLuongNhieu(
            @RequestParam("mahd") String maHoaDon,
            @RequestParam("updateData") String updateDataJson) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> updateData = mapper.readValue(updateDataJson, new TypeReference<List<Map<String, Object>>>() {});

            int count = 0;
            for (Map<String, Object> data : updateData) {
                String maSPCT = (String) data.get("maSPCT");
                Integer soLuongMoi = (Integer) data.get("soLuongMoi");

                if (maSPCT != null && soLuongMoi != null && soLuongMoi > 0) {
                    HoaDonChiTiet hdct = hoaDonChiTietRepository.findByMaHoaDon_MaHoaDonAndSanPhamChiTiet_MaSanPhamChiTiet(
                            maHoaDon, maSPCT
                    );

                    if (hdct != null) {
                        // Cập nhật số lượng
                        hdct.setSoLuong(soLuongMoi);

                        // Cập nhật thành tiền
                        BigDecimal donGia = hdct.getDonGia();
                        BigDecimal thanhTienMoi = donGia.multiply(BigDecimal.valueOf(soLuongMoi));
                        hdct.setThanhTien(thanhTienMoi);

                        hoaDonChiTietRepository.save(hdct);
                        count++;
                    }
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã cập nhật " + count + " sản phẩm!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/capnhatsoluongmot")
    @ResponseBody
    public ResponseEntity<?> capNhatSoLuongMot(
            @RequestParam("mahd") String maHoaDon,
            @RequestParam("mactsp") String maSPCT,
            @RequestParam("soLuong") int soLuongMoi) {

        try {
            System.out.println("=== CAP NHAT SO LUONG ===");
            System.out.println("Ma HD: " + maHoaDon);
            System.out.println("Ma SPCT: " + maSPCT);
            System.out.println("So luong moi: " + soLuongMoi);

            // Tìm chi tiết hóa đơn
            HoaDonChiTiet hdct = hoaDonChiTietRepository.findByMaHoaDon_MaHoaDonAndSanPhamChiTiet_MaSanPhamChiTiet(
                    maHoaDon, maSPCT
            );

            if (hdct == null) {
                throw new RuntimeException("Không tìm thấy sản phẩm trong hóa đơn!");
            }

            // Lấy sản phẩm chi tiết
            SanPhamChiTiet spct = hdct.getSanPhamChiTiet();
            if (spct == null) {
                throw new RuntimeException("Không tìm thấy sản phẩm!");
            }

            int soLuongCu = hdct.getSoLuong();
            int soLuongMoiFinal = soLuongMoi > 0 ? soLuongMoi : 1;

            // Tính chênh lệch
            int chenhLech = soLuongMoiFinal - soLuongCu;

            System.out.println("So luong cu: " + soLuongCu);
            System.out.println("Chenh lech: " + chenhLech);
            System.out.println("Ton kho hien tai: " + spct.getSoLuongTon());

            // Cập nhật tồn kho
            if (chenhLech > 0) {
                // Tăng số lượng -> Giảm tồn kho
                if (spct.getSoLuongTon() < chenhLech) {
                    throw new RuntimeException("Không đủ tồn kho! Còn " + spct.getSoLuongTon());
                }
                spct.setSoLuongTon(spct.getSoLuongTon() - chenhLech);
                System.out.println("-> Giam ton kho: " + chenhLech);
            } else if (chenhLech < 0) {
                // Giảm số lượng -> Tăng tồn kho
                spct.setSoLuongTon(spct.getSoLuongTon() + Math.abs(chenhLech));
                System.out.println("-> Tang ton kho: " + Math.abs(chenhLech));
            } else {
                System.out.println("-> Khong thay doi ton kho");
            }

            // Lưu cập nhật tồn kho
            sanPhamChiTietRepository.save(spct);

            // Cập nhật số lượng và thành tiền
            hdct.setSoLuong(soLuongMoiFinal);
            BigDecimal donGia = hdct.getDonGia();
            BigDecimal thanhTienMoi = donGia.multiply(BigDecimal.valueOf(soLuongMoiFinal));
            hdct.setThanhTien(thanhTienMoi);

            hoaDonChiTietRepository.save(hdct);

            System.out.println("✅ Cap nhat so luong thanh cong!");
            System.out.println("Ton kho moi: " + spct.getSoLuongTon());
            System.out.println("=== END CAP NHAT SO LUONG ===");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã cập nhật số lượng thành công!");
            response.put("tonKhoMoi", spct.getSoLuongTon());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/xoasp")
    @ResponseBody
    public ResponseEntity<?> xoaSanPham(
            @RequestParam("mahd") String maHoaDon,
            @RequestParam("mactsp") String maSPCT) {

        try {
            System.out.println("=== XOA SAN PHAM ===");
            System.out.println("Ma HD: " + maHoaDon);
            System.out.println("Ma SPCT: " + maSPCT);

            // Dùng method tìm kiếm trực tiếp từ Repository
            HoaDonChiTiet hdct = hoaDonChiTietRepository.findByMaHoaDon_MaHoaDonAndSanPhamChiTiet_MaSanPhamChiTiet(
                    maHoaDon, maSPCT
            );

            if (hdct == null) {
                System.out.println("❌ Khong tim thay san pham!");

                // Log thêm để debug
                List<HoaDonChiTiet> allItems = hoaDonChiTietRepository.findByMaHoaDon_MaHoaDon(maHoaDon);
                System.out.println("Tong so san pham trong hoa don: " + allItems.size());
                for (HoaDonChiTiet item : allItems) {
                    System.out.println("  - Ma SPCT: " + item.getSanPhamChiTiet().getMaSanPhamChiTiet());
                }

                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Không tìm thấy sản phẩm trong hóa đơn!");
                return ResponseEntity.badRequest().body(response);
            }

            SanPhamChiTiet spct = hdct.getSanPhamChiTiet();
            int soLuongHoan = hdct.getSoLuong();
            System.out.println("So luong hoan: " + soLuongHoan);

            // Hoàn lại tồn kho
            spct.setSoLuongTon(spct.getSoLuongTon() + soLuongHoan);
            sanPhamChiTietRepository.save(spct);

            // Xóa chi tiết hóa đơn
            hoaDonChiTietRepository.delete(hdct);

            System.out.println("✅ Xoa thanh cong!");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã xóa sản phẩm và hoàn " + soLuongHoan + " SP vào kho!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
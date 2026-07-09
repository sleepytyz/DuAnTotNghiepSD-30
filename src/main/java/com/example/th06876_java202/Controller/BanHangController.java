package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.*;
import com.example.th06876_java202.Repository.*;
import com.example.th06876_java202.Service.*;
import com.example.th06876_java202.config.CustomUserDetails;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.EncodeHintType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.math.BigDecimal;
import org.slf4j.Logger;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/banhang")
@Transactional
public class BanHangController {

    @Autowired
     GiamGiaChiTietRepo giamGiaChiTietRepository;

    @Autowired
    SanPhamChiTietRepository sanPhamChiTietRepository;

    @Autowired
    HoaDonChiTietRepository hoaDonChiTietRepository;

    @Autowired
    DiaChiRepo diaChiRepo;

    @Autowired
    private GHNShippingService ghnShippingService;

    @Autowired
    HoaDonRepo hoaDonRepo;

    @Autowired
    private DiaChiService diaChiService;

    private static final Logger logger = LoggerFactory.getLogger(BanHangController.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String bank = "MB";

    private String account = "0344552008";
    // @Value("${vietqr.account-name}")
    private String accountName = "TRUONG HAI MINH";

    private final SanPhamService sanPhamService;
    private final SanPhamChiTietService sanPhamChiTietService;
    private final KhachHangService khachHangService;
    private final HoaDonService hoaDonService;
    private final HoaDonChiTietService hoaDonChiTietService;
    private final DotGiamGiaService dotGiamGiaService;
    private final TaiKhoanService taiKhoanService;
    private final GiamGiaService giamGiaService;

        private static final BigDecimal PHI_SHIP_MAC_DINH = BigDecimal.valueOf(30000);
    private static final int MAX_HOA_DON_CHO = 5;

    public BanHangController(SanPhamChiTietService sanPhamChiTietService,
                             KhachHangService khachHangService,
                             HoaDonService hoaDonService,
                             HoaDonChiTietService hoaDonChiTietService,
                             DotGiamGiaService dotGiamGiaService,
                             SanPhamService sanPhamService,
                             TaiKhoanService taiKhoanService,
                             GiamGiaService giamGiaService) {
        this.sanPhamChiTietService = sanPhamChiTietService;
        this.khachHangService = khachHangService;
        this.hoaDonService = hoaDonService;
        this.hoaDonChiTietService = hoaDonChiTietService;
        this.dotGiamGiaService = dotGiamGiaService;
        this.sanPhamService = sanPhamService;
        this.taiKhoanService = taiKhoanService;
        this.giamGiaService = giamGiaService;
    }

    public BigDecimal tinhMucGiamVoucher(GiamGia gg, BigDecimal tongTien) {
        if (gg == null || tongTien == null || tongTien.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        System.out.println("=== TINH MUC GIAM VOUCHER ===");
        System.out.println("Ma: " + gg.getMaGiamGia());
        System.out.println("Loai: " + gg.getLoaiGiamGia());
        System.out.println("GiaTriGiam: " + gg.getGiaTriGiam());
        System.out.println("GiamToiDa: " + gg.getGiamToiDa());
        System.out.println("TongTien: " + tongTien);

        // ⭐ KIỂM TRA LOẠI GIẢM GIÁ
        if ("PhanTram".equalsIgnoreCase(gg.getLoaiGiamGia())) {
            BigDecimal phanTram = gg.getGiaTriGiam();
            if (phanTram == null || phanTram.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("-> Phan tram null hoac <= 0, tra ve 0");
                return BigDecimal.ZERO;
            }

            // ⭐ TÍNH GIẢM THEO PHẦN TRĂM
            BigDecimal giam = tongTien.multiply(phanTram).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

            // ⭐ KIỂM TRA GIẢM TỐI ĐA
            if (gg.getGiamToiDa() != null && gg.getGiamToiDa().compareTo(BigDecimal.ZERO) > 0) {
                giam = giam.min(gg.getGiamToiDa());
            }

            System.out.println("-> Giam tinh: " + giam);
            return giam;

        } else if ("SoTien".equalsIgnoreCase(gg.getLoaiGiamGia()) || "Tien".equalsIgnoreCase(gg.getLoaiGiamGia())) {
            BigDecimal giam = gg.getGiaTriGiam();
            if (giam == null) {
                System.out.println("-> Gia tri giam null, tra ve 0");
                return BigDecimal.ZERO;
            }

            // Kiểm tra giảm tối đa (nếu có)
            if (gg.getGiamToiDa() != null && gg.getGiamToiDa().compareTo(BigDecimal.ZERO) > 0) {
                giam = giam.min(gg.getGiamToiDa());
            }

            System.out.println("-> Giam tinh: " + giam);
            return giam;
        }

        System.out.println("-> Khong xac dinh loai giam gia, tra ve 0");
        return BigDecimal.ZERO;
    }

    private KhachHang getOrCreateKhachLe() {
        List<KhachHang> listKH = khachHangService.findAllBySdt("0000000000");

        if (listKH != null && !listKH.isEmpty()) {
            return listKH.get(0);
        }

        KhachHang khachLe = new KhachHang();
        khachLe.setMaKH("KH" + System.currentTimeMillis());
        khachLe.setHoTen("Khách lẻ");
        khachLe.setSdt("0000000000");
        khachLe.setEmail("khachle@fsshop.com");
        khachLe.setNgayDangKy(LocalDate.now());
        khachLe.setTrangThai(true);
        khachLe.setGioiTinh(true);

        khachHangService.save(khachLe);
        return khachLe;
    }

        @GetMapping("/index")
        public String index(@RequestParam(value = "mahd", required = false) String mahd,
                            @RequestParam(value = "qr", required = false) String qrData,
                            Model model) {

            System.out.println("========== BAN HANG INDEX ==========");
            System.out.println("📋 Loading page with mahd: " + mahd);

            // ⭐ KHỞI TẠO listDiaChi TỪ ĐẦU
            List<DiaChi> listDiaChi = new ArrayList<>();

            List<HoaDon> hoaDonCho = hoaDonService.findByTrangThai("Đang xử lý");
            if (hoaDonCho == null) hoaDonCho = new ArrayList<>();
            if (hoaDonCho.size() > MAX_HOA_DON_CHO) {
                hoaDonCho = hoaDonCho.subList(0, MAX_HOA_DON_CHO);
            }

            HoaDon hoadonHienTai = null;
            List<HoaDonChiTiet> hdct = new ArrayList<>();
            BigDecimal tongTienGioHang = BigDecimal.ZERO;
            BigDecimal tienGiamVoucher = BigDecimal.ZERO;
            BigDecimal tongThanhToan = BigDecimal.ZERO;
            BigDecimal tienShip = BigDecimal.ZERO;
            String qrCodeBase64 = null;
            DiaChi diaChiMacDinh = null;

            if (mahd != null && !mahd.trim().isEmpty()) {
                hoadonHienTai = hoaDonService.findById(mahd);
                System.out.println("📄 Hoa don found: " + (hoadonHienTai != null ? "YES" : "NO"));

                if (hoadonHienTai != null) {
                    List<HoaDonChiTiet> temp = hoaDonChiTietService.findById(mahd);
                    hdct = (temp != null) ? temp : new ArrayList<>();
                    System.out.println("📦 So luong san pham: " + hdct.size());

                    // ⭐ TÍNH TỔNG TIỀN HÀNG
                    tongTienGioHang = hdct.stream()
                            .map(HoaDonChiTiet::getThanhTien)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    // ⭐ TÍNH TIỀN GIẢM VOUCHER
                    tienGiamVoucher = tinhTienGiamVoucher(hoadonHienTai, tongTienGioHang);

                    // ⭐ LẤY ĐỊA CHỈ
                    if (hoadonHienTai.getMaKhachHang() != null) {
                        diaChiMacDinh = diaChiService.findByKhachHangAndDiaChiMacDinh(
                                hoadonHienTai.getMaKhachHang(), true
                        );

                        if (diaChiMacDinh == null) {
                            List<DiaChi> danhSachDiaChi = diaChiService.findByKhachHang(
                                    hoadonHienTai.getMaKhachHang()
                            );
                            if (danhSachDiaChi != null && !danhSachDiaChi.isEmpty()) {
                                diaChiMacDinh = danhSachDiaChi.get(0);
                            }
                        }

                        System.out.println("📍 Dia chi mac dinh: " + (diaChiMacDinh != null ? "YES" : "NO"));
                        if (diaChiMacDinh != null) {
                            System.out.println("   - " + diaChiMacDinh.getDiaChiCuThe() + ", " +
                                    diaChiMacDinh.getPhuongXa() + ", " +
                                    diaChiMacDinh.getQuanHuyen());
                        }

                        // ⭐⭐ LẤY DANH SÁCH ĐỊA CHỈ CỦA KHÁCH HÀNG ⭐⭐
                        KhachHang kh = hoadonHienTai.getMaKhachHang();
                        System.out.println("👤 Khách hàng: " + kh.getMaKH() + " - " + kh.getHoTen());

                        // Lấy tất cả địa chỉ của khách hàng
                        listDiaChi = diaChiService.findByKhachHang(kh);
                        System.out.println("📍 Số địa chỉ tìm thấy: " + (listDiaChi != null ? listDiaChi.size() : 0));

                        if (listDiaChi != null && !listDiaChi.isEmpty()) {
                            for (DiaChi dc : listDiaChi) {
                                System.out.println("   - ID: " + dc.getMaDiaChi() +
                                        ", Tên: " + dc.getTenNguoiNhan() +
                                        ", Địa chỉ: " + dc.getDiaChiCuThe() +
                                        ", Mặc định: " + dc.getDiaChiMacDinh());
                            }
                        }
                    } else {
                        System.out.println("⚠️ Không có khách hàng");
                        listDiaChi = new ArrayList<>();
                    }

                    // ⭐ TÍNH PHÍ SHIP (CHỈ CHO ONLINE)
                    if ("Online".equalsIgnoreCase(hoadonHienTai.getLoaiBan())) {
                        tienShip = tinhPhiShipGHN(hoadonHienTai);
                        if (tienShip == null || tienShip.compareTo(BigDecimal.ZERO) <= 0) {
                            tienShip = PHI_SHIP_MAC_DINH;
                        }
                        hoadonHienTai.setTienShip(tienShip);
                        System.out.println("🚚 Tien ship: " + tienShip);
                    } else {
                        tienShip = BigDecimal.ZERO;
                        hoadonHienTai.setTienShip(BigDecimal.ZERO);
                    }

                    // ⭐ TÍNH TỔNG THANH TOÁN = TIỀN HÀNG - GIẢM VOUCHER + PHÍ SHIP
                    tongThanhToan = tongTienGioHang
                            .subtract(tienGiamVoucher != null ? tienGiamVoucher : BigDecimal.ZERO)
                            .add(tienShip != null ? tienShip : BigDecimal.ZERO);

                    // Đảm bảo không âm
                    if (tongThanhToan.compareTo(BigDecimal.ZERO) < 0) {
                        tongThanhToan = BigDecimal.ZERO;
                    }

                    // ⭐ LƯU TỔNG TIỀN VÀO HÓA ĐƠN
                    hoadonHienTai.setTongTien(tongThanhToan);
                    hoaDonService.save(hoadonHienTai);

                    System.out.println("💰 Tong tien hang: " + tongTienGioHang);
                    System.out.println("💰 Tien giam voucher: " + tienGiamVoucher);
                    System.out.println("🚚 Tien ship: " + tienShip);
                    System.out.println("💰 Tong thanh toan: " + tongThanhToan);
                    System.out.println("💰 Tien giam voucher sau khi tinh: " + tienGiamVoucher);
                    System.out.println("💰 Voucher hien tai: " + (hoadonHienTai.getMaGiamGia() != null ? hoadonHienTai.getMaGiamGia().getMaGiamGia() : "null"));
                }
            }

            // ⭐ QR CODE TỪ URL (nếu có)
            if (qrData != null && !qrData.isEmpty()) {
                qrCodeBase64 = generateQRCodeBase64(qrData);
            }

            List<SanPhamChiTiet> sanPhamList = sanPhamChiTietService.getallll();
            if (sanPhamList == null) sanPhamList = new ArrayList<>();

            Map<String, BigDecimal> mapGiamGia = new HashMap<>();
            Map<String, BigDecimal> mapGiaSauGiam = new HashMap<>();

            for (SanPhamChiTiet spct : sanPhamList) {
                BigDecimal giaGoc = spct.getGiaBan() != null ? spct.getGiaBan() : BigDecimal.ZERO;
                BigDecimal giamGia = tinhGiamGiaSanPham(spct);
                BigDecimal giaSauGiam = giaGoc.subtract(giamGia).max(BigDecimal.ZERO);

                mapGiamGia.put(spct.getMaSanPhamChiTiet(), giamGia);
                mapGiaSauGiam.put(spct.getMaSanPhamChiTiet(), giaSauGiam);
            }

            // ================================================================
            // ⭐⭐ SỬA PHẦN LẤY VOUCHER THEO KHÁCH HÀNG ⭐⭐
            // ================================================================
            List<GiamGia> listVoucherHoatDong = new ArrayList<>();
            GiamGia voucherTotNhat = null;

            if (hoadonHienTai != null) {
                KhachHang khachHang = hoadonHienTai.getMaKhachHang();

                // Nếu có khách hàng và không phải khách lẻ
                if (khachHang != null && !"0000000000".equals(khachHang.getSdt())) {
                    // Lấy voucher cho khách hàng (công khai + cá nhân được áp dụng)
                    listVoucherHoatDong = getVoucherChoKhachHang(khachHang);
                    System.out.println("👤 Khách hàng: " + khachHang.getHoTen() +
                            " (" + khachHang.getMaKH() + ") - Số voucher: " + listVoucherHoatDong.size());
                } else {
                    // Khách lẻ hoặc chưa có khách hàng -> chỉ lấy voucher công khai
                    listVoucherHoatDong = getVoucherCongKhai();
                    System.out.println("👤 Khách lẻ - Số voucher: " + listVoucherHoatDong.size());
                }

                // ⭐ TÌM VOUCHER TỐT NHẤT CHO HÓA ĐƠN HIỆN TẠI
                if (!listVoucherHoatDong.isEmpty() && !hdct.isEmpty()) {
                    voucherTotNhat = timVoucherTotNhatChoHoaDon(hoadonHienTai, tongTienGioHang);
                    if (voucherTotNhat != null) {
                        System.out.println("⭐ Voucher tốt nhất: " + voucherTotNhat.getMaGiamGia() +
                                " - " + voucherTotNhat.getTenGiamGia());
                    }
                }
            } else {
                // Chưa có hóa đơn -> chỉ hiển thị voucher công khai
                listVoucherHoatDong = getVoucherCongKhai();
                System.out.println("👤 Chưa có hóa đơn - Số voucher: " + listVoucherHoatDong.size());
            }

            // ================================================================
            // ⭐⭐ THÊM listDiaChi VÀO MODEL (QUAN TRỌNG) ⭐⭐
            // ================================================================
            model.addAttribute("listDiaChi", listDiaChi != null ? listDiaChi : new ArrayList<>());
            System.out.println("✅ Đã thêm listDiaChi vào model: " + (listDiaChi != null ? listDiaChi.size() : 0) + " địa chỉ");

            // ================================================================

            model.addAttribute("diaChi", new DiaChi());
            model.addAttribute("diaChiMacDinh", diaChiMacDinh);
            model.addAttribute("hoaDonCho", hoaDonCho);
            model.addAttribute("hoadonHienTai", hoadonHienTai);
            model.addAttribute("listhdct", hdct);
            model.addAttribute("tongTienGioHang", tongTienGioHang);
            model.addAttribute("tienGiamVoucher", tienGiamVoucher);
            model.addAttribute("tongThanhToan", tongThanhToan);
            model.addAttribute("tienShip", tienShip);
            model.addAttribute("qrCodeBase64", qrCodeBase64);
            model.addAttribute("hoadonct", new HoaDonChiTiet());
            model.addAttribute("kh", new KhachHang());
            model.addAttribute("hoadon", new HoaDon());

            // ⭐ TRUYỀN DANH SÁCH VOUCHER ĐÃ LỌC THEO KHÁCH HÀNG
            model.addAttribute("listgg", listVoucherHoatDong);
            model.addAttribute("voucherTotNhat", voucherTotNhat);

            // ⭐ THÊM THÔNG TIN KHÁCH HÀNG HIỆN TẠI ĐỂ HIỂN THỊ
            model.addAttribute("khachHangHienTai", hoadonHienTai != null ? hoadonHienTai.getMaKhachHang() : null);

            model.addAttribute("listkh", khachHangService.getkh());
            model.addAttribute("listsanpham", sanPhamList);
            model.addAttribute("listsanphamms", sanPhamChiTietService.getMsac());
            model.addAttribute("listsanphams", sanPhamChiTietService.getSize());
            model.addAttribute("mapGiamGia", mapGiamGia);
            model.addAttribute("mapGiaSauGiam", mapGiaSauGiam);

            System.out.println("📋 Tổng số voucher hiển thị: " + listVoucherHoatDong.size());
            System.out.println("========== END INDEX ==========");
            return "banhang/index";
        }

        @GetMapping("/hoa-don-chi-tiet")
        @ResponseBody
        public ResponseEntity<Map<String, Object>> getHoaDonChiTiet(
                @RequestParam("mahd") String maHoaDon) {

            Map<String, Object> response = new HashMap<>();

            try {
                // 1. Lấy hóa đơn
                HoaDon hoaDon = hoaDonService.findById(maHoaDon);
                if (hoaDon == null) {
                    response.put("success", false);
                    response.put("message", "Không tìm thấy hóa đơn");
                    return ResponseEntity.ok(response);
                }

                // 2. Lấy chi tiết hóa đơn
                List<HoaDonChiTiet> chiTietList = hoaDonChiTietService.findById(maHoaDon);
                if (chiTietList == null) chiTietList = new ArrayList<>();

                // 3. Tính tổng tiền
                BigDecimal tongTienHang = chiTietList.stream()
                        .map(HoaDonChiTiet::getThanhTien)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // 4. Tính tiền giảm voucher
                BigDecimal tienGiamVoucher = tinhTienGiamVoucher(hoaDon, tongTienHang);

                // 5. Tính phí ship
                BigDecimal tienShip = BigDecimal.ZERO;
                if ("Online".equalsIgnoreCase(hoaDon.getLoaiBan())) {
                    tienShip = tinhPhiShipGHN(hoaDon);
                    if (tienShip == null || tienShip.compareTo(BigDecimal.ZERO) <= 0) {
                        tienShip = PHI_SHIP_MAC_DINH;
                    }
                }

                // 6. Tính tổng thanh toán
                BigDecimal tongThanhToan = tongTienHang
                        .subtract(tienGiamVoucher != null ? tienGiamVoucher : BigDecimal.ZERO)
                        .add(tienShip != null ? tienShip : BigDecimal.ZERO);

                if (tongThanhToan.compareTo(BigDecimal.ZERO) < 0) {
                    tongThanhToan = BigDecimal.ZERO;
                }

                // 7. Lấy địa chỉ mặc định (nếu có)
                DiaChi diaChiMacDinh = null;
                List<DiaChi> listDiaChi = new ArrayList<>();
                if (hoaDon.getMaKhachHang() != null) {
                    diaChiMacDinh = diaChiService.findByKhachHangAndDiaChiMacDinh(
                            hoaDon.getMaKhachHang(), true
                    );

                    if (diaChiMacDinh == null) {
                        List<DiaChi> danhSachDiaChi = diaChiService.findByKhachHang(
                                hoaDon.getMaKhachHang()
                        );
                        if (danhSachDiaChi != null && !danhSachDiaChi.isEmpty()) {
                            diaChiMacDinh = danhSachDiaChi.get(0);
                        }
                    }

                    listDiaChi = diaChiService.findByKhachHang(hoaDon.getMaKhachHang());
                }

                // 8. Lấy voucher tốt nhất
                List<GiamGia> listVoucher = new ArrayList<>();
                GiamGia voucherTotNhat = null;

                if (hoaDon.getMaKhachHang() != null &&
                        !"0000000000".equals(hoaDon.getMaKhachHang().getSdt())) {
                    listVoucher = getVoucherChoKhachHang(hoaDon.getMaKhachHang());
                } else {
                    listVoucher = getVoucherCongKhai();
                }

                if (!listVoucher.isEmpty() && !chiTietList.isEmpty()) {
                    voucherTotNhat = timVoucherTotNhatChoHoaDon(hoaDon, tongTienHang);
                }

                // 9. Build response data
                Map<String, Object> data = new HashMap<>();
                data.put("maHoaDon", hoaDon.getMaHoaDon());
                data.put("loaiBan", hoaDon.getLoaiBan());
                data.put("trangThai", hoaDon.getTrangThai());
                data.put("ngayTao", hoaDon.getNgayTao() != null ?
                        hoaDon.getNgayTao().format(DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy")) : "");
                data.put("tongTienHang", tongTienHang);
                data.put("tienGiamVoucher", tienGiamVoucher);
                data.put("tienShip", tienShip);
                data.put("tongThanhToan", tongThanhToan);
                data.put("phuongThucThanhToan", hoaDon.getPhuongThucThanhToan());
                data.put("ghiChu", hoaDon.getGhiChu());
                data.put("diaChiGiaoHang", hoaDon.getDiaChiGiaoHang());

                // Thông tin khách hàng
                if (hoaDon.getMaKhachHang() != null) {
                    Map<String, Object> khachHang = new HashMap<>();
                    khachHang.put("maKH", hoaDon.getMaKhachHang().getMaKH());
                    khachHang.put("ten", hoaDon.getMaKhachHang().getHoTen());
                    khachHang.put("sdt", hoaDon.getMaKhachHang().getSdt());
                    data.put("khachHang", khachHang);
                }

                // Thông tin nhân viên
                if (hoaDon.getMaNhanVien() != null) {
                    Map<String, Object> nhanVien = new HashMap<>();
                    nhanVien.put("ten", hoaDon.getMaNhanVien().getHoTen());
                    data.put("nhanVien", nhanVien);
                }

                // Chi tiết sản phẩm
                List<Map<String, Object>> chiTietSanPham = new ArrayList<>();
                for (HoaDonChiTiet ct : chiTietList) {
                    Map<String, Object> ctMap = new HashMap<>();
                    ctMap.put("maSanPhamChiTiet", ct.getSanPhamChiTiet() != null ?
                            ct.getSanPhamChiTiet().getMaSanPhamChiTiet() : "");
                    ctMap.put("tenSanPham", ct.getSanPhamChiTiet() != null &&
                            ct.getSanPhamChiTiet().getSanPham() != null ?
                            ct.getSanPhamChiTiet().getSanPham().getTenSanPham() : "Không xác định");
                    ctMap.put("soLuong", ct.getSoLuong());
                    ctMap.put("donGia", ct.getDonGia());
                    ctMap.put("thanhTien", ct.getThanhTien());
                    ctMap.put("tienGiam", ct.getTienGiam());

                    // Thêm màu sắc, kích thước nếu có
                    if (ct.getSanPhamChiTiet() != null) {
                        if (ct.getSanPhamChiTiet().getMauSac() != null) {
                            ctMap.put("mauSac", ct.getSanPhamChiTiet().getMauSac().getTenMauSac());
                        }
                        if (ct.getSanPhamChiTiet().getKichThuoc() != null) {
                            ctMap.put("kichThuoc", ct.getSanPhamChiTiet().getKichThuoc().getTenKichThuoc());
                        }
                    }

                    chiTietSanPham.add(ctMap);
                }
                data.put("chiTietSanPham", chiTietSanPham);

                // Địa chỉ
                data.put("diaChiMacDinh", diaChiMacDinh);
                data.put("listDiaChi", listDiaChi);

                // Voucher
                data.put("listVoucher", listVoucher);
                data.put("voucherTotNhat", voucherTotNhat);

                response.put("success", true);
                response.put("data", data);

            } catch (Exception e) {
                response.put("success", false);
                response.put("message", "Lỗi: " + e.getMessage());
                e.printStackTrace();
            }

            return ResponseEntity.ok(response);
        }

    @PostMapping("/apdungvouchertotnhat")
    public String apDungVoucherTotNhat(@RequestParam("mahd") String mahd,
                                       RedirectAttributes redirectAttributes) {
        try {
            HoaDon hoaDon = hoaDonService.findById(mahd);
            if (hoaDon == null) {
                redirectAttributes.addFlashAttribute("mess", "Không tìm thấy hóa đơn!");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/banhang/index";
            }

            List<HoaDonChiTiet> listhdct = hoaDonChiTietService.findById(mahd);
            if (listhdct == null || listhdct.isEmpty()) {
                redirectAttributes.addFlashAttribute("mess", "Hóa đơn chưa có sản phẩm!");
                redirectAttributes.addFlashAttribute("messageType", "warning");
                return "redirect:/banhang/index?mahd=" + mahd;
            }

            BigDecimal tongTien = listhdct.stream()
                    .map(HoaDonChiTiet::getThanhTien)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Tìm voucher tốt nhất
            GiamGia voucherTotNhat = timVoucherTotNhatChoHoaDon(hoaDon, tongTien);

            if (voucherTotNhat == null) {
                if (hoaDon.getMaGiamGia() != null) {
                    hoaDon.setMaGiamGia(null);
                    hoaDonService.save(hoaDon);
                    redirectAttributes.addFlashAttribute("mess", "Đã bỏ mã giảm giá. Không có voucher nào phù hợp!");
                } else {
                    redirectAttributes.addFlashAttribute("mess", "Không có voucher nào phù hợp với hóa đơn!");
                }
                redirectAttributes.addFlashAttribute("messageType", "warning");
                return "redirect:/banhang/index?mahd=" + mahd;
            }

            // Kiểm tra nếu đã áp dụng voucher này rồi
            if (hoaDon.getMaGiamGia() != null &&
                    hoaDon.getMaGiamGia().getMaGiamGia().equals(voucherTotNhat.getMaGiamGia())) {
                redirectAttributes.addFlashAttribute("mess",
                        "Đã áp dụng voucher tốt nhất: " + voucherTotNhat.getTenGiamGia());
                redirectAttributes.addFlashAttribute("messageType", "info");
                return "redirect:/banhang/index?mahd=" + mahd;
            }

            // Áp dụng voucher mới
            hoaDon.setMaGiamGia(voucherTotNhat);
            hoaDonService.save(hoaDon);

            BigDecimal tienGiam = tinhMucGiamVoucher(voucherTotNhat, tongTien);
            redirectAttributes.addFlashAttribute("mess",
                    "✅ Đã áp dụng voucher tốt nhất: " + voucherTotNhat.getTenGiamGia() +
                            " (Giảm " + formatCurrency(tienGiam) + ")");
            redirectAttributes.addFlashAttribute("messageType", "success");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("mess", "Lỗi áp dụng voucher: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/banhang/index?mahd=" + mahd;
    }

    // ===== POST: Tạo hóa đơn mới =====
    @PostMapping("/taohd")
    public String taohd(@ModelAttribute("hoadon") HoaDon hoaDon,
                        @RequestParam("loaiBan") String loaiBan,
                        Authentication authentication,
                        RedirectAttributes redirectAttributes) {

        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("mess", "Vui lòng đăng nhập!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/login";
        }

        try {
            List<HoaDon> hoaDonCho = hoaDonService.findByTrangThai("Đang xử lý");
            if (hoaDonCho != null && hoaDonCho.size() >= MAX_HOA_DON_CHO) {
                redirectAttributes.addFlashAttribute("mess",
                        "Đã đạt tối đa " + MAX_HOA_DON_CHO + " hóa đơn chờ! Vui lòng xử lý hoặc hủy bớt.");
                redirectAttributes.addFlashAttribute("messageType", "warning");
                return "redirect:/banhang/index";
            }

            Object principal = authentication.getPrincipal();
            if (!(principal instanceof CustomUserDetails)) {
                redirectAttributes.addFlashAttribute("mess", "Lỗi xác thực người dùng!");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/login";
            }

            CustomUserDetails userDetails = (CustomUserDetails) principal;
            TaiKhoan account = userDetails.getTaiKhoan();

            if (account.getNhanVien() == null) {
                redirectAttributes.addFlashAttribute("mess", "Tài khoản chưa được gán nhân viên!");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/banhang/index";
            }

            KhachHang khachLe = getOrCreateKhachLe();

            String maHoaDon = taoMaHoaDon();
            hoaDon.setMaHoaDon(maHoaDon);
            hoaDon.setNgayTao(LocalDateTime.now());
            hoaDon.setTrangThai("Đang xử lý");
            hoaDon.setLoaiBan(loaiBan);
            hoaDon.setMaNhanVien(account.getNhanVien());
            hoaDon.setMaKhachHang(khachLe);

            if ("Online".equalsIgnoreCase(loaiBan)) {
                hoaDon.setTienShip(PHI_SHIP_MAC_DINH);
            } else {
                hoaDon.setTienShip(BigDecimal.ZERO);
            }

            HoaDon hdVuaLuu = hoaDonService.save(hoaDon);
            redirectAttributes.addFlashAttribute("mess",
                    "Tạo hóa đơn " + loaiBan + " thành công! Mã: HD" + maHoaDon);
            redirectAttributes.addFlashAttribute("messageType", "success");
            return "redirect:/banhang/index?mahd=" + hdVuaLuu.getMaHoaDon();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mess", "Lỗi tạo hóa đơn: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/banhang/index";
        }
    }

    private String taoMaHoaDon() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return "HD" + LocalDateTime.now().format(formatter);
    }

    @PostMapping("/themsphd")
    public String themSanPhamVaoHoaDon(@RequestParam("mahd") String mahd,
                                       @RequestParam("mactsp") String mactsp,
                                       @RequestParam("sluong") Integer sluong,
                                       RedirectAttributes redirectAttributes) {

        if (mahd == null || mahd.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("mess", "Vui lòng tạo hoặc chọn hóa đơn trước!");
            redirectAttributes.addFlashAttribute("messageType", "warning");
            return "redirect:/banhang/index";
        }

        if (sluong == null || sluong <= 0) {
            redirectAttributes.addFlashAttribute("mess", "Số lượng phải lớn hơn 0!");
            redirectAttributes.addFlashAttribute("messageType", "warning");
            return "redirect:/banhang/index?mahd=" + mahd;
        }

        HoaDon hdd = hoaDonService.findById(mahd);
        if (hdd == null) {
            redirectAttributes.addFlashAttribute("mess", "Hóa đơn không tồn tại!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/banhang/index";
        }

        if ("Đã thanh toán".equals(hdd.getTrangThai()) || "Đã huỷ".equals(hdd.getTrangThai())) {
            redirectAttributes.addFlashAttribute("mess",
                    "Không thể thêm sản phẩm. Hóa đơn đã " + hdd.getTrangThai() + "!");
            redirectAttributes.addFlashAttribute("messageType", "warning");
            return "redirect:/banhang/index?mahd=" + mahd;
        }

        SanPhamChiTiet spct = sanPhamChiTietService.findbyId(mactsp).orElse(null);
        if (spct == null) {
            redirectAttributes.addFlashAttribute("mess", "Sản phẩm không tồn tại!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/banhang/index?mahd=" + mahd;
        }

        if (spct.getSoLuongTon() == null || spct.getSoLuongTon() <= 0) {
            redirectAttributes.addFlashAttribute("mess", "Sản phẩm đã hết hàng!");
            redirectAttributes.addFlashAttribute("messageType", "warning");
            return "redirect:/banhang/index?mahd=" + mahd;
        }

        try {
            HoaDonChiTiet hdct = hoaDonChiTietService.findAll(mahd, mactsp);

            BigDecimal giaGoc = spct.getGiaBan() != null ? spct.getGiaBan() : BigDecimal.ZERO;
            BigDecimal giamDonVi = tinhGiamGiaSanPham(spct);
            BigDecimal giaDonVi = giaGoc.subtract(giamDonVi).max(BigDecimal.ZERO);

            if (hdct == null) {
                if (sluong > spct.getSoLuongTon()) {
                    redirectAttributes.addFlashAttribute("mess",
                            "Số lượng vượt tồn kho! Còn: " + spct.getSoLuongTon());
                    redirectAttributes.addFlashAttribute("messageType", "warning");
                    return "redirect:/banhang/index?mahd=" + mahd;
                }

                hdct = new HoaDonChiTiet();
                hdct.setMaHoaDon(hdd);
                hdct.setSanPhamChiTiet(spct);
                hdct.setSoLuong(sluong);
                hdct.setDonGia(giaDonVi);
                hdct.setTienGiam(giamDonVi.multiply(BigDecimal.valueOf(sluong)));
                hdct.setThanhTien(giaDonVi.multiply(BigDecimal.valueOf(sluong)));

                spct.setSoLuongTon(spct.getSoLuongTon() - sluong);

            } else {
                int slMoi = hdct.getSoLuong() + sluong;
                int tonKhoThuc = spct.getSoLuongTon();

                if (slMoi > tonKhoThuc + hdct.getSoLuong()) {
                    redirectAttributes.addFlashAttribute("mess",
                            "Chỉ có thể thêm tối đa " + tonKhoThuc + " sản phẩm nữa!");
                    redirectAttributes.addFlashAttribute("messageType", "warning");
                    return "redirect:/banhang/index?mahd=" + mahd;
                }

                hdct.setSoLuong(slMoi);
                hdct.setDonGia(giaDonVi);
                hdct.setTienGiam(giamDonVi.multiply(BigDecimal.valueOf(slMoi)));
                hdct.setThanhTien(giaDonVi.multiply(BigDecimal.valueOf(slMoi)));

                spct.setSoLuongTon(spct.getSoLuongTon() - sluong);
            }

            sanPhamChiTietService.capNhatTrangThaii(spct);
            sanPhamChiTietService.them(spct);
            hoaDonChiTietService.luu(hdct);

            if ("Online".equalsIgnoreCase(hdd.getLoaiBan())) {
                BigDecimal shipMoi = tinhPhiShipGHN(hdd);
                if (shipMoi != null && shipMoi.compareTo(BigDecimal.ZERO) > 0) {
                    hdd.setTienShip(shipMoi);
                    hoaDonService.save(hdd);
                }
            }

            redirectAttributes.addFlashAttribute("mess",
                    "Thêm sản phẩm thành công! Đã trừ " + sluong + " SP khỏi kho.");
            redirectAttributes.addFlashAttribute("messageType", "success");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("mess", "Lỗi khi thêm sản phẩm: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        BigDecimal giaGoc = spct.getGiaBan() != null ? spct.getGiaBan() : BigDecimal.ZERO;
        BigDecimal giamDonVi = tinhGiamGiaSanPham(spct);
        BigDecimal giaDonVi = giaGoc.subtract(giamDonVi).max(BigDecimal.ZERO);

        System.out.println("=== THEM SAN PHAM ===");
        System.out.println("Gia goc: " + giaGoc);
        System.out.println("Giam don vi: " + giamDonVi);
        System.out.println("Gia don vi sau giam: " + giaDonVi);
        System.out.println("So luong: " + sluong);
        System.out.println("Thanh tien: " + giaDonVi.multiply(BigDecimal.valueOf(sluong)));
        return "redirect:/banhang/index?mahd=" + mahd;
    }

    @PostMapping("/themsphd-ajax")
    @ResponseBody
    public ResponseEntity<?> themSanPhamAjax(@RequestParam("mahd") String mahd,
                                             @RequestParam("mactsp") String mactsp,
                                             @RequestParam("sluong") Integer sluong) {
        try {
            // ⭐ LOGIC THÊM SẢN PHẨM (GIỐNG HÀM themSanPhamVaoHoaDon)
            if (mahd == null || mahd.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lòng tạo hoặc chọn hóa đơn trước!"
                ));
            }

            if (sluong == null || sluong <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Số lượng phải lớn hơn 0!"
                ));
            }

            HoaDon hdd = hoaDonService.findById(mahd);
            if (hdd == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Hóa đơn không tồn tại!"
                ));
            }

            if ("Đã thanh toán".equals(hdd.getTrangThai()) || "Đã huỷ".equals(hdd.getTrangThai())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Không thể thêm sản phẩm. Hóa đơn đã " + hdd.getTrangThai() + "!"
                ));
            }

            SanPhamChiTiet spct = sanPhamChiTietService.findbyId(mactsp).orElse(null);
            if (spct == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Sản phẩm không tồn tại!"
                ));
            }

            if (spct.getSoLuongTon() == null || spct.getSoLuongTon() <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Sản phẩm đã hết hàng!"
                ));
            }

            HoaDonChiTiet hdct = hoaDonChiTietService.findAll(mahd, mactsp);

            BigDecimal giaGoc = spct.getGiaBan() != null ? spct.getGiaBan() : BigDecimal.ZERO;
            BigDecimal giamDonVi = tinhGiamGiaSanPham(spct);
            BigDecimal giaDonVi = giaGoc.subtract(giamDonVi).max(BigDecimal.ZERO);

            if (hdct == null) {
                if (sluong > spct.getSoLuongTon()) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "Số lượng vượt tồn kho! Còn: " + spct.getSoLuongTon()
                    ));
                }

                hdct = new HoaDonChiTiet();
                hdct.setMaHoaDon(hdd);
                hdct.setSanPhamChiTiet(spct);
                hdct.setSoLuong(sluong);
                hdct.setDonGia(giaDonVi);
                hdct.setTienGiam(giamDonVi.multiply(BigDecimal.valueOf(sluong)));
                hdct.setThanhTien(giaDonVi.multiply(BigDecimal.valueOf(sluong)));

                spct.setSoLuongTon(spct.getSoLuongTon() - sluong);

            } else {
                int slMoi = hdct.getSoLuong() + sluong;
                int tonKhoThuc = spct.getSoLuongTon();

                if (slMoi > tonKhoThuc + hdct.getSoLuong()) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "Chỉ có thể thêm tối đa " + tonKhoThuc + " sản phẩm nữa!"
                    ));
                }

                hdct.setSoLuong(slMoi);
                hdct.setDonGia(giaDonVi);
                hdct.setTienGiam(giamDonVi.multiply(BigDecimal.valueOf(slMoi)));
                hdct.setThanhTien(giaDonVi.multiply(BigDecimal.valueOf(slMoi)));

                spct.setSoLuongTon(spct.getSoLuongTon() - sluong);
            }

            sanPhamChiTietService.capNhatTrangThaii(spct);
            sanPhamChiTietService.them(spct);
            hoaDonChiTietService.luu(hdct);

            if ("Online".equalsIgnoreCase(hdd.getLoaiBan())) {
                BigDecimal shipMoi = tinhPhiShipGHN(hdd);
                if (shipMoi != null && shipMoi.compareTo(BigDecimal.ZERO) > 0) {
                    hdd.setTienShip(shipMoi);
                    hoaDonService.save(hdd);
                }
            }

            // ⭐ LẤY DỮ LIỆU MỚI ĐỂ TRẢ VỀ
            List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByMaHoaDon_MaHoaDon(mahd);
            List<Map<String, Object>> danhSachSanPham = new ArrayList<>();
            BigDecimal tongTienHang = BigDecimal.ZERO;

            for (HoaDonChiTiet ct : chiTiets) {
                Map<String, Object> item = new HashMap<>();
                SanPhamChiTiet sp = ct.getSanPhamChiTiet();
                item.put("maSanPhamChiTiet", sp.getMaSanPhamChiTiet());
                item.put("tenSanPham", sp.getSanPham().getTenSanPham()
                        + " [" + sp.getMauSac().getTenMauSac()
                        + " - " + sp.getKichThuoc().getTenKichThuoc() + "]");
                item.put("soLuong", ct.getSoLuong());
                item.put("donGia", ct.getDonGia());
                item.put("thanhTien", ct.getThanhTien());
                danhSachSanPham.add(item);
                tongTienHang = tongTienHang.add(ct.getThanhTien());
            }

            // ⭐ TRẢ VỀ DỮ LIỆU
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Thêm sản phẩm thành công!");
            response.put("danhSachSanPham", danhSachSanPham);
            response.put("tongTienHang", tongTienHang);
            response.put("maHoaDon", mahd);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/giamsp-ajax")
    @ResponseBody
    public ResponseEntity<?> giamSanPhamAjax(@RequestParam("mahd") String mahd,
                                             @RequestParam("mactsp") String mactsp) {
        try {
            HoaDon hdd = hoaDonService.findById(mahd);
            if (hdd == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Hóa đơn không tồn tại!"
                ));
            }

            HoaDonChiTiet hdct = hoaDonChiTietService.findAll(mahd, mactsp);
            if (hdct == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Sản phẩm không có trong hóa đơn!"
                ));
            }

            if (hdct.getSoLuong() <= 1) {
                // ⭐ Nếu số lượng = 1, xóa sản phẩm khỏi hóa đơn
                SanPhamChiTiet spct = hdct.getSanPhamChiTiet();
                spct.setSoLuongTon(spct.getSoLuongTon() + hdct.getSoLuong());
                sanPhamChiTietService.them(spct);

                // ⭐ Xóa bằng id (Integer)
                hoaDonChiTietService.xoa(hdct.getId());
            } else {
                // Giảm số lượng
                int slMoi = hdct.getSoLuong() - 1;
                hdct.setSoLuong(slMoi);

                // Tính lại tiền
                BigDecimal giaDonVi = hdct.getDonGia();
                BigDecimal giamDonVi = hdct.getTienGiam().divide(BigDecimal.valueOf(hdct.getSoLuong() + 1), 2, RoundingMode.HALF_UP);
                hdct.setTienGiam(giamDonVi.multiply(BigDecimal.valueOf(slMoi)));
                hdct.setThanhTien(giaDonVi.multiply(BigDecimal.valueOf(slMoi)));

                // Trả lại tồn kho
                SanPhamChiTiet spct = hdct.getSanPhamChiTiet();
                spct.setSoLuongTon(spct.getSoLuongTon() + 1);
                sanPhamChiTietService.them(spct);

                hoaDonChiTietService.luu(hdct);
            }

            // ⭐ Cập nhật tổng tiền hóa đơn
            capNhatTongTienHoaDon(mahd);

            // ⭐ Lấy dữ liệu giỏ hàng
            Map<String, Object> response = getGioHangData(mahd);
            response.put("success", true);
            response.put("message", "Đã giảm số lượng!");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi: " + e.getMessage()
            ));
        }
    }

    // Trong method getGioHangData
    private Map<String, Object> getGioHangData(String mahd) {
        Map<String, Object> data = new HashMap<>();

        HoaDon hoaDon = hoaDonService.findById(mahd);
        if (hoaDon == null) {
            data.put("danhSachSanPham", new ArrayList<>());
            data.put("tongTienHang", BigDecimal.ZERO);
            data.put("maHoaDon", mahd); // ⭐ QUAN TRỌNG
            return data;
        }

        List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByMaHoaDon_MaHoaDon(mahd);
        List<Map<String, Object>> danhSach = new ArrayList<>();
        BigDecimal tongTienHang = BigDecimal.ZERO;

        for (HoaDonChiTiet ct : chiTiets) {
            SanPhamChiTiet spct = ct.getSanPhamChiTiet();
            Map<String, Object> item = new HashMap<>();
            item.put("maSanPhamChiTiet", spct.getMaSanPhamChiTiet());
            item.put("tenSanPham", spct.getSanPham().getTenSanPham()
                    + " [" + spct.getMauSac().getTenMauSac()
                    + " - " + spct.getKichThuoc().getTenKichThuoc() + "]");
            item.put("soLuong", ct.getSoLuong());
            item.put("donGia", ct.getDonGia());
            item.put("thanhTien", ct.getThanhTien());
            item.put("tonKho", spct.getSoLuongTon() != null ? spct.getSoLuongTon() : 0);
            danhSach.add(item);
            tongTienHang = tongTienHang.add(ct.getThanhTien());
        }

        data.put("danhSachSanPham", danhSach);
        data.put("tongTienHang", tongTienHang);
        data.put("tongTienMoi", hoaDon.getTongTien());
        data.put("maHoaDon", mahd); // ⭐ QUAN TRỌNG

        return data;
    }

    private void capNhatTongTienHoaDon(String mahd) {
        HoaDon hoaDon = hoaDonService.findById(mahd);
        if (hoaDon == null) return;

        // ⭐ SỬA: Dùng repository
        List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByMaHoaDon_MaHoaDon(mahd);
        BigDecimal tongTien = BigDecimal.ZERO;

        for (HoaDonChiTiet ct : chiTiets) {
            if (ct.getThanhTien() != null) {
                tongTien = tongTien.add(ct.getThanhTien());
            }
        }

        // Trừ tiền voucher nếu có
        BigDecimal tienGiam = BigDecimal.ZERO;
        if (hoaDon.getMaGiamGia() != null) {
            tienGiam = tinhTienGiamVoucher(hoaDon, tongTien);
        }

        BigDecimal tongTienMoi = tongTien.subtract(tienGiam);
        hoaDon.setTongTien(tongTienMoi);
        hoaDonService.save(hoaDon);
    }

    @PostMapping("/giamsp")
    public String giamSoLuongSanPham(@RequestParam("mahd") String mahd,
                                     @RequestParam("mactsp") String mactsp,
                                     RedirectAttributes redirectAttributes) {

        HoaDonChiTiet hdct = hoaDonChiTietService.findAll(mahd, mactsp);
        if (hdct == null) {
            redirectAttributes.addFlashAttribute("mess", "Không tìm thấy sản phẩm trong hóa đơn!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/banhang/index?mahd=" + mahd;
        }

        SanPhamChiTiet spct = hdct.getSanPhamChiTiet();
        int slMoi = hdct.getSoLuong() - 1;

        if (slMoi <= 0) {
            spct.setSoLuongTon(spct.getSoLuongTon() + hdct.getSoLuong());
            sanPhamChiTietService.capNhatTrangThaii(spct);
            sanPhamChiTietService.them(spct);
            hoaDonChiTietService.xoa(hdct);
            redirectAttributes.addFlashAttribute("mess",
                    "Đã xóa sản phẩm khỏi hóa đơn và hoàn lại kho!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } else {
            spct.setSoLuongTon(spct.getSoLuongTon() + 1);
            sanPhamChiTietService.capNhatTrangThaii(spct);
            sanPhamChiTietService.them(spct);

            BigDecimal giaDonVi = hdct.getDonGia() != null ? hdct.getDonGia() : BigDecimal.ZERO;
            BigDecimal giamDonVi = timGiamGiaTotNhat(spct.getSanPham().getMaSanPham());

            hdct.setSoLuong(slMoi);
            hdct.setThanhTien(giaDonVi.multiply(BigDecimal.valueOf(slMoi)));
            hdct.setTienGiam(giamDonVi.multiply(BigDecimal.valueOf(slMoi)));
            hoaDonChiTietService.luu(hdct);

            redirectAttributes.addFlashAttribute("mess",
                    "Đã giảm số lượng và hoàn 1 SP vào kho!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        }

        return "redirect:/banhang/index?mahd=" + mahd;
    }


    @PostMapping("/chongg")
    public String chonGiamGia(@RequestParam("mahd") String mahd,
                              @RequestParam("magg") String magg,
                              RedirectAttributes redirectAttributes) {
        try {
            HoaDon hoaDon = hoaDonService.findById(mahd);
            if (hoaDon == null) {
                redirectAttributes.addFlashAttribute("mess", "Không tìm thấy hóa đơn!");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/banhang/index";
            }

            GiamGia giamGia = giamGiaService.getGiamGiaById(magg).orElse(null);
            if (giamGia == null) {
                redirectAttributes.addFlashAttribute("mess", "Mã giảm giá không tồn tại!");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/banhang/index?mahd=" + mahd;
            }

            // ⭐ Kiểm tra vô hạn
            if (giamGia.getIsVoHan() == null || !giamGia.getIsVoHan()) {
                if (giamGia.getSoLuong() == null || giamGia.getSoLuong() <= 0) {
                    redirectAttributes.addFlashAttribute("mess", "Mã giảm giá đã hết lượt sử dụng!");
                    redirectAttributes.addFlashAttribute("messageType", "warning");
                    return "redirect:/banhang/index?mahd=" + mahd;
                }
            }

            String trangThai = giamGiaService.tinhToanTrangThai(giamGia);
            if (!"Hoạt động".equals(trangThai)) {
                redirectAttributes.addFlashAttribute("mess", "Mã giảm giá không còn hiệu lực!");
                redirectAttributes.addFlashAttribute("messageType", "warning");
                return "redirect:/banhang/index?mahd=" + mahd;
            }

            hoaDon.setMaGiamGia(giamGia);
            hoaDonService.save(hoaDon);

            // ⭐ TÍNH LẠI TIỀN GIẢM VOUCHER
            List<HoaDonChiTiet> listhdct = hoaDonChiTietService.findById(mahd);
            BigDecimal tongTien = listhdct.stream()
                    .map(HoaDonChiTiet::getThanhTien)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal tienGiam = tinhMucGiamVoucher(giamGia, tongTien);

            redirectAttributes.addFlashAttribute("mess",
                    "Đã áp dụng mã: " + giamGia.getTenGiamGia() + " (Giảm " + formatCurrency(tienGiam) + ")");
            redirectAttributes.addFlashAttribute("messageType", "success");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mess", "Lỗi áp dụng mã giảm giá: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/banhang/index?mahd=" + mahd;
    }

    @PostMapping("/bogg")
    @ResponseBody
    @Transactional
    public Map<String, Object> boGiamGia(
            @RequestParam("mahd") String mahd,
            @RequestParam(value = "phuongthuc", required = false, defaultValue = "default") String phuongthuc,
            @RequestParam(value = "_csrf", required = false) String csrf,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {

        Map<String, Object> response = new HashMap<>();
        try {
            HoaDon hoaDon = hoaDonService.findById(mahd);
            if (hoaDon == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy hóa đơn!");
                return response;
            }

            // ⭐ CHỈ CẦN SET NULL, KHÔNG TRỪ SỐ LƯỢNG
            hoaDon.setMaGiamGia(null);
            hoaDonService.save(hoaDon);

            response.put("success", true);
            response.put("message", "Đã bỏ mã giảm giá thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi bỏ voucher: " + e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }

        return response;
    }

    @PostMapping("/bokh")
    public String boKhachHang(@RequestParam("mahd") String mahd,
                              RedirectAttributes redirectAttributes) {
        HoaDon hoaDon = hoaDonService.findById(mahd);
        if (hoaDon == null) {
            redirectAttributes.addFlashAttribute("mess", "Không tìm thấy hóa đơn!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/banhang/index";
        }

        if ("Đã thanh toán".equals(hoaDon.getTrangThai())) {
            redirectAttributes.addFlashAttribute("mess", "Không thể bỏ khách hàng của hóa đơn đã thanh toán!");
            redirectAttributes.addFlashAttribute("messageType", "warning");
            return "redirect:/banhang/index?mahd=" + mahd;
        }

        KhachHang khachLe = getOrCreateKhachLe();
        hoaDon.setMaKhachHang(khachLe);
        hoaDonService.save(hoaDon);
        redirectAttributes.addFlashAttribute("mess", "Đã chuyển về khách lẻ!");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/banhang/index?mahd=" + mahd;
    }

    @PostMapping("/taoqr")
    @ResponseBody
    public Map<String,Object> taoQR(@RequestParam("mahd") String mahd){
        Map<String,Object> response = new HashMap<>();
        try{
            HoaDon hoaDon = hoaDonService.findById(mahd);
            if(hoaDon==null){
                response.put("error","Không tìm thấy hóa đơn");
                response.put("success", false);
                return response;
            }

            List<HoaDonChiTiet> list = hoaDonChiTietService.findById(mahd);
            if(list == null || list.isEmpty()){
                response.put("error","Hóa đơn không có sản phẩm");
                response.put("success", false);
                return response;
            }

            BigDecimal tongTien = tinhTongTienHoaDon(hoaDon,list);

            String bankCode = bank;
            String accountNo = account;
            String accountNameEncoded = URLEncoder.encode(accountName.trim(), StandardCharsets.UTF_8.name());
            String orderIdEncoded = URLEncoder.encode(hoaDon.getMaHoaDon(), StandardCharsets.UTF_8.name());

            long timestamp = System.currentTimeMillis();

            // Tạo URL VietQR
            String qrUrl = String.format(
                    "https://img.vietqr.io/image/%s-%s-compact2.png?amount=%d&addInfo=%s&accountName=%s&t=%d",
                    bankCode,
                    accountNo,
                    tongTien.intValue(),
                    orderIdEncoded,
                    accountNameEncoded,
                    timestamp
            );

            System.out.println("📱 QR URL: " + qrUrl);

            // ⭐ TRẢ VỀ URL TRỰC TIẾP
            response.put("qrUrl", qrUrl);
            response.put("amount", tongTien);
            response.put("orderId", hoaDon.getMaHoaDon());
            response.put("success", true);

        }catch(Exception e){
            e.printStackTrace();
            response.put("error", e.getMessage());
            response.put("success", false);
        }
        return response;
    }

    private String generateQRCodeBase64(String data) {
        try {
            if (data == null || data.isEmpty()) {
                System.err.println("❌ Data QR rỗng!");
                return null;
            }

            System.out.println("📱 Generating QR for: " + data);

            // Tạo QR code với kích thước 400x400
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            // Thêm error correction level cao
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.H);

            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 400, 400, hints);

            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            String base64 = java.util.Base64.getEncoder().encodeToString(outputStream.toByteArray());
            System.out.println("✅ QR generated, size: " + base64.length() + " bytes");

            return base64;
        } catch (Exception e) {
            System.err.println("❌ Lỗi tạo QR: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0 VNĐ";
        java.text.NumberFormat fmt = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        return fmt.format(amount) + " VNĐ";
    }


    @PostMapping("/thanhtoan")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> thanhToan(
            @RequestParam("mahd") String mahd,
            @RequestParam("method") String method,
            @RequestParam("amount") BigDecimal amount) {

        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("========== THANH TOAN (NEW) ==========");
            System.out.println("Ma HD: " + mahd);
            System.out.println("Phuong thuc: " + method);
            System.out.println("So tien: " + amount);

            HoaDon hd = hoaDonService.findById(mahd);
            if (hd == null) {
                response.put("success", false);
                response.put("message", "Hóa đơn không tồn tại!");
                return ResponseEntity.ok(response);
            }

            if ("Đã thanh toán".equals(hd.getTrangThai())) {
                response.put("success", false);
                response.put("message", "Hóa đơn này đã được thanh toán!");
                return ResponseEntity.ok(response);
            }

            if ("Đã huỷ".equals(hd.getTrangThai())) {
                response.put("success", false);
                response.put("message", "Hóa đơn đã bị hủy, không thể thanh toán!");
                return ResponseEntity.ok(response);
            }

            if ("Online".equalsIgnoreCase(hd.getLoaiBan()) && hd.getMaKhachHang() == null) {
                response.put("success", false);
                response.put("message", "Hóa đơn Online bắt buộc phải có thông tin khách hàng!");
                return ResponseEntity.ok(response);
            }

            List<HoaDonChiTiet> listhdct = hoaDonChiTietService.findById(mahd);
            if (listhdct == null || listhdct.isEmpty()) {
                response.put("success", false);
                response.put("message", "Hóa đơn chưa có sản phẩm! Không thể thanh toán.");
                return ResponseEntity.ok(response);
            }

            // ================================================================
            // ⭐⭐ KIỂM TRA VOUCHER TRƯỚC KHI THANH TOÁN ⭐⭐
            // ================================================================
            BigDecimal tongTien = listhdct.stream()
                    .map(HoaDonChiTiet::getThanhTien)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            GiamGia currentVoucher = hd.getMaGiamGia();
            boolean voucherChanged = false;
            boolean voucherRemoved = false;
            String voucherMessage = "";

            if (currentVoucher != null) {
                System.out.println("🔍 Kiểm tra voucher hiện tại: " + currentVoucher.getMaGiamGia());

                // Kiểm tra voucher có hợp lệ không
                Map<String, Object> voucherCheck = kiemTraVoucherHienTai(currentVoucher, tongTien, hd);

                if (voucherCheck != null) {
                    // Voucher không hợp lệ
                    String errorType = (String) voucherCheck.get("type");
                    String errorMessage = (String) voucherCheck.get("message");
                    System.out.println("⚠️ Voucher không hợp lệ: " + errorType + " - " + errorMessage);

                    // Tìm voucher thay thế tốt nhất
                    GiamGia bestVoucher = timVoucherTotNhatChoHoaDon(hd, tongTien);

                    if (bestVoucher != null && !bestVoucher.getMaGiamGia().equals(currentVoucher.getMaGiamGia())) {
                        // Có voucher thay thế -> tự động áp dụng
                        hd.setMaGiamGia(bestVoucher);
                        hoaDonService.save(hd);
                        voucherChanged = true;
                        voucherMessage = "Voucher cũ không hợp lệ! Đã chuyển sang voucher: " + bestVoucher.getTenGiamGia();
                        System.out.println("✅ Đã tự động chuyển sang voucher: " + bestVoucher.getMaGiamGia());
                    } else {
                        // Không có voucher thay thế -> xóa voucher
                        hd.setMaGiamGia(null);
                        hoaDonService.save(hd);
                        voucherRemoved = true;
                        voucherMessage = "Voucher không hợp lệ! Đã xóa voucher. Lý do: " + errorMessage;
                        System.out.println("❌ Đã xóa voucher không hợp lệ");
                    }
                } else {
                    System.out.println("✅ Voucher hợp lệ");
                }
            } else {
                // Không có voucher -> tìm voucher tốt nhất để áp dụng tự động
                System.out.println("🔍 Chưa có voucher, tìm voucher tốt nhất...");
                GiamGia bestVoucher = timVoucherTotNhatChoHoaDon(hd, tongTien);
                if (bestVoucher != null) {
                    hd.setMaGiamGia(bestVoucher);
                    hoaDonService.save(hd);
                    voucherChanged = true;
                    voucherMessage = "Đã tự động áp dụng voucher tốt nhất: " + bestVoucher.getTenGiamGia();
                    System.out.println("✅ Đã tự động áp dụng voucher tốt nhất: " + bestVoucher.getMaGiamGia());
                } else {
                    System.out.println("ℹ️ Không có voucher khả dụng");
                }
            }

            // ================================================================
            // ⭐⭐ TÍNH LẠI TỔNG TIỀN SAU KHI KIỂM TRA VOUCHER ⭐⭐
            // ================================================================
            tongTien = listhdct.stream()
                    .map(HoaDonChiTiet::getThanhTien)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Trừ voucher (nếu có)
            if (hd.getMaGiamGia() != null) {
                BigDecimal tienGiam = tinhMucGiamVoucher(hd.getMaGiamGia(), tongTien);
                tongTien = tongTien.subtract(tienGiam);
                System.out.println("💰 Tiền giảm voucher: " + tienGiam);
            }

            // Cộng phí ship cho Online
            if ("Online".equalsIgnoreCase(hd.getLoaiBan())) {
                BigDecimal ship = hd.getTienShip() != null ? hd.getTienShip() : BigDecimal.ZERO;
                if (ship.compareTo(BigDecimal.ZERO) == 0) {
                    // Tính lại phí ship nếu chưa có
                    ship = tinhPhiShipGHN(hd);
                    if (ship != null && ship.compareTo(BigDecimal.ZERO) > 0) {
                        hd.setTienShip(ship);
                    }
                }
                tongTien = tongTien.add(ship);
                System.out.println("🚚 Phí ship: " + ship);
            }

            // Đảm bảo không âm
            if (tongTien.compareTo(BigDecimal.ZERO) < 0) {
                tongTien = BigDecimal.ZERO;
            }

            System.out.println("💰 Tổng tiền sau khi tính: " + tongTien);

            // ================================================================
            // ⭐⭐ KIỂM TRA TIỀN KHÁCH ĐƯA ⭐⭐
            // ================================================================
            if ("cash".equalsIgnoreCase(method) || "tienmat".equalsIgnoreCase(method)) {
                if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                    response.put("success", false);
                    response.put("message", "Vui lòng nhập số tiền khách đưa!");
                    return ResponseEntity.ok(response);
                }

                if (amount.compareTo(tongTien) < 0) {
                    BigDecimal conThieu = tongTien.subtract(amount);
                    response.put("success", false);
                    response.put("message", "Tiền khách đưa không đủ! Còn thiếu: " + formatCurrency(conThieu));
                    return ResponseEntity.ok(response);
                }

                // Tính tiền thừa
                BigDecimal tienThua = amount.subtract(tongTien);
                hd.setTienThua(tienThua);
            } else {
                // Chuyển khoản hoặc COD
                hd.setTienThua(BigDecimal.ZERO);
            }

            // ================================================================
            // ⭐⭐ GIẢM SỐ LƯỢNG VOUCHER ⭐⭐
            // ================================================================
            if (hd.getMaGiamGia() != null) {
                GiamGia gg = hd.getMaGiamGia();
                if (gg.getIsVoHan() == null || !gg.getIsVoHan()) {
                    if (gg.getSoLuong() != null && gg.getSoLuong() > 0) {
                        giamGiaService.giamSoLuongVoucher(gg.getMaGiamGia());
                        System.out.println("📉 Đã giảm số lượng voucher: " + gg.getMaGiamGia());
                    }
                }
            }

            // ================================================================
            // ⭐⭐ CẬP NHẬT HÓA ĐƠN ⭐⭐
            // ================================================================
            hd.setTongTien(tongTien);
            hd.setTienKhachDua(amount);
            hd.setPhuongThucThanhToan(getPhuongThucText(method));
            hd.setNgayThanhToan(LocalDateTime.now());

            // Cập nhật trạng thái theo loại bán
            if ("Online".equalsIgnoreCase(hd.getLoaiBan())) {
                // Đơn hàng online -> Đã xác nhận
                hd.setTrangThai("Đã xác nhận");
                System.out.println("✅ Đơn hàng Online đã xác nhận!");
            } else {
                // Đơn hàng tại quầy -> Đã thanh toán
                hd.setTrangThai("Đã thanh toán");
                System.out.println("✅ Đơn hàng tại quầy đã thanh toán!");
            }

            hoaDonService.save(hd);

            System.out.println("✅ Thanh toan thanh cong!");
            System.out.println("Trang thai: " + hd.getTrangThai());
            System.out.println("========== END THANH TOAN ==========");

            // ================================================================
            // ⭐⭐ TRẢ VỀ RESPONSE ⭐⭐
            // ================================================================
            response.put("success", true);
            response.put("message", "Thanh toán thành công!");
            response.put("maHoaDon", mahd);
            response.put("trangThai", hd.getTrangThai());
            response.put("tongTien", tongTien);
            response.put("tienThua", hd.getTienThua());

            // Thêm thông tin về voucher
            if (voucherChanged) {
                response.put("voucherChanged", true);
                response.put("voucherMessage", voucherMessage);
            }
            if (voucherRemoved) {
                response.put("voucherRemoved", true);
                response.put("voucherMessage", voucherMessage);
            }
            if (hd.getMaGiamGia() != null) {
                response.put("voucherApplied", hd.getMaGiamGia().getMaGiamGia());
                response.put("voucherName", hd.getMaGiamGia().getTenGiamGia());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi thanh toán: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    private String getPhuongThucText(String method) {
        if ("cash".equalsIgnoreCase(method) || "tienmat".equalsIgnoreCase(method)) {
            return "Tiền mặt";
        } else if ("transfer".equalsIgnoreCase(method) || "chuyenkhoan".equalsIgnoreCase(method)) {
            return "Chuyển khoản";
        } else if ("cod".equalsIgnoreCase(method)) {
            return "COD";
        }
        return method;
    }

    @PostMapping("/thanhtoanhd")
    public String thanhToanHoaDon(@RequestParam("mahd") String mahd,
                                  @RequestParam(value = "tienkhachdua", required = false) BigDecimal tienkhachdua,
                                  @RequestParam("phuongthuc") String phuongthuc,
                                  RedirectAttributes redirectAttributes) {

        HoaDon hd = hoaDonService.findById(mahd);
        if (hd == null) {
            redirectAttributes.addFlashAttribute("mess", "Hóa đơn không tồn tại!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/banhang/index";
        }

        if ("Đã thanh toán".equals(hd.getTrangThai())) {
            redirectAttributes.addFlashAttribute("mess", "Hóa đơn này đã được thanh toán!");
            redirectAttributes.addFlashAttribute("messageType", "warning");
            return "redirect:/banhang/index?mahd=" + mahd;
        }

        if ("Đã huỷ".equals(hd.getTrangThai())) {
            redirectAttributes.addFlashAttribute("mess", "Hóa đơn đã bị hủy, không thể thanh toán!");
            redirectAttributes.addFlashAttribute("messageType", "warning");
            return "redirect:/banhang/index";
        }

        if ("Online".equalsIgnoreCase(hd.getLoaiBan()) && hd.getMaKhachHang() == null) {
            redirectAttributes.addFlashAttribute("mess",
                    "Hóa đơn Online bắt buộc phải có thông tin khách hàng!");
            redirectAttributes.addFlashAttribute("messageType", "warning");
            return "redirect:/banhang/index?mahd=" + mahd;
        }

        List<HoaDonChiTiet> listhdct = hoaDonChiTietService.findById(mahd);
        if (listhdct == null || listhdct.isEmpty()) {
            redirectAttributes.addFlashAttribute("mess",
                    "Hóa đơn chưa có sản phẩm! Không thể thanh toán.");
            redirectAttributes.addFlashAttribute("messageType", "warning");
            return "redirect:/banhang/index?mahd=" + mahd;
        }

        if ("COD".equalsIgnoreCase(phuongthuc)) {
            tienkhachdua = BigDecimal.ZERO;
        }

        BigDecimal tongTien = tinhTongTienHoaDon(hd, listhdct);

        if ("Chuyển khoản".equalsIgnoreCase(phuongthuc)) {
            tienkhachdua = tongTien;
        }

        if (!"COD".equalsIgnoreCase(phuongthuc)) {
            if (tienkhachdua == null || tienkhachdua.compareTo(tongTien) < 0) {
                BigDecimal conThieu = tongTien.subtract(tienkhachdua != null ? tienkhachdua : BigDecimal.ZERO);
                redirectAttributes.addFlashAttribute("mess",
                        "Tiền khách đưa không đủ! Còn thiếu: " + formatCurrency(conThieu));
                redirectAttributes.addFlashAttribute("messageType", "warning");
                return "redirect:/banhang/index?mahd=" + mahd;
            }
        }

        try {
            if (hd.getMaGiamGia() != null) {
                GiamGia gg = hd.getMaGiamGia();
                if (gg.getSoLuong() != null && gg.getSoLuong() > 0) {
                    giamGiaService.giamSoLuongVoucher(gg.getMaGiamGia());
                }
            }

            if ("Online".equalsIgnoreCase(hd.getLoaiBan()) && !"COD".equalsIgnoreCase(phuongthuc)) {
                BigDecimal ship = tinhPhiShipGHN(hd);
                if (ship != null && ship.compareTo(BigDecimal.ZERO) > 0) {
                    hd.setTienShip(ship);
                    tongTien = tongTien.add(ship);
                }
            } else if ("COD".equalsIgnoreCase(phuongthuc)) {
                hd.setTienShip(BigDecimal.ZERO);
            }

            hd.setTongTien(tongTien);
            hd.setTienKhachDua(tienkhachdua);

            if ("COD".equalsIgnoreCase(phuongthuc)) {
                hd.setTienThua(BigDecimal.ZERO);
            } else {
                hd.setTienThua(tienkhachdua.subtract(tongTien));
            }

            hd.setPhuongThucThanhToan(phuongthuc);
            hd.setNgayThanhToan(LocalDateTime.now());

            // ⭐ SỬA: Cập nhật trạng thái theo loại bán
            if ("Online".equalsIgnoreCase(hd.getLoaiBan())) {
                // Đơn hàng online -> Đã xác nhận
                hd.setTrangThai("Đã xác nhận");
                redirectAttributes.addFlashAttribute("mess",
                        "Đơn hàng HD" + mahd + " đã xác nhận thành công!");
            } else {
                // Đơn hàng tại quầy -> Đã thanh toán
                hd.setTrangThai("Đã thanh toán");
                redirectAttributes.addFlashAttribute("mess",
                        "Thanh toán thành công! Tiền thừa: " + formatCurrency(hd.getTienThua()));
            }
            redirectAttributes.addFlashAttribute("messageType", "success");
            hoaDonService.save(hd);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mess", "Lỗi thanh toán: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/banhang/index?mahd=" + mahd;
        }

        return "redirect:/banhang/index";
    }

    @PostMapping("/huyhd")
    public String huyHoaDon(@RequestParam("mahd") String mahd,
                            RedirectAttributes redirectAttributes) {
        HoaDon hd = hoaDonService.findById(mahd);

        if (hd == null) {
            redirectAttributes.addFlashAttribute("mess", "Hóa đơn không tồn tại!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/banhang/index";
        }

        if ("Đã thanh toán".equals(hd.getTrangThai())) {
            redirectAttributes.addFlashAttribute("mess",
                    "Không thể hủy hóa đơn đã thanh toán!");
            redirectAttributes.addFlashAttribute("messageType", "warning");
            return "redirect:/banhang/index?mahd=" + mahd;
        }

        if ("Đã huỷ".equals(hd.getTrangThai())) {
            redirectAttributes.addFlashAttribute("mess", "Hóa đơn này đã bị hủy rồi!");
            redirectAttributes.addFlashAttribute("messageType", "warning");
            return "redirect:/banhang/index";
        }

        List<HoaDonChiTiet> listhdct = hoaDonChiTietService.findById(mahd);
        int tongSPHoanLai = 0;
        if (listhdct != null && !listhdct.isEmpty()) {
            for (HoaDonChiTiet dc : listhdct) {
                SanPhamChiTiet spct = dc.getSanPhamChiTiet();
                spct.setSoLuongTon(spct.getSoLuongTon() + dc.getSoLuong());
                sanPhamChiTietService.capNhatTrangThaii(spct);
                sanPhamChiTietService.them(spct);
                tongSPHoanLai += dc.getSoLuong();
            }
        }

        hd.setTrangThai("Đã huỷ");
        hoaDonService.save(hd);
        redirectAttributes.addFlashAttribute("mess",
                "Hủy hóa đơn thành công! Đã hoàn " + tongSPHoanLai + " SP vào kho.");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/banhang/index";
    }

    @PostMapping("/themkh")
    @Transactional
    public String themKhachHang(@Valid @ModelAttribute("kh") KhachHang kh,
                                BindingResult bindingResult,
                                @RequestParam(value = "mahd", required = false) String mahd,
                                @RequestParam(value = "ghiChuGiaoHang", required = false) String ghiChuGiaoHang,
                                RedirectAttributes redirectAttributes) {

        String redirectUrl = (mahd != null && !mahd.trim().isEmpty())
                ? "redirect:/banhang/index?mahd=" + mahd
                : "redirect:/banhang/index";

        if ("0000000000".equals(kh.getSdt())) {
            redirectAttributes.addFlashAttribute("mess", "Không thể tạo khách hàng với SĐT mặc định!");
            redirectAttributes.addFlashAttribute("messageType", "warning");
            return redirectUrl;
        }

        if (bindingResult.hasErrors()) {
            String loiDauTien = bindingResult.getFieldErrors().stream()
                    .map(fe -> fe.getDefaultMessage())
                    .findFirst().orElse("Dữ liệu không hợp lệ");
            redirectAttributes.addFlashAttribute("mess", "Lỗi: " + loiDauTien);
            redirectAttributes.addFlashAttribute("messageType", "error");
            return redirectUrl;
        }

        if (khachHangService.existsBySdt(kh.getSdt())) {
            redirectAttributes.addFlashAttribute("mess",
                    "Số điện thoại " + kh.getSdt() + " đã được đăng ký!");
            redirectAttributes.addFlashAttribute("messageType", "warning");
            return redirectUrl;
        }

        if (kh.getNgayDangKy() == null) {
            kh.setNgayDangKy(LocalDate.now());
        }

        kh.setMaKH(taoMaKhachHang());

        KhachHang khachHangDaLuu = khachHangService.save(kh);

        if (mahd != null && !mahd.trim().isEmpty()) {
            HoaDon hd = hoaDonService.findById(mahd);
            if (hd != null) {
                if (hd.getMaKhachHang() != null && "0000000000".equals(hd.getMaKhachHang().getSdt())) {
                    hd.setMaKhachHang(khachHangDaLuu);
                    if (ghiChuGiaoHang != null && !ghiChuGiaoHang.trim().isEmpty()) {
                        hd.setGhiChu(ghiChuGiaoHang);
                    }
                    hoaDonService.save(hd);
                    redirectAttributes.addFlashAttribute("mess",
                            "Thêm & gán khách hàng " + khachHangDaLuu.getHoTen() + " vào hóa đơn thành công!");
                    redirectAttributes.addFlashAttribute("messageType", "success");
                    return redirectUrl;
                }
            }
        }

        redirectAttributes.addFlashAttribute("mess", "Thêm khách hàng mới thành công!");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return redirectUrl;
    }

    private String taoMaKhachHang() {
        Random random = new Random();
        int soNgauNhien = 1000 + random.nextInt(9000);
        return "KH" + soNgauNhien;
    }

    @PostMapping("/chonkh")
    public String chonKhachHang(@RequestParam("mahd") String mahd,
                                @RequestParam("makh") String makh,
                                RedirectAttributes redirectAttributes) {
        HoaDon hd = hoaDonService.findById(mahd);
        if (hd == null) {
            redirectAttributes.addFlashAttribute("mess", "Hóa đơn không tồn tại!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/banhang/index";
        }

        KhachHang kh = khachHangService.getKhachHangById(makh);
        if (kh == null) {
            redirectAttributes.addFlashAttribute("mess", "Khách hàng không tồn tại!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/banhang/index?mahd=" + mahd;
        }

        if ("0000000000".equals(kh.getSdt())) {
            redirectAttributes.addFlashAttribute("mess", "Không thể chọn khách hàng mặc định!");
            redirectAttributes.addFlashAttribute("messageType", "warning");
            return "redirect:/banhang/index?mahd=" + mahd;
        }

        hd.setMaKhachHang(kh);
        hoaDonService.save(hd);

        // ⭐ THÊM: Lưu thông tin khách hàng vào session để JS lấy
        redirectAttributes.addFlashAttribute("khachHangSelected", kh.getMaKH());
        redirectAttributes.addFlashAttribute("khachHangTen", kh.getHoTen());
        redirectAttributes.addFlashAttribute("mess", "Đã chọn khách hàng: " + kh.getHoTen());
        redirectAttributes.addFlashAttribute("messageType", "success");

        return "redirect:/banhang/index?mahd=" + mahd;
    }

    @PostMapping("/capnhatghichu")
    public String capNhatGhiChu(@RequestParam("mahd") String mahd,
                                @RequestParam("ghiChu") String ghiChu,
                                RedirectAttributes redirectAttributes) {
        HoaDon hd = hoaDonService.findById(mahd);
        if (hd != null) {
            hd.setGhiChu(ghiChu);
            hoaDonService.save(hd);
            redirectAttributes.addFlashAttribute("mess", "Đã cập nhật ghi chú!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        }
        return "redirect:/banhang/index?mahd=" + mahd;
    }

    @GetMapping("/inhoadon/{id}")
    public String inHoaDon(@PathVariable("id") String id, Model model) {
        HoaDon hoaDon = hoaDonService.findById(id);
        if (hoaDon == null) {
            return "redirect:/banhang/index";
        }

        List<HoaDonChiTiet> listHdct = hoaDonChiTietService.findByHoaDOn(hoaDon);
        if (listHdct == null) listHdct = new ArrayList<>();

        BigDecimal tongTien = listHdct.stream()
                .map(item -> item.getThanhTien() != null ? item.getThanhTien() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("hd", hoaDon);
        model.addAttribute("listHdct", listHdct);
        model.addAttribute("tongTien", tongTien);

        return "inhoadon";
    }

    @GetMapping("/khachhang")
    public String khachhang(@RequestParam(value = "sdt", required = false) String sdt, Model model) {
        List<KhachHang> kh = (sdt == null || sdt.trim().isEmpty())
                ? khachHangService.getAllKhachHang()
                : khachHangService.findAllBySdt(sdt);
        model.addAttribute("listkh", kh != null ? kh : new ArrayList<>());
        model.addAttribute("kh", new KhachHang());
        return "banhang/index";
    }

    @GetMapping("/test-ghn-token")
    @ResponseBody
    public Map<String, Object> testGHNToken() {
        Map<String, Object> result = new HashMap<>();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Token", ghnShippingService.getApiToken());
            headers.set("ShopId", String.valueOf(ghnShippingService.getShopId()));

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shop/all",
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            result.put("status", response.getStatusCode().value());
            result.put("body", response.getBody());
            result.put("success", response.getStatusCode().value() == 200);

            if (response.getStatusCode().value() == 200) {
                JsonNode root = objectMapper.readTree(response.getBody());
                result.put("shopName", root.path("data").path("shop_name").asText());
                result.put("message", "Token hợp lệ!");
            } else {
                result.put("message", "Token không hợp lệ!");
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("message", "Lỗi kiểm tra token: " + e.getMessage());
        }
        return result;
    }

    /**
     * Lấy danh sách quận/huyện từ GHN
     */
    @GetMapping("/ghn/districts")
    @ResponseBody
    public Map<String, Object> getGHNDistricts(@RequestParam(defaultValue = "1") int provinceId) {
        Map<String, Object> result = new HashMap<>();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Token", ghnShippingService.getApiToken());

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    "https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/district?province_id=" + provinceId,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            result.put("success", true);
            result.put("status", response.getStatusCode().value());

            if (response.getStatusCode().value() == 200) {
                JsonNode root = objectMapper.readTree(response.getBody());
                result.put("data", root.path("data"));
                result.put("message", "Lấy danh sách quận/huyện thành công!");
            } else {
                result.put("message", "Lỗi lấy danh sách quận/huyện!");
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * Test tính phí ship trực tiếp với GHN
     */
    @GetMapping("/test-ship-fee")
    @ResponseBody
    public Map<String, Object> testShipFee(
            @RequestParam(defaultValue = "Ba Đình") String toDistrict,
            @RequestParam(defaultValue = "Điện Biên") String toWard,
            @RequestParam(defaultValue = "1000") int weight,
            @RequestParam(defaultValue = "500000") int amount) {

        Map<String, Object> result = new HashMap<>();
        result.put("request", Map.of(
                "toDistrict", toDistrict,
                "toWard", toWard,
                "weight", weight,
                "amount", amount
        ));

        try {
            System.out.println("========== TEST SHIP FEE ==========");
            System.out.println("📍 Quận: " + toDistrict);
            System.out.println("📍 Phường: " + toWard);
            System.out.println("⚖️ Cân nặng: " + weight + "g");
            System.out.println("💰 Số tiền: " + amount + "đ");

            // Test với GHN API
            BigDecimal ghnFee = ghnShippingService.calculateShippingFeeDefault(
                    toDistrict, toWard, weight, BigDecimal.valueOf(amount)
            );

            // Test fallback
            BigDecimal fallbackFee = ghnShippingService.calculateFallbackShippingFee(
                    BigDecimal.valueOf(amount), weight
            );

            result.put("ghnShippingFee", ghnFee);
            result.put("fallbackFee", fallbackFee);
            result.put("success", true);
            result.put("message", "Tính phí ship thành công!");

            System.out.println("🚚 GHN Fee: " + ghnFee);
            System.out.println("🔄 Fallback Fee: " + fallbackFee);
            System.out.println("========== END TEST ==========");

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("message", "Lỗi tính phí ship: " + e.getMessage());
        }

        return result;
    }

    /**
     * Test tính phí ship cho một hóa đơn cụ thể
     */
    @GetMapping("/test-ship-invoice")
    @ResponseBody
    public Map<String, Object> testShipInvoice(@RequestParam String mahd) {
        Map<String, Object> result = new HashMap<>();

        try {
            HoaDon hoaDon = hoaDonService.findById(mahd);
            if (hoaDon == null) {
                result.put("success", false);
                result.put("message", "Không tìm thấy hóa đơn!");
                return result;
            }

            result.put("invoice", Map.of(
                    "maHoaDon", hoaDon.getMaHoaDon(),
                    "loaiBan", hoaDon.getLoaiBan(),
                    "khachHang", hoaDon.getMaKhachHang() != null ? hoaDon.getMaKhachHang().getHoTen() : "null"
            ));

            // Lấy địa chỉ
            DiaChi diaChi = null;
            if (hoaDon.getMaKhachHang() != null) {
                diaChi = diaChiService.findByKhachHangAndDiaChiMacDinh(
                        hoaDon.getMaKhachHang(), true
                );
                if (diaChi == null) {
                    List<DiaChi> list = diaChiService.findByKhachHang(hoaDon.getMaKhachHang());
                    if (list != null && !list.isEmpty()) {
                        diaChi = list.get(0);
                    }
                }
            }

            if (diaChi == null) {
                result.put("success", false);
                result.put("message", "Khách hàng chưa có địa chỉ!");
                return result;
            }

            result.put("address", Map.of(
                    "diaChi", diaChi.getDiaChiCuThe(),
                    "phuongXa", diaChi.getPhuongXa(),
                    "quanHuyen", diaChi.getQuanHuyen(),
                    "tinhThanh", diaChi.getTinhThanh()
            ));

            // Tính phí ship
            BigDecimal fee = tinhPhiShipGHN(hoaDon);
            result.put("shippingFee", fee);
            result.put("success", true);
            result.put("message", "Tính phí ship thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    // ===== PRIVATE METHODS =====

    private BigDecimal tinhPhiShipGHN(HoaDon hoaDon) {
        try {
            System.out.println("========== TÍNH PHÍ SHIP GHN ==========");
            System.out.println("📦 Hóa đơn: " + hoaDon.getMaHoaDon());
            System.out.println("👤 Khách hàng: " + (hoaDon.getMaKhachHang() != null ? hoaDon.getMaKhachHang().getHoTen() : "null"));

            if (hoaDon.getMaKhachHang() == null || !"Online".equalsIgnoreCase(hoaDon.getLoaiBan())) {
                System.out.println("⚠️ Không phải đơn Online hoặc chưa có khách hàng");
                return PHI_SHIP_MAC_DINH;
            }

            KhachHang khachHang = hoaDon.getMaKhachHang();

            if ("0000000000".equals(khachHang.getSdt())) {
                System.out.println("⚠️ Khách hàng là khách lẻ");
                return PHI_SHIP_MAC_DINH;
            }

                DiaChi diaChiMacDinh = diaChiService.findByKhachHangAndDiaChiMacDinh(khachHang, true);

            if (diaChiMacDinh == null) {
                List<DiaChi> danhSachDiaChi = diaChiService.findByKhachHang(khachHang);
                if (danhSachDiaChi == null || danhSachDiaChi.isEmpty()) {
                    System.out.println("⚠️ Không có địa chỉ nào");
                    return PHI_SHIP_MAC_DINH;
                }
                diaChiMacDinh = danhSachDiaChi.get(0);
            }

            List<HoaDonChiTiet> listhdct = hoaDonChiTietService.findById(hoaDon.getMaHoaDon());
            if (listhdct == null || listhdct.isEmpty()) {
                System.out.println("⚠️ Hóa đơn không có sản phẩm");
                return PHI_SHIP_MAC_DINH;
            }

            int totalWeight = listhdct.stream()
                    .mapToInt(HoaDonChiTiet::getSoLuong)
                    .sum() * 500;

            BigDecimal tongTien = listhdct.stream()
                    .map(HoaDonChiTiet::getThanhTien)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String quanHuyen = diaChiMacDinh.getQuanHuyen();
            String phuongXa = diaChiMacDinh.getPhuongXa();

            System.out.println("📍 Quận/Huyện: " + quanHuyen);
            System.out.println("📍 Phường/Xã: " + phuongXa);
            System.out.println("⚖️ Tổng cân nặng: " + totalWeight + "g");
            System.out.println("💰 Tổng tiền: " + tongTien);

            if (quanHuyen == null || quanHuyen.trim().isEmpty()) {
                System.out.println("⚠️ Không có thông tin quận/huyện");
                return PHI_SHIP_MAC_DINH;
            }

            String diaChiDayDu = diaChiMacDinh.getDiaChiCuThe() + ", " +
                    phuongXa + ", " +
                    quanHuyen + ", " +
                    diaChiMacDinh.getTinhThanh();

            if (hoaDon.getGhiChu() == null || hoaDon.getGhiChu().trim().isEmpty()) {
                hoaDon.setGhiChu("Địa chỉ giao hàng: " + diaChiDayDu +
                        " | Người nhận: " + diaChiMacDinh.getTenNguoiNhan() +
                        " | SĐT: " + diaChiMacDinh.getSoDienThoaiNguoiNhan());
                hoaDonService.save(hoaDon);
            }

            BigDecimal shippingFee = ghnShippingService.calculateShippingFeeDefault(
                    quanHuyen,
                    phuongXa,
                    totalWeight,
                    tongTien
            );

            System.out.println("🚚 Phí ship tính được: " + shippingFee);
            System.out.println("========== END TÍNH PHÍ SHIP ==========");

            // ✅ SỬA: KHÔNG dùng max với PHI_SHIP_MAC_DINH nữa
            // return shippingFee.max(PHI_SHIP_MAC_DINH);

            // Nếu API thành công, dùng kết quả API
            if (shippingFee != null && shippingFee.compareTo(BigDecimal.ZERO) > 0) {
                return shippingFee;
            }

            // Chỉ dùng PHI_SHIP_MAC_DINH khi API thất bại
            return PHI_SHIP_MAC_DINH;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Lỗi tính phí ship, sử dụng fallback: " + e.getMessage());
            return PHI_SHIP_MAC_DINH;
        }
    }

    private List<GiamGia> timVoucherTotNhat(BigDecimal tongTien) {
        List<GiamGia> allVouchers = giamGiaService.getGiamGia1();
        if (allVouchers == null || allVouchers.isEmpty()) return new ArrayList<>();

        return allVouchers.stream()
                .filter(gg -> gg.getSoLuong() != null && gg.getSoLuong() > 0)
                .filter(gg -> "Hoạt động".equals(giamGiaService.tinhToanTrangThai(gg)))
                .sorted((a, b) -> {
                    BigDecimal giamA = tinhMucGiamVoucher(a, tongTien);
                    BigDecimal giamB = tinhMucGiamVoucher(b, tongTien);
                    return giamB.compareTo(giamA);
                })
                .limit(3)
                .collect(Collectors.toList());
    }

    private BigDecimal timGiamGiaTotNhat(String maSanPham) {
        List<DotGiamGia> listDgg = dotGiamGiaService.getBymasp(maSanPham);
        if (listDgg == null || listDgg.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return listDgg.stream()
                .filter(dgg -> "Hoạt động".equals(dgg.getTrangThai()))
                .filter(dgg -> dgg.getGiaTriGiam() != null)
                .filter(dgg -> dgg.getGiaTriGiam().compareTo(BigDecimal.ZERO) > 0)
                .map(dgg -> {
                    BigDecimal giam = dgg.getGiaTriGiam();
                    if (giam.compareTo(BigDecimal.valueOf(100)) <= 0) {
                        return giam;
                    }
                    return giam;
                })
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal tinhTienGiamVoucher(HoaDon hoaDon, BigDecimal tongTien) {
        if (hoaDon == null || hoaDon.getMaGiamGia() == null) return BigDecimal.ZERO;

        GiamGia gg = hoaDon.getMaGiamGia();

        // ⭐ LOG ĐỂ DEBUG
        System.out.println("=== TINH TIEN GIAM VOUCHER ===");
        System.out.println("Ma Giam Gia: " + gg.getMaGiamGia());
        System.out.println("Ten Giam Gia: " + gg.getTenGiamGia());
        System.out.println("Loai Giam Gia: " + gg.getLoaiGiamGia());
        System.out.println("Gia Tri Giam: " + gg.getGiaTriGiam());
        System.out.println("Giam Toi Da: " + gg.getGiamToiDa());
        System.out.println("Tong Tien: " + tongTien);

        // ⭐ KIỂM TRA VÔ HẠN - BỎ QUA KIỂM TRA SỐ LƯỢNG
        if (gg.getIsVoHan() == null || !gg.getIsVoHan()) {
            if (gg.getSoLuong() == null || gg.getSoLuong() <= 0) {
                System.out.println("-> Voucher het luot, tra ve 0");
                return BigDecimal.ZERO;
            }
        } else {
            System.out.println("-> Voucher vo han, bo qua kiem tra so luong");
        }

        // ⭐ KIỂM TRA TRẠNG THÁI
        String trangThai = giamGiaService.tinhToanTrangThai(gg);
        System.out.println("Trang Thai: " + trangThai);
        if (!"Hoạt động".equals(trangThai)) {
            System.out.println("-> Voucher khong hoat dong, tra ve 0");
            return BigDecimal.ZERO;
        }

        BigDecimal tienGiam = tinhMucGiamVoucher(gg, tongTien);
        System.out.println("-> Tien giam: " + tienGiam);
        System.out.println("=== END TINH TIEN GIAM VOUCHER ===");

        return tienGiam;
    }

    private BigDecimal tinhTongTienHoaDon(HoaDon hd, List<HoaDonChiTiet> listhdct) {
        if (listhdct == null || listhdct.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal tongTien = listhdct.stream()
                .map(HoaDonChiTiet::getThanhTien)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        tongTien = tongTien.subtract(tinhTienGiamVoucher(hd, tongTien));

        if ("Online".equalsIgnoreCase(hd.getLoaiBan())) {
            BigDecimal ship = tinhPhiShipGHN(hd);
            if (ship == null || ship.compareTo(BigDecimal.ZERO) <= 0) {
                ship = PHI_SHIP_MAC_DINH;
            }
            hd.setTienShip(ship);
            tongTien = tongTien.add(ship);
        }

        return tongTien.max(BigDecimal.ZERO);
    }

    private BigDecimal tinhGiamGiaSanPham(SanPhamChiTiet spct) {
        if (spct == null || spct.getSanPham() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal giaGoc = spct.getGiaBan() != null ? spct.getGiaBan() : BigDecimal.ZERO;
        BigDecimal giamMax = BigDecimal.ZERO;

        List<DotGiamGia> listDgg = dotGiamGiaService.getBymasp(spct.getSanPham().getMaSanPham());
        if (listDgg == null || listDgg.isEmpty()) {
            System.out.println("-> Khong co dot giam gia cho san pham: " + spct.getSanPham().getMaSanPham());
            return BigDecimal.ZERO;
        }

        System.out.println("=== TINH GIAM GIA SAN PHAM ===");
        System.out.println("Ma SP: " + spct.getSanPham().getMaSanPham());
        System.out.println("Gia goc: " + giaGoc);

        for (DotGiamGia dgg : listDgg) {
            System.out.println("  Dot giam: " + dgg.getMaGiamGia());
            System.out.println("  Gia tri giam: " + dgg.getGiaTriGiam());
            System.out.println("  Trang thai: " + dgg.getTrangThai());

            if (!"Hoạt động".equals(dgg.getTrangThai())) {
                System.out.println("  -> Bo qua (khong hoat dong)");
                continue;
            }

            if (dgg.getGiaTriGiam() == null || dgg.getGiaTriGiam().compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("  -> Bo qua (gia tri giam <= 0)");
                continue;
            }

            BigDecimal giam = dgg.getGiaTriGiam();
            BigDecimal giamTinh = BigDecimal.ZERO;

            // Nếu giá trị giảm <= 100 -> là phần trăm
            if (giam.compareTo(BigDecimal.valueOf(100)) <= 0) {
                BigDecimal phanTram = giam.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                giamTinh = giaGoc.multiply(phanTram);
                System.out.println("  Giam phan tram: " + giam + "% => " + giamTinh);
            } else {
                giamTinh = giam;
                System.out.println("  Giam so tien: " + giamTinh);
            }

            if (giamTinh.compareTo(giamMax) > 0) {
                giamMax = giamTinh;
                System.out.println("  -> Giam max hien tai: " + giamMax);
            }
        }

        System.out.println("=> Giam max: " + giamMax);
        System.out.println("=== END TINH GIAM GIA SAN PHAM ===");

        return giamMax.setScale(0, RoundingMode.HALF_UP);
    }

    @PostMapping("/tinh-phi-ship")
    @ResponseBody
    public Map<String, Object> tinhPhiShip(
            @RequestParam String district,
            @RequestParam String ward,
            @RequestParam BigDecimal tongTien,
            @RequestParam(defaultValue = "500") int weight) {

        Map<String, Object> map = new HashMap<>();

        BigDecimal phiShip = ghnShippingService.calculateShippingFeeDefault(
                district,
                ward,
                weight,
                tongTien
        );

        map.put("shippingFee", phiShip);
        map.put("success", true);

        return map;
    }


    private List<GiamGia> getVoucherDangHoatDong() {
        List<GiamGia> allVouchers = giamGiaService.getGiamGia1();
        if (allVouchers == null || allVouchers.isEmpty()) {
            return new ArrayList<>();
        }

        return allVouchers.stream()
                .filter(gg -> {
                    if (gg.getIsVoHan() != null && gg.getIsVoHan()) {
                        return true;
                    }
                    return gg.getSoLuong() != null && gg.getSoLuong() > 0;
                })
                .filter(gg -> "Hoạt động".equals(giamGiaService.tinhToanTrangThai(gg)))
                .sorted((a, b) -> {
                    BigDecimal giamA = a.getGiaTriGiam() != null ? a.getGiaTriGiam() : BigDecimal.ZERO;
                    BigDecimal giamB = b.getGiaTriGiam() != null ? b.getGiaTriGiam() : BigDecimal.ZERO;

                    if (a.getLoaiGiamGia().equals(b.getLoaiGiamGia())) {
                        return giamB.compareTo(giamA);
                    }
                    if ("Tien".equalsIgnoreCase(a.getLoaiGiamGia())) {
                        return -1;
                    }
                    if ("Tien".equalsIgnoreCase(b.getLoaiGiamGia())) {
                        return 1;
                    }
                    return giamB.compareTo(giamA);
                })
                .collect(Collectors.toList());
    }


    private GiamGia timVoucherTotNhatChoHoaDon(HoaDon hoaDon, BigDecimal tongTien) {
        List<GiamGia> vouchers;
        KhachHang khachHang = hoaDon != null ? hoaDon.getMaKhachHang() : null;

        if (khachHang != null && !"0000000000".equals(khachHang.getSdt())) {
            vouchers = getVoucherChoKhachHang(khachHang);
        } else {
            vouchers = getVoucherCongKhai();
        }

        if (vouchers.isEmpty() || tongTien == null || tongTien.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        GiamGia bestVoucher = null;
        BigDecimal maxGiam = BigDecimal.ZERO;
        GiamGia currentVoucher = hoaDon != null ? hoaDon.getMaGiamGia() : null;

        for (GiamGia gg : vouchers) {
            // Bỏ qua voucher hiện tại
            if (currentVoucher != null && currentVoucher.getMaGiamGia().equals(gg.getMaGiamGia())) {
                continue;
            }

            // ⭐ KIỂM TRA TRẠNG THÁI: Chỉ lấy "Hoạt động"
            String trangThai = giamGiaService.tinhToanTrangThai(gg);
            if (!"Hoạt động".equals(trangThai)) {
                continue;
            }

            // Kiểm tra đơn tối thiểu
            if (gg.getDonToiThieu() != null && gg.getDonToiThieu().compareTo(BigDecimal.ZERO) > 0
                    && tongTien.compareTo(gg.getDonToiThieu()) < 0) {
                continue;
            }

            BigDecimal giam = tinhMucGiamVoucher(gg, tongTien);
            if (giam.compareTo(maxGiam) > 0) {
                maxGiam = giam;
                bestVoucher = gg;
            }
        }

        return bestVoucher;
    }

    private List<GiamGia> getVoucherChoKhachHang(KhachHang khachHang) {
        List<GiamGia> allVouchers = giamGiaService.getGiamGia1();
        if (allVouchers == null || allVouchers.isEmpty()) {
            return new ArrayList<>();
        }

        List<GiamGia> result = new ArrayList<>();

        for (GiamGia gg : allVouchers) {
            // ⭐ KIỂM TRA TRẠNG THÁI: Chỉ lấy "Hoạt động"
            String trangThai = giamGiaService.tinhToanTrangThai(gg);
            if (!"Hoạt động".equals(trangThai)) {
                continue;
            }

            // 1. Kiểm tra số lượng
            if (gg.getIsVoHan() == null || !gg.getIsVoHan()) {
                if (gg.getSoLuong() == null || gg.getSoLuong() <= 0) {
                    continue;
                }
            }

            // 2. Kiểm tra loại áp dụng
            if (gg.getLoaiApDung() != null && gg.getLoaiApDung() == 1) {
                // Loại công khai
                result.add(gg);
            } else if (gg.getLoaiApDung() != null && gg.getLoaiApDung() == 2) {
                // Loại cá nhân - Chỉ hiển thị nếu khách hàng được áp dụng
                if (khachHang != null && !"0000000000".equals(khachHang.getSdt())) {
                    boolean isEligible = kiemTraVoucherChoKhachHang(gg, khachHang);
                    if (isEligible) {
                        result.add(gg);
                    }
                }
            } else {
                // Không xác định loại -> mặc định công khai
                result.add(gg);
            }
        }

        // Sắp xếp
        result.sort((a, b) -> {
            BigDecimal giamA = a.getGiaTriGiam() != null ? a.getGiaTriGiam() : BigDecimal.ZERO;
            BigDecimal giamB = b.getGiaTriGiam() != null ? b.getGiaTriGiam() : BigDecimal.ZERO;

            if ("Tien".equalsIgnoreCase(a.getLoaiGiamGia()) && !"Tien".equalsIgnoreCase(b.getLoaiGiamGia())) {
                return -1;
            }
            if (!"Tien".equalsIgnoreCase(a.getLoaiGiamGia()) && "Tien".equalsIgnoreCase(b.getLoaiGiamGia())) {
                return 1;
            }
            return giamB.compareTo(giamA);
        });

        return result;
    }

    private List<GiamGia> getVoucherCongKhai() {
        List<GiamGia> allVouchers = giamGiaService.getGiamGia1();
        if (allVouchers == null || allVouchers.isEmpty()) {
            return new ArrayList<>();
        }

        return allVouchers.stream()
                .filter(gg -> {
                    // ⭐ KIỂM TRA TRẠNG THÁI: Chỉ lấy "Hoạt động"
                    String trangThai = giamGiaService.tinhToanTrangThai(gg);
                    if (!"Hoạt động".equals(trangThai)) {
                        return false;
                    }

                    // Kiểm tra số lượng
                    if (gg.getIsVoHan() == null || !gg.getIsVoHan()) {
                        if (gg.getSoLuong() == null || gg.getSoLuong() <= 0) {
                            return false;
                        }
                    }

                    // Chỉ lấy voucher công khai (loại 1) hoặc không xác định
                    return gg.getLoaiApDung() == null || gg.getLoaiApDung() == 1;
                })
                .sorted((a, b) -> {
                    BigDecimal giamA = a.getGiaTriGiam() != null ? a.getGiaTriGiam() : BigDecimal.ZERO;
                    BigDecimal giamB = b.getGiaTriGiam() != null ? b.getGiaTriGiam() : BigDecimal.ZERO;

                    if ("Tien".equalsIgnoreCase(a.getLoaiGiamGia()) && !"Tien".equalsIgnoreCase(b.getLoaiGiamGia())) {
                        return -1;
                    }
                    if (!"Tien".equalsIgnoreCase(a.getLoaiGiamGia()) && "Tien".equalsIgnoreCase(b.getLoaiGiamGia())) {
                        return 1;
                    }
                    return giamB.compareTo(giamA);
                })
                .collect(Collectors.toList());
    }

    // ===== KIỂM TRA VOUCHER CÓ ÁP DỤNG CHO KHÁCH HÀNG KHÔNG =====
    private boolean kiemTraVoucherChoKhachHang(GiamGia voucher, KhachHang khachHang) {
        if (voucher == null || khachHang == null) {
            return false;
        }

        try {
            // Loại 1 = Công khai: ai cũng được áp dụng
            if (voucher.getLoaiApDung() != null && voucher.getLoaiApDung() == 1) {
                return true;
            }

            // Loại 2 = Cá nhân: kiểm tra trong bảng KHACHHANG_VOUCHER
            if (voucher.getLoaiApDung() != null && voucher.getLoaiApDung() == 2) {
                boolean exists = giamGiaChiTietRepository.existsById_MaGiamGiaAndId_MaKhachHang(
                        voucher.getMaGiamGia(),
                        khachHang.getMaKH()
                );
                return exists;
            }

            // Nếu không xác định loại, mặc định là công khai
            return true;
        } catch (Exception e) {
            System.err.println("❌ Lỗi kiểm tra voucher: " + e.getMessage());
            return false;
        }
    }

    @GetMapping("/voucher-goi-y")
    @ResponseBody
    public Map<String, Object> getVoucherGoiY(@RequestParam("mahd") String mahd) {
        Map<String, Object> response = new HashMap<>();
        try {
            HoaDon hoaDon = hoaDonService.findById(mahd);
            if (hoaDon == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy hóa đơn!");
                return response;
            }

            List<HoaDonChiTiet> listhdct = hoaDonChiTietService.findById(mahd);
            if (listhdct == null || listhdct.isEmpty()) {
                response.put("success", false);
                response.put("message", "Hóa đơn chưa có sản phẩm!");
                return response;
            }

            BigDecimal tongTien = listhdct.stream()
                    .map(HoaDonChiTiet::getThanhTien)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // ⭐ LẤY TẤT CẢ VOUCHER HOẠT ĐỘNG
            List<GiamGia> allVouchers = giamGiaService.getGiamGia1();
            KhachHang khachHang = hoaDon.getMaKhachHang();

            // ⭐ PHÂN LOẠI VOUCHER
            List<GiamGia> publicVouchers = new ArrayList<>();  // Công khai
            List<GiamGia> personalVouchers = new ArrayList<>(); // Cá nhân

            for (GiamGia gg : allVouchers) {
                // Kiểm tra trạng thái
                String trangThai = giamGiaService.tinhToanTrangThai(gg);
                if (!"Hoạt động".equals(trangThai)) continue;

                // Kiểm tra số lượng
                if (gg.getIsVoHan() == null || !gg.getIsVoHan()) {
                    if (gg.getSoLuong() == null || gg.getSoLuong() <= 0) continue;
                }

                // ⭐ PHÂN LOẠI THEO LoaiApDung
                if (gg.getLoaiApDung() == null || gg.getLoaiApDung() == 1) {
                    // VOUCHER CÔNG KHAI - Thêm vào danh sách công khai
                    publicVouchers.add(gg);
                } else if (gg.getLoaiApDung() == 2) {
                    // VOUCHER CÁ NHÂN - Chỉ thêm nếu có khách hàng và được áp dụng
                    if (khachHang != null && !"0000000000".equals(khachHang.getSdt())) {
                        boolean isEligible = kiemTraVoucherChoKhachHang(gg, khachHang);
                        if (isEligible) {
                            personalVouchers.add(gg);
                        }
                    }
                }
            }

            System.out.println("========== VOUCHER GOI Y ==========");
            System.out.println("Voucher công khai: " + publicVouchers.size());
            System.out.println("Voucher cá nhân: " + personalVouchers.size());
            System.out.println("Tổng: " + (publicVouchers.size() + personalVouchers.size()));

            // ⭐ GỘP DANH SÁCH: Công khai + Cá nhân (ưu tiên công khai trước)
            List<GiamGia> combinedVouchers = new ArrayList<>();
            combinedVouchers.addAll(publicVouchers);
            combinedVouchers.addAll(personalVouchers);

            // ⭐ TẠO RESPONSE
            List<Map<String, Object>> voucherList = new ArrayList<>();
            GiamGia currentVoucher = hoaDon.getMaGiamGia();

            for (GiamGia gg : combinedVouchers) {
                Map<String, Object> item = new HashMap<>();
                item.put("maGiamGia", gg.getMaGiamGia());
                item.put("tenGiamGia", gg.getTenGiamGia());
                item.put("loaiGiamGia", gg.getLoaiGiamGia());
                item.put("giaTriGiam", gg.getGiaTriGiam());
                item.put("giamToiDa", gg.getGiamToiDa());
                item.put("soLuong", gg.getSoLuong());
                item.put("isVoHan", gg.getIsVoHan());
                item.put("donToiThieu", gg.getDonToiThieu());
                item.put("ngayKetThuc", gg.getNgayKetThuc());

                // ⭐ THÊM THÔNG TIN LOẠI ÁP DỤNG
                item.put("loaiApDung", gg.getLoaiApDung());
                item.put("loaiApDungText", gg.getLoaiApDung() != null && gg.getLoaiApDung() == 1 ? "Công khai" : "Cá nhân");

                // ⭐ THÊM BADGE PHÂN LOẠI
                String typeBadge = "";
                if (gg.getLoaiApDung() != null && gg.getLoaiApDung() == 2) {
                    typeBadge = "<span class='badge bg-danger ms-1' style='font-size:8px;'>👤 Cá nhân</span>";
                } else {
                    typeBadge = "<span class='badge bg-primary ms-1' style='font-size:8px;'>🌐 Công khai</span>";
                }
                item.put("typeBadge", typeBadge);

                BigDecimal tienGiam = tinhMucGiamVoucher(gg, tongTien);
                item.put("tienGiam", tienGiam);

                boolean isEligible = true;
                String status = "Sẵn sàng áp dụng";
                String statusClass = "success";
                BigDecimal canThem = BigDecimal.ZERO;

                // Kiểm tra đơn tối thiểu
                if (gg.getDonToiThieu() != null && gg.getDonToiThieu().compareTo(BigDecimal.ZERO) > 0
                        && tongTien.compareTo(gg.getDonToiThieu()) < 0) {
                    isEligible = false;
                    canThem = gg.getDonToiThieu().subtract(tongTien);
                    status = "Cần thêm " + formatCurrency(canThem);
                    statusClass = "warning";
                    item.put("canThem", canThem);
                }

                // Kiểm tra nếu đang áp dụng
                if (currentVoucher != null && currentVoucher.getMaGiamGia().equals(gg.getMaGiamGia())) {
                    status = "✅ Đang áp dụng";
                    statusClass = "info";
                    isEligible = true;
                    item.put("isApplied", true);
                } else {
                    item.put("isApplied", false);
                }

                String soLuongDisplay = "";
                if (gg.getIsVoHan() != null && gg.getIsVoHan()) {
                    soLuongDisplay = "♾️ Không giới hạn";
                } else {
                    soLuongDisplay = "Còn: " + (gg.getSoLuong() != null ? gg.getSoLuong() : 0) + " lượt";
                }
                item.put("soLuongDisplay", soLuongDisplay);
                item.put("isEligible", isEligible);
                item.put("status", status);
                item.put("statusClass", statusClass);

                voucherList.add(item);
            }

            // ⭐ SẮP XẾP
            voucherList.sort((a, b) -> {
                // 1. Đang áp dụng lên đầu
                boolean aApplied = (boolean) a.get("isApplied");
                boolean bApplied = (boolean) b.get("isApplied");
                if (aApplied && !bApplied) return -1;
                if (!aApplied && bApplied) return 1;

                // 2. Công khai lên trước cá nhân
                Integer aLoai = (Integer) a.get("loaiApDung");
                Integer bLoai = (Integer) b.get("loaiApDung");
                if (aLoai != null && bLoai != null) {
                    if (aLoai == 1 && bLoai == 2) return -1;
                    if (aLoai == 2 && bLoai == 1) return 1;
                }

                // 3. Có thể áp dụng lên sau
                boolean aEligible = (boolean) a.get("isEligible");
                boolean bEligible = (boolean) b.get("isEligible");
                if (aEligible && !bEligible) return -1;
                if (!aEligible && bEligible) return 1;

                // 4. Chưa đủ điều kiện: sắp xếp theo số tiền thiếu
                if (!aEligible && !bEligible) {
                    BigDecimal aCanThem = (BigDecimal) a.getOrDefault("canThem", BigDecimal.ZERO);
                    BigDecimal bCanThem = (BigDecimal) b.getOrDefault("canThem", BigDecimal.ZERO);
                    return aCanThem.compareTo(bCanThem);
                }

                // 5. Cả 2 đều đủ điều kiện: sắp xếp theo tiền giảm
                BigDecimal aGiam = (BigDecimal) a.get("tienGiam");
                BigDecimal bGiam = (BigDecimal) b.get("tienGiam");
                return bGiam.compareTo(aGiam);
            });

            response.put("success", true);
            response.put("vouchers", voucherList);
            response.put("tongTien", tongTien);
            response.put("currentVoucher", currentVoucher != null ? currentVoucher.getMaGiamGia() : null);
            response.put("totalVouchers", combinedVouchers.size());
            response.put("displayVouchers", voucherList.size());
            response.put("publicCount", publicVouchers.size());
            response.put("personalCount", personalVouchers.size());

            // ⭐ Tìm voucher tốt nhất
            GiamGia bestVoucher = null;
            BigDecimal maxGiam = BigDecimal.ZERO;

            for (GiamGia gg : combinedVouchers) {
                if (gg.getDonToiThieu() != null && gg.getDonToiThieu().compareTo(BigDecimal.ZERO) > 0
                        && tongTien.compareTo(gg.getDonToiThieu()) < 0) {
                    continue;
                }

                BigDecimal giam = tinhMucGiamVoucher(gg, tongTien);
                if (giam.compareTo(maxGiam) > 0) {
                    maxGiam = giam;
                    bestVoucher = gg;
                }
            }

            if (bestVoucher != null) {
                response.put("bestVoucher", Map.of(
                        "maGiamGia", bestVoucher.getMaGiamGia(),
                        "tenGiamGia", bestVoucher.getTenGiamGia(),
                        "tienGiam", tinhMucGiamVoucher(bestVoucher, tongTien),
                        "isCurrent", currentVoucher != null && currentVoucher.getMaGiamGia().equals(bestVoucher.getMaGiamGia())
                ));
            } else {
                response.put("bestVoucher", null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // Trong controller - tạo QR cho sản phẩm
    @GetMapping("/tao-qr-sanpham")
    @ResponseBody
    public Map<String, Object> taoQRSanPham(@RequestParam("mactsp") String mactsp,
                                            @RequestParam(value = "sluong", defaultValue = "1") int sluong) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Tạo dữ liệu QR: mã sản phẩm|số lượng
            String qrData = mactsp + "|" + sluong;

            // Mã hóa Base64 để an toàn
            String encoded = java.util.Base64.getEncoder().encodeToString(qrData.getBytes());

            // Tạo URL QR
            String qrUrl = "http://localhost:8080/banhang/quet-qr?data=" + encoded;

            // Tạo QR code dạng base64
            String qrBase64 = generateQRCodeBase64(qrUrl);

            response.put("success", true);
            response.put("qrData", qrData);
            response.put("qrCode", qrBase64);
            response.put("qrUrl", qrUrl);
            response.put("mactsp", mactsp);
            response.put("sluong", sluong);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }


    // Thêm vào BanHangController.java
    @GetMapping("/getInvoiceTotal/{mahd}")
    @ResponseBody
    public Map<String, Object> getInvoiceTotal(@PathVariable("mahd") String mahd) {
        Map<String, Object> response = new HashMap<>();
        try {
            HoaDon hoaDon = hoaDonService.findById(mahd);
            if (hoaDon == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy hóa đơn!");
                return response;
            }

            List<HoaDonChiTiet> listhdct = hoaDonChiTietService.findById(mahd);
            if (listhdct == null || listhdct.isEmpty()) {
                response.put("success", false);
                response.put("message", "Hóa đơn chưa có sản phẩm!");
                return response;
            }

            BigDecimal tongTien = listhdct.stream()
                    .map(HoaDonChiTiet::getThanhTien)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Trừ voucher
            if (hoaDon.getMaGiamGia() != null) {
                BigDecimal tienGiam = tinhMucGiamVoucher(hoaDon.getMaGiamGia(), tongTien);
                tongTien = tongTien.subtract(tienGiam);
            }

            // Cộng phí ship
            if ("Online".equalsIgnoreCase(hoaDon.getLoaiBan())) {
                BigDecimal ship = hoaDon.getTienShip() != null ? hoaDon.getTienShip() : BigDecimal.ZERO;
                tongTien = tongTien.add(ship);
            }

            response.put("success", true);
            response.put("total", tongTien);
            response.put("tongTienHang", tongTien);
            response.put("maHoaDon", mahd);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }




    // ===== KIỂM TRA VOUCHER TRƯỚC KHI THANH TOÁN =====
    @GetMapping("/kiemtravoucher/{maHoaDon}")
    @ResponseBody
    public Map<String, Object> kiemTraVoucher(@PathVariable("maHoaDon") String maHoaDon) {
        Map<String, Object> response = new HashMap<>();
        try {
            HoaDon hoaDon = hoaDonService.findById(maHoaDon);
            if (hoaDon == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy hóa đơn!");
                return response;
            }

            // Lấy danh sách sản phẩm
            List<HoaDonChiTiet> listhdct = hoaDonChiTietService.findById(maHoaDon);
            if (listhdct == null || listhdct.isEmpty()) {
                response.put("success", false);
                response.put("message", "Hóa đơn chưa có sản phẩm!");
                return response;
            }

            BigDecimal tongTien = listhdct.stream()
                    .map(HoaDonChiTiet::getThanhTien)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<Map<String, Object>> warnings = new ArrayList<>();
            boolean hasWarning = false;
            GiamGia currentVoucher = hoaDon.getMaGiamGia();

            // ===== 1. Nếu CÓ voucher đang áp dụng =====
            if (currentVoucher != null) {
                Map<String, Object> checkResult = kiemTraVoucherHienTai(currentVoucher, tongTien, hoaDon);
                if (checkResult != null) {
                    hasWarning = true;
                    warnings.add(checkResult);
                }

                // ===== 2. Kiểm tra voucher TỐT HƠN =====
                if (!hasWarning) {
                    GiamGia betterVoucher = timVoucherTotNhatChoHoaDon(hoaDon, tongTien);
                    if (betterVoucher != null && !betterVoucher.getMaGiamGia().equals(currentVoucher.getMaGiamGia())) {
                        BigDecimal currentDiscount = tinhMucGiamVoucher(currentVoucher, tongTien);
                        BigDecimal betterDiscount = tinhMucGiamVoucher(betterVoucher, tongTien);

                        if (betterDiscount.compareTo(currentDiscount) > 0) {
                            hasWarning = true;
                            Map<String, Object> warning = new HashMap<>();
                            warning.put("type", "BETTER_VOUCHER");
                            warning.put("maVoucher", betterVoucher.getMaGiamGia());
                            warning.put("tenVoucher", betterVoucher.getTenGiamGia());
                            warning.put("message", "Có voucher tốt hơn: " + betterVoucher.getTenGiamGia());
                            warning.put("currentDiscount", currentDiscount);
                            warning.put("betterDiscount", betterDiscount);
                            warnings.add(warning);
                        }
                    }
                }
            } else {
                // ===== 3. Nếu KHÔNG có voucher, kiểm tra có voucher nào khả dụng không =====
                GiamGia bestVoucher = timVoucherTotNhatChoHoaDon(hoaDon, tongTien);
                if (bestVoucher != null) {
                    hasWarning = true;
                    Map<String, Object> warning = new HashMap<>();
                    warning.put("type", "VOUCHER_AVAILABLE");
                    warning.put("maVoucher", bestVoucher.getMaGiamGia());
                    warning.put("tenVoucher", bestVoucher.getTenGiamGia());
                    warning.put("message", "Có voucher khả dụng: " + bestVoucher.getTenGiamGia());
                    warning.put("tienGiam", tinhMucGiamVoucher(bestVoucher, tongTien));
                    warnings.add(warning);
                }
            }

            response.put("success", true);
            response.put("hasWarning", hasWarning);
            response.put("warnings", warnings);
            response.put("voucherHienTai", currentVoucher != null ?
                    currentVoucher.getMaGiamGia() + " - " + currentVoucher.getTenGiamGia() :
                    "Chưa có voucher");
            response.put("tongTien", tongTien);
            response.put("message", hasWarning ? "Có " + warnings.size() + " cảnh báo voucher" : "Voucher hợp lệ");

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        return response;
    }


    // ===== API CẬP NHẬT VOUCHER KHI CHỌN KHÁCH HÀNG =====
    @PostMapping("/update-vouchers")
    @ResponseBody
    public Map<String, Object> updateVouchers(
            @RequestParam("mahd") String mahd,
            @RequestParam(value = "maKH", required = false) String maKH) {

        Map<String, Object> response = new HashMap<>();
        try {
            HoaDon hoaDon = hoaDonService.findById(mahd);
            if (hoaDon == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy hóa đơn!");
                return response;
            }

            // Cập nhật khách hàng nếu có maKH
            if (maKH != null && !maKH.isEmpty()) {
                KhachHang khachHang = khachHangService.getKhachHangById(maKH);
                if (khachHang != null) {
                    hoaDon.setMaKhachHang(khachHang);
                    hoaDonService.save(hoaDon);
                }
            }

            // Lấy danh sách voucher theo khách hàng
            List<GiamGia> vouchers = getVoucherChoHoaDon(hoaDon);

            // Lấy tổng tiền hiện tại
            List<HoaDonChiTiet> listhdct = hoaDonChiTietService.findById(mahd);
            BigDecimal tongTien = listhdct.stream()
                    .map(HoaDonChiTiet::getThanhTien)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Chuyển đổi sang DTO
            List<Map<String, Object>> voucherList = new ArrayList<>();
            GiamGia currentVoucher = hoaDon.getMaGiamGia();

            for (GiamGia gg : vouchers) {
                Map<String, Object> item = new HashMap<>();
                item.put("maGiamGia", gg.getMaGiamGia());
                item.put("tenGiamGia", gg.getTenGiamGia());
                item.put("loaiGiamGia", gg.getLoaiGiamGia());
                item.put("giaTriGiam", gg.getGiaTriGiam());
                item.put("giamToiDa", gg.getGiamToiDa());
                item.put("soLuong", gg.getSoLuong());
                item.put("isVoHan", gg.getIsVoHan());
                item.put("donToiThieu", gg.getDonToiThieu());
                item.put("loaiApDung", gg.getLoaiApDung());
                item.put("loaiApDungText", gg.getLoaiApDung() == 1 ? "Công khai" : "Cá nhân");
                item.put("isApplied", currentVoucher != null && currentVoucher.getMaGiamGia().equals(gg.getMaGiamGia()));

                // Tính tiền giảm
                BigDecimal tienGiam = tinhMucGiamVoucher(gg, tongTien);
                item.put("tienGiam", tienGiam);

                // Kiểm tra điều kiện áp dụng
                boolean isEligible = true;
                String status = "Sẵn sàng áp dụng";
                String statusClass = "success";
                BigDecimal canThem = BigDecimal.ZERO;

                if (gg.getDonToiThieu() != null && gg.getDonToiThieu().compareTo(BigDecimal.ZERO) > 0
                        && tongTien.compareTo(gg.getDonToiThieu()) < 0) {
                    isEligible = false;
                    canThem = gg.getDonToiThieu().subtract(tongTien);
                    status = "Cần thêm " + formatCurrency(canThem);
                    statusClass = "warning";
                    item.put("canThem", canThem);
                }

                // Kiểm tra nếu đang áp dụng voucher này
                if (item.get("isApplied") == Boolean.TRUE) {
                    status = "✅ Đang áp dụng";
                    statusClass = "info";
                    isEligible = true;
                }

                item.put("isEligible", isEligible);
                item.put("status", status);
                item.put("statusClass", statusClass);

                voucherList.add(item);
            }

            // Tìm voucher tốt nhất
            GiamGia bestVoucher = timVoucherTotNhatChoHoaDon(hoaDon, tongTien);

            response.put("success", true);
            response.put("vouchers", voucherList);
            response.put("count", voucherList.size());
            response.put("khachHang", hoaDon.getMaKhachHang() != null ?
                    hoaDon.getMaKhachHang().getHoTen() : "Khách lẻ");
            response.put("maKH", hoaDon.getMaKhachHang() != null ?
                    hoaDon.getMaKhachHang().getMaKH() : null);
            response.put("currentVoucher", currentVoucher != null ? currentVoucher.getMaGiamGia() : null);

            if (bestVoucher != null) {
                response.put("bestVoucher", Map.of(
                        "maGiamGia", bestVoucher.getMaGiamGia(),
                        "tenGiamGia", bestVoucher.getTenGiamGia(),
                        "tienGiam", tinhMucGiamVoucher(bestVoucher, tongTien),
                        "isCurrent", currentVoucher != null && currentVoucher.getMaGiamGia().equals(bestVoucher.getMaGiamGia())
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // ===== LẤY VOUCHER CHO HÓA ĐƠN =====
    private List<GiamGia> getVoucherChoHoaDon(HoaDon hoaDon) {
        if (hoaDon == null) {
            return getVoucherCongKhai();
        }

        KhachHang khachHang = hoaDon.getMaKhachHang();
        if (khachHang != null && !"0000000000".equals(khachHang.getSdt())) {
            return getVoucherChoKhachHang(khachHang);
        }
        return getVoucherCongKhai();
    }


        @PostMapping("/chonkh-ajax")
        @ResponseBody
        public Map<String, Object> chonKhachHangAjax(@RequestParam("mahd") String mahd,
                                                     @RequestParam("makh") String makh) {
            Map<String, Object> response = new HashMap<>();
            try {
                HoaDon hd = hoaDonService.findById(mahd);
                if (hd == null) {
                    response.put("success", false);
                    response.put("message", "Không tìm thấy hóa đơn!");
                    return response;
                }

                KhachHang kh = khachHangService.getKhachHangById(makh);
                if (kh == null) {
                    response.put("success", false);
                    response.put("message", "Khách hàng không tồn tại!");
                    return response;
                }

                if ("0000000000".equals(kh.getSdt())) {
                    response.put("success", false);
                    response.put("message", "Không thể chọn khách hàng mặc định!");
                    return response;
                }

                // Cập nhật khách hàng cho hóa đơn
                hd.setMaKhachHang(kh);
                hoaDonService.save(hd);

                // Lấy danh sách voucher cho khách hàng
                List<HoaDonChiTiet> listhdct = hoaDonChiTietService.findById(mahd);
                BigDecimal tongTien = listhdct.stream()
                        .map(HoaDonChiTiet::getThanhTien)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Lấy voucher theo khách hàng (công khai + cá nhân)
                List<GiamGia> vouchers = getVoucherChoKhachHang(kh);

                // Chuyển đổi sang DTO
                List<Map<String, Object>> voucherList = new ArrayList<>();
                GiamGia currentVoucher = hd.getMaGiamGia();

                for (GiamGia gg : vouchers) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("maGiamGia", gg.getMaGiamGia());
                    item.put("tenGiamGia", gg.getTenGiamGia());
                    item.put("loaiGiamGia", gg.getLoaiGiamGia());
                    item.put("giaTriGiam", gg.getGiaTriGiam());
                    item.put("giamToiDa", gg.getGiamToiDa());
                    item.put("soLuong", gg.getSoLuong());
                    item.put("isVoHan", gg.getIsVoHan());
                    item.put("donToiThieu", gg.getDonToiThieu());
                    item.put("loaiApDung", gg.getLoaiApDung());
                    item.put("loaiApDungText", gg.getLoaiApDung() != null && gg.getLoaiApDung() == 1 ? "Công khai" : "Cá nhân");
                    item.put("isApplied", currentVoucher != null && currentVoucher.getMaGiamGia().equals(gg.getMaGiamGia()));

                    // Tính tiền giảm
                    BigDecimal tienGiam = tinhMucGiamVoucher(gg, tongTien);
                    item.put("tienGiam", tienGiam);

                    // Kiểm tra điều kiện áp dụng
                    boolean isEligible = true;
                    String status = "Sẵn sàng áp dụng";
                    String statusClass = "success";
                    BigDecimal canThem = BigDecimal.ZERO;

                    if (gg.getDonToiThieu() != null && gg.getDonToiThieu().compareTo(BigDecimal.ZERO) > 0
                            && tongTien.compareTo(gg.getDonToiThieu()) < 0) {
                        isEligible = false;
                        canThem = gg.getDonToiThieu().subtract(tongTien);
                        status = "Cần thêm " + formatCurrency(canThem);
                        statusClass = "warning";
                        item.put("canThem", canThem);
                    }

                    if (item.get("isApplied") == Boolean.TRUE) {
                        status = "✅ Đang áp dụng";
                        statusClass = "info";
                        isEligible = true;
                    }

                    item.put("isEligible", isEligible);
                    item.put("status", status);
                    item.put("statusClass", statusClass);

                    voucherList.add(item);
                }

                // Tìm voucher tốt nhất
                GiamGia bestVoucher = timVoucherTotNhatChoHoaDon(hd, tongTien);

                response.put("success", true);
                response.put("message", "Đã chọn khách hàng: " + kh.getHoTen());
                response.put("vouchers", voucherList);
                response.put("count", voucherList.size());
                response.put("khachHang", Map.of(
                        "maKH", kh.getMaKH(),
                        "hoTen", kh.getHoTen(),
                        "sdt", kh.getSdt()
                ));
                response.put("loaiKhachHang", "khachhang");
                response.put("currentVoucher", currentVoucher != null ? currentVoucher.getMaGiamGia() : null);

                if (bestVoucher != null) {
                    response.put("bestVoucher", Map.of(
                            "maGiamGia", bestVoucher.getMaGiamGia(),
                            "tenGiamGia", bestVoucher.getTenGiamGia(),
                            "tienGiam", tinhMucGiamVoucher(bestVoucher, tongTien),
                            "isCurrent", currentVoucher != null && currentVoucher.getMaGiamGia().equals(bestVoucher.getMaGiamGia())
                    ));
                }

            } catch (Exception e) {
                e.printStackTrace();
                response.put("success", false);
                response.put("message", "Lỗi: " + e.getMessage());
            }
            return response;
        }
    // Trong BanHangController.java

    @GetMapping("/get-vouchers")
    @ResponseBody
    public Map<String, Object> getVouchers(@RequestParam("mahd") String mahd,
                                           @RequestParam(value = "maKH", required = false) String maKH) {
        Map<String, Object> response = new HashMap<>();
        try {
            HoaDon hoaDon = hoaDonService.findById(mahd);
            if (hoaDon == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy hóa đơn!");
                return response;
            }

            List<HoaDonChiTiet> listhdct = hoaDonChiTietService.findById(mahd);
            BigDecimal tongTien = listhdct.stream()
                    .map(HoaDonChiTiet::getThanhTien)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Lấy danh sách voucher
            List<GiamGia> vouchers;
            KhachHang khachHang = null;
            String tenKhachHang = "Khách lẻ";
            String loaiKhachHang = "vanglai";

            if (maKH != null && !maKH.isEmpty()) {
                khachHang = khachHangService.getKhachHangById(maKH);
                if (khachHang != null && !"0000000000".equals(khachHang.getSdt())) {
                    vouchers = getVoucherChoKhachHang(khachHang);
                    tenKhachHang = khachHang.getHoTen();
                    loaiKhachHang = "khachhang";
                } else {
                    vouchers = getVoucherCongKhai();
                    loaiKhachHang = "vanglai";
                }
            } else {
                // Kiểm tra xem hóa đơn đã có khách hàng chưa
                if (hoaDon.getMaKhachHang() != null && !"0000000000".equals(hoaDon.getMaKhachHang().getSdt())) {
                    khachHang = hoaDon.getMaKhachHang();
                    vouchers = getVoucherChoKhachHang(khachHang);
                    tenKhachHang = khachHang.getHoTen();
                    loaiKhachHang = "khachhang";
                } else {
                    vouchers = getVoucherCongKhai();
                    loaiKhachHang = "vanglai";
                }
            }

            // Chuyển đổi sang DTO
            List<Map<String, Object>> voucherList = new ArrayList<>();
            GiamGia currentVoucher = hoaDon.getMaGiamGia();

            for (GiamGia gg : vouchers) {
                Map<String, Object> item = new HashMap<>();
                item.put("maGiamGia", gg.getMaGiamGia());
                item.put("tenGiamGia", gg.getTenGiamGia());
                item.put("loaiGiamGia", gg.getLoaiGiamGia());
                item.put("giaTriGiam", gg.getGiaTriGiam());
                item.put("giamToiDa", gg.getGiamToiDa());
                item.put("soLuong", gg.getSoLuong());
                item.put("isVoHan", gg.getIsVoHan());
                item.put("donToiThieu", gg.getDonToiThieu());
                item.put("loaiApDung", gg.getLoaiApDung());
                item.put("loaiApDungText", gg.getLoaiApDung() != null && gg.getLoaiApDung() == 1 ? "Công khai" : "Cá nhân");
                item.put("isApplied", currentVoucher != null && currentVoucher.getMaGiamGia().equals(gg.getMaGiamGia()));

                BigDecimal tienGiam = tinhMucGiamVoucher(gg, tongTien);
                item.put("tienGiam", tienGiam);

                boolean isEligible = true;
                String status = "Sẵn sàng áp dụng";
                String statusClass = "success";
                BigDecimal canThem = BigDecimal.ZERO;

                if (gg.getDonToiThieu() != null && gg.getDonToiThieu().compareTo(BigDecimal.ZERO) > 0
                        && tongTien.compareTo(gg.getDonToiThieu()) < 0) {
                    isEligible = false;
                    canThem = gg.getDonToiThieu().subtract(tongTien);
                    status = "Cần thêm " + formatCurrency(canThem);
                    statusClass = "warning";
                    item.put("canThem", canThem);
                }

                if (item.get("isApplied") == Boolean.TRUE) {
                    status = "✅ Đang áp dụng";
                    statusClass = "info";
                    isEligible = true;
                }

                item.put("isEligible", isEligible);
                item.put("status", status);
                item.put("statusClass", statusClass);

                voucherList.add(item);
            }

            GiamGia bestVoucher = timVoucherTotNhatChoHoaDon(hoaDon, tongTien);

            response.put("success", true);
            response.put("vouchers", voucherList);
            response.put("count", voucherList.size());
            response.put("tenKhachHang", tenKhachHang);
            response.put("loaiKhachHang", loaiKhachHang);
            response.put("currentVoucher", currentVoucher != null ? currentVoucher.getMaGiamGia() : null);

            if (bestVoucher != null) {
                response.put("bestVoucher", Map.of(
                        "maGiamGia", bestVoucher.getMaGiamGia(),
                        "tenGiamGia", bestVoucher.getTenGiamGia(),
                        "tienGiam", tinhMucGiamVoucher(bestVoucher, tongTien),
                        "isCurrent", currentVoucher != null && currentVoucher.getMaGiamGia().equals(bestVoucher.getMaGiamGia())
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/themdiachi")
    @ResponseBody
    public ResponseEntity<?> themDiaChi(@RequestBody Map<String, Object> payload) {
        try {
            logger.info("📝 Thêm địa chỉ mới: {}", payload);

            // ⭐ LẤY DỮ LIỆU
            String maKH = (String) payload.get("maKH");
            String tenNguoiNhan = (String) payload.get("tenNguoiNhan");
            String soDienThoai = (String) payload.get("soDienThoaiNguoiNhan");
            String diaChiCuThe = (String) payload.get("diaChiCuThe");
            String phuongXa = (String) payload.get("phuongXa");
            String quanHuyen = (String) payload.get("quanHuyen");
            String tinhThanh = (String) payload.get("tinhThanh");
            Boolean diaChiMacDinh = (Boolean) payload.get("diaChiMacDinh");

            // ⭐ VALIDATE
            if (maKH == null || maKH.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Mã khách hàng không hợp lệ!"));
            }

            if (tenNguoiNhan == null || tenNguoiNhan.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Vui lòng nhập tên người nhận!"));
            }

            if (diaChiCuThe == null || diaChiCuThe.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Vui lòng nhập địa chỉ cụ thể!"));
            }

            // ⭐ LẤY KHÁCH HÀNG
            KhachHang khachHang = khachHangService.findByMaKH(maKH);
            if (khachHang == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Khách hàng không tồn tại!"));
            }

            // ⭐ TẠO ĐỊA CHỈ MỚI
            DiaChi diaChi = new DiaChi();
            diaChi.setKhachHang(khachHang);
            diaChi.setTenNguoiNhan(tenNguoiNhan);
            diaChi.setSoDienThoaiNguoiNhan(soDienThoai != null ? soDienThoai : "");
            diaChi.setDiaChiCuThe(diaChiCuThe);
            diaChi.setPhuongXa(phuongXa != null ? phuongXa : "");
            diaChi.setQuanHuyen(quanHuyen != null ? quanHuyen : "");
            diaChi.setTinhThanh(tinhThanh != null ? tinhThanh : "");
            diaChi.setDiaChiMacDinh(diaChiMacDinh != null && diaChiMacDinh);

            // ⭐ LƯU - SERVICE TỰ RESET NẾU LÀ MẶC ĐỊNH
            DiaChi saved = diaChiService.save(diaChi);

            logger.info("✅ Thêm địa chỉ thành công! ID: {}", saved.getMaDiaChi());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Thêm địa chỉ thành công!",
                    "maDiaChi", saved.getMaDiaChi(),
                    "diaChiMacDinh", saved.getDiaChiMacDinh()
            ));

        } catch (Exception e) {
            logger.error("❌ Lỗi thêm địa chỉ: ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", "Lỗi: " + e.getMessage()
                    ));
        }
    }

    // ============================================================
    // 2. API SỬA ĐỊA CHỈ
    // ============================================================
    @PutMapping("/suadiachi/{maDiaChi}")
    @ResponseBody
    public ResponseEntity<?> suaDiaChi(@PathVariable("maDiaChi") Integer maDiaChi,
                                       @RequestBody Map<String, Object> payload) {
        try {
            logger.info("📝 Sửa địa chỉ ID: {}, data: {}", maDiaChi, payload);

            if (maDiaChi == null || maDiaChi <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Mã địa chỉ không hợp lệ!"));
            }

            Optional<DiaChi> diaChiOpt = diaChiService.findById(maDiaChi);
            if (!diaChiOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Địa chỉ không tồn tại!"));
            }

            DiaChi existing = diaChiOpt.get();

            // ⭐ CẬP NHẬT THÔNG TIN
            String tenNguoiNhan = (String) payload.get("tenNguoiNhan");
            String soDienThoai = (String) payload.get("soDienThoaiNguoiNhan");
            String diaChiCuThe = (String) payload.get("diaChiCuThe");
            String phuongXa = (String) payload.get("phuongXa");
            String quanHuyen = (String) payload.get("quanHuyen");
            String tinhThanh = (String) payload.get("tinhThanh");
            Boolean diaChiMacDinh = (Boolean) payload.get("diaChiMacDinh");

            if (tenNguoiNhan != null && !tenNguoiNhan.trim().isEmpty()) {
                existing.setTenNguoiNhan(tenNguoiNhan);
            }
            if (soDienThoai != null && !soDienThoai.isEmpty()) {
                existing.setSoDienThoaiNguoiNhan(soDienThoai);
            }
            if (diaChiCuThe != null && !diaChiCuThe.trim().isEmpty()) {
                existing.setDiaChiCuThe(diaChiCuThe);
            }
            if (phuongXa != null) {
                existing.setPhuongXa(phuongXa);
            }
            if (quanHuyen != null) {
                existing.setQuanHuyen(quanHuyen);
            }
            if (tinhThanh != null) {
                existing.setTinhThanh(tinhThanh);
            }

            // ⭐ XỬ LÝ ĐỊA CHỈ MẶC ĐỊNH
            if (diaChiMacDinh != null) {
                if (diaChiMacDinh) {
                    // Nếu đặt mặc định, reset các địa chỉ khác
                    diaChiService.resetDiaChiMacDinh(existing.getKhachHang().getMaKH());
                }
                existing.setDiaChiMacDinh(diaChiMacDinh);
            }

            // ⭐ LƯU
            DiaChi saved = diaChiService.save(existing);

            logger.info("✅ Sửa địa chỉ thành công! ID: {}", saved.getMaDiaChi());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật địa chỉ thành công!",
                    "maDiaChi", saved.getMaDiaChi(),
                    "diaChiMacDinh", saved.getDiaChiMacDinh()
            ));

        } catch (Exception e) {
            logger.error("❌ Lỗi sửa địa chỉ: ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", "Lỗi: " + e.getMessage()
                    ));
        }
    }

    // ============================================================
    // 3. API XÓA ĐỊA CHỈ
    // ============================================================
    @DeleteMapping("/xoadiachi/{maDiaChi}")
    @ResponseBody
    public ResponseEntity<?> xoaDiaChi(@PathVariable("maDiaChi") Integer maDiaChi) {
        try {
            logger.info("📝 Xóa địa chỉ ID: {}", maDiaChi);

            if (maDiaChi == null || maDiaChi <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Mã địa chỉ không hợp lệ!"));
            }

            Optional<DiaChi> diaChiOpt = diaChiService.findById(maDiaChi);
            if (!diaChiOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Địa chỉ không tồn tại!"));
            }

            // ⭐ KIỂM TRA NẾU LÀ ĐỊA CHỈ MẶC ĐỊNH
            DiaChi diaChi = diaChiOpt.get();
            if (Boolean.TRUE.equals(diaChi.getDiaChiMacDinh())) {
                // Nếu xóa địa chỉ mặc định, cần set địa chỉ khác làm mặc định
                String maKH = diaChi.getKhachHang().getMaKH();
                List<DiaChi> otherAddresses = diaChiService.findByKhachHang_MaKH(maKH)
                        .stream()
                        .filter(d -> !d.getMaDiaChi().equals(maDiaChi))
                        .collect(Collectors.toList());

                if (!otherAddresses.isEmpty()) {
                    // Đặt địa chỉ đầu tiên làm mặc định
                    DiaChi newDefault = otherAddresses.get(0);
                    newDefault.setDiaChiMacDinh(true);
                    diaChiService.save(newDefault);
                    logger.info("📌 Đã đặt địa chỉ {} làm mặc định thay thế", newDefault.getMaDiaChi());
                }
            }

            // ⭐ XÓA ĐỊA CHỈ
            diaChiService.deleteById(maDiaChi);

            logger.info("✅ Xóa địa chỉ thành công! ID: {}", maDiaChi);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Xóa địa chỉ thành công!"
            ));

        } catch (Exception e) {
            logger.error("❌ Lỗi xóa địa chỉ: ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", "Lỗi: " + e.getMessage()
                    ));
        }
    }

    @PostMapping("/setdefault/{maDiaChi}")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> setDefaultDiaChi(
            @PathVariable("maDiaChi") Integer maDiaChi,
            @RequestParam(value = "mahd", required = false) String mahd) {

        try {
            logger.info("📝 Đặt mặc định địa chỉ ID: {}", maDiaChi);

            if (maDiaChi == null || maDiaChi <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Mã địa chỉ không hợp lệ!"));
            }

            Optional<DiaChi> diaChiOpt = diaChiService.findById(maDiaChi);
            if (!diaChiOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Địa chỉ không tồn tại!"));
            }

            DiaChi diaChi = diaChiOpt.get();
            KhachHang khachHang = diaChi.getKhachHang();
            if (khachHang == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Không tìm thấy khách hàng!"));
            }

            String maKH = khachHang.getMaKH();
            logger.info("👤 Đặt mặc định cho khách hàng: {}", maKH);

            // ⭐ RESET TẤT CẢ VỀ FALSE
            diaChiService.resetDiaChiMacDinh(maKH);
            logger.info("✅ Đã reset địa chỉ mặc định cho KH: {}", maKH);

            // ⭐ ĐẶT ĐỊA CHỈ NÀY LÀM MẶC ĐỊNH
            diaChi.setDiaChiMacDinh(true);
            DiaChi saved = diaChiService.save(diaChi);
            logger.info("✅ Đã đặt địa chỉ {} làm mặc định, giá trị trong DB: {}", maDiaChi, saved.getDiaChiMacDinh());

            // ⭐ KIỂM TRA XEM ĐÃ CẬP NHẬT CHƯA
            int count = diaChiService.countDefaultAddressByKhachHang(maKH);
            logger.info("📊 Số địa chỉ mặc định của KH {}: {}", maKH, count);

            // ⭐ NẾU COUNT = 0, DÙNG CÁCH 2: UPDATE TRỰC TIẾP
            if (count == 0) {
                logger.warn("⚠️ COUNT = 0, sử dụng update trực tiếp...");
                diaChiService.resetDiaChiMacDinh(maKH);
                // Set trực tiếp
                diaChiRepo.setDefaultAddressDirectly(maKH, maDiaChi);
                int countAfter = diaChiService.countDefaultAddressByKhachHang(maKH);
                logger.info("📊 Số địa chỉ mặc định sau update trực tiếp: {}", countAfter);

                // Load lại địa chỉ từ DB
                diaChi = diaChiService.findById(maDiaChi).orElse(diaChi);
            }

            // ⭐ CẬP NHẬT HÓA ĐƠN NẾU CÓ
            if (mahd != null && !mahd.isEmpty()) {
                HoaDon hd = hoaDonService.findById(mahd);
                if (hd != null) {
                    String diaChiDayDu = buildFullAddress(diaChi);
                    hd.setDiaChiGiaoHang(diaChiDayDu);
                    hd.setGhiChu("Địa chỉ giao hàng: " + diaChiDayDu +
                            " | Người nhận: " + diaChi.getTenNguoiNhan() +
                            " | SĐT: " + diaChi.getSoDienThoaiNguoiNhan());
                    hoaDonService.save(hd);
                    logger.info("✅ Đã cập nhật địa chỉ cho hóa đơn: {}", mahd);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã đặt địa chỉ làm mặc định!");
            response.put("maDiaChi", maDiaChi);
            response.put("maKH", maKH);
            response.put("diaChi", diaChi.getDiaChiCuThe());
            response.put("tenNguoiNhan", diaChi.getTenNguoiNhan());
            response.put("soDienThoai", diaChi.getSoDienThoaiNguoiNhan());
            response.put("diaChiDayDu", buildFullAddress(diaChi));
            response.put("countDefault", count);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Lỗi đặt mặc định: ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", "Lỗi: " + e.getMessage()
                    ));
        }
    }

    @GetMapping("/diachi/{maKH}")
    @ResponseBody
    public ResponseEntity<?> getDiaChiByKhachHang(@PathVariable("maKH") String maKH) {
        try {
            logger.info("📝 Lấy địa chỉ của KH: {}", maKH);

            if (maKH == null || maKH.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Mã khách hàng không hợp lệ!"));
            }

            List<DiaChi> diaChiList = diaChiService.findByKhachHang_MaKH(maKH);

            if (diaChiList == null || diaChiList.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }

            // ⭐ CHUYỂN ĐỔI DTO
            List<Map<String, Object>> result = diaChiList.stream()
                    .map(dc -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("maDiaChi", dc.getMaDiaChi());
                        map.put("diaChiCuThe", dc.getDiaChiCuThe() != null ? dc.getDiaChiCuThe() : "");
                        map.put("phuongXa", dc.getPhuongXa() != null ? dc.getPhuongXa() : "");
                        map.put("quanHuyen", dc.getQuanHuyen() != null ? dc.getQuanHuyen() : "");
                        map.put("tinhThanh", dc.getTinhThanh() != null ? dc.getTinhThanh() : "");
                        map.put("tenNguoiNhan", dc.getTenNguoiNhan() != null ? dc.getTenNguoiNhan() : "");
                        map.put("soDienThoaiNguoiNhan", dc.getSoDienThoaiNguoiNhan() != null ? dc.getSoDienThoaiNguoiNhan() : "");
                        map.put("diaChiMacDinh", dc.getDiaChiMacDinh() != null && dc.getDiaChiMacDinh());
                        return map;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("❌ Lỗi lấy địa chỉ: ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ============================================================
    // 6. API LẤY CHI TIẾT ĐỊA CHỈ
    // ============================================================
    @GetMapping("/diachi/detail/{maDiaChi}")
    @ResponseBody
    public ResponseEntity<?> getDiaChiDetail(@PathVariable("maDiaChi") Integer maDiaChi) {
        try {
            Optional<DiaChi> diaChiOpt = diaChiService.findById(maDiaChi);
            if (!diaChiOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Địa chỉ không tồn tại!"));
            }

            DiaChi dc = diaChiOpt.get();
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("maDiaChi", dc.getMaDiaChi());
            result.put("diaChiCuThe", dc.getDiaChiCuThe() != null ? dc.getDiaChiCuThe() : "");
            result.put("phuongXa", dc.getPhuongXa() != null ? dc.getPhuongXa() : "");
            result.put("quanHuyen", dc.getQuanHuyen() != null ? dc.getQuanHuyen() : "");
            result.put("tinhThanh", dc.getTinhThanh() != null ? dc.getTinhThanh() : "");
            result.put("tenNguoiNhan", dc.getTenNguoiNhan() != null ? dc.getTenNguoiNhan() : "");
            result.put("soDienThoaiNguoiNhan", dc.getSoDienThoaiNguoiNhan() != null ? dc.getSoDienThoaiNguoiNhan() : "");
            result.put("diaChiMacDinh", dc.getDiaChiMacDinh() != null && dc.getDiaChiMacDinh());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("❌ Lỗi lấy chi tiết địa chỉ: ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    // ============================================================
    // 7. API CHỌN ĐỊA CHỈ CHO HÓA ĐƠN
    // ============================================================
    @PostMapping("/chondiachi")
    @ResponseBody
    public ResponseEntity<?> chonDiaChi(@RequestBody Map<String, Object> payload) {
        try {
            String mahd = (String) payload.get("mahd");
            Integer maDiaChi = (Integer) payload.get("maDiaChi");

            if (mahd == null || mahd.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Mã hóa đơn không hợp lệ!"));
            }

            if (maDiaChi == null || maDiaChi <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Mã địa chỉ không hợp lệ!"));
            }

            HoaDon hd = hoaDonService.findById(mahd);
            if (hd == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Hóa đơn không tồn tại!"));
            }

            Optional<DiaChi> diaChiOpt = diaChiService.findById(maDiaChi);
            if (!diaChiOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Địa chỉ không tồn tại!"));
            }

            DiaChi diaChi = diaChiOpt.get();

            // ⭐ CẬP NHẬT HÓA ĐƠN
            String diaChiDayDu = buildFullAddress(diaChi);
            hd.setDiaChiGiaoHang(diaChiDayDu);
            hd.setGhiChu("Địa chỉ giao hàng: " + diaChiDayDu +
                    " | Người nhận: " + diaChi.getTenNguoiNhan() +
                    " | SĐT: " + diaChi.getSoDienThoaiNguoiNhan());
            hoaDonService.save(hd);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã chọn địa chỉ giao hàng thành công!",
                    "diaChi", diaChiDayDu,
                    "tenNguoiNhan", diaChi.getTenNguoiNhan(),
                    "soDienThoai", diaChi.getSoDienThoaiNguoiNhan(),
                    "maDiaChi", diaChi.getMaDiaChi()
            ));

        } catch (Exception e) {
            logger.error("❌ Lỗi chọn địa chỉ: ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", "Lỗi: " + e.getMessage()
                    ));
        }
    }

    // ============================================================
    // 8. UTILITY - TẠO ĐỊA CHỈ ĐẦY ĐỦ
    // ============================================================
    private String buildFullAddress(DiaChi diaChi) {
        if (diaChi == null) return "";

        StringBuilder sb = new StringBuilder();
        if (diaChi.getDiaChiCuThe() != null && !diaChi.getDiaChiCuThe().isEmpty()) {
            sb.append(diaChi.getDiaChiCuThe());
        }
        if (diaChi.getPhuongXa() != null && !diaChi.getPhuongXa().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(diaChi.getPhuongXa());
        }
        if (diaChi.getQuanHuyen() != null && !diaChi.getQuanHuyen().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(diaChi.getQuanHuyen());
        }
        if (diaChi.getTinhThanh() != null && !diaChi.getTinhThanh().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(diaChi.getTinhThanh());
        }
        return sb.toString();
    }

    // ============ API CẬP NHẬT HÓA ĐƠN ============
    @PostMapping("/update/{mahd}")
    @ResponseBody
    public ResponseEntity<?> updateHoaDon(@PathVariable("mahd") String mahd) {
        try {
            HoaDon hd = hoaDonService.findById(mahd);
            if (hd == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Hóa đơn không tồn tại!"));
            }

            if (hd.getMaKhachHang() != null) {
                String maKH = hd.getMaKhachHang().getMaKH();
                DiaChi diaChiMacDinh = diaChiService.findDefaultByMaKH(maKH);

                if (diaChiMacDinh != null) {
                    String diaChiDayDu = diaChiMacDinh.getDiaChiCuThe();
                    if (diaChiMacDinh.getPhuongXa() != null && !diaChiMacDinh.getPhuongXa().isEmpty()) {
                        diaChiDayDu += ", " + diaChiMacDinh.getPhuongXa();
                    }
                    if (diaChiMacDinh.getQuanHuyen() != null && !diaChiMacDinh.getQuanHuyen().isEmpty()) {
                        diaChiDayDu += ", " + diaChiMacDinh.getQuanHuyen();
                    }
                    if (diaChiMacDinh.getTinhThanh() != null && !diaChiMacDinh.getTinhThanh().isEmpty()) {
                        diaChiDayDu += ", " + diaChiMacDinh.getTinhThanh();
                    }

                    hd.setDiaChiGiaoHang(diaChiDayDu);
                    hd.setGhiChu("Địa chỉ giao hàng: " + diaChiDayDu +
                            " | Người nhận: " + diaChiMacDinh.getTenNguoiNhan() +
                            " | SĐT: " + diaChiMacDinh.getSoDienThoaiNguoiNhan());

                    hoaDonService.save(hd);
                }
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật hóa đơn thành công!"
            ));

        } catch (Exception e) {
            logger.error("❌ Lỗi cập nhật hóa đơn: ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    // Trong GiamGiaController.java

    @GetMapping("/api/voucher-ca-nhan")
    @ResponseBody
    public ResponseEntity<List<VoucherDTO>> getVoucherCaNhan(@RequestParam String maKhachHang) {
        try {
            // Lấy danh sách voucher cá nhân của khách hàng
            List<GiamGiaChiTiet> chiTietList = giamGiaChiTietRepository.findByKhachHang_MaKH(maKhachHang);

            List<VoucherDTO> result = new ArrayList<>();
            LocalDate today = LocalDate.now();

            for (GiamGiaChiTiet ct : chiTietList) {
                GiamGia giamGia = ct.getGiamGia();
                if (giamGia == null) continue;

                // Chỉ lấy voucher đang hoạt động và còn hạn
                if (!"Hoạt động".equals(giamGia.getTrangThai())) continue;
                if (giamGia.getNgayKetThuc() != null && giamGia.getNgayKetThuc().toLocalDate().isBefore(today)) continue;

                VoucherDTO dto = new VoucherDTO();
                dto.setMaGiamGia(giamGia.getMaGiamGia());
                dto.setTenGiamGia(giamGia.getTenGiamGia());
                dto.setLoaiGiamGia(giamGia.getLoaiGiamGia());
                dto.setGiaTriGiam(giamGia.getGiaTriGiam());
                dto.setDonToiThieu(giamGia.getDonToiThieu());
                dto.setGiamToiDa(giamGia.getGiamToiDa());
                dto.setNgayBatDau(giamGia.getNgayBatDau());
                dto.setNgayKetThuc(giamGia.getNgayKetThuc());
                dto.setLoaiApDung(giamGia.getLoaiApDung());

                // ===== LẤY TRẠNG THÁI SỬ DỤNG =====
                Integer trangThaiSuDung = ct.getTrangThaiSuDung() != null ? ct.getTrangThaiSuDung() : 0;
                dto.setTrangThaiSuDung(trangThaiSuDung);
                dto.setDaSuDung(trangThaiSuDung == 1);

                result.add(dto);
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // ===== KIỂM TRA KHÁCH HÀNG ĐÃ DÙNG VOUCHER CHƯA =====
    @GetMapping("/api/check-voucher-used")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkVoucherUsed(
            @RequestParam String maKhachHang,
            @RequestParam String maGiamGia) {

        Map<String, Object> response = new HashMap<>();
        try {
            GiamGiaChiTietId id = new GiamGiaChiTietId(maKhachHang, maGiamGia);
            GiamGiaChiTiet ct = giamGiaChiTietRepository.findById(id).orElse(null);

            boolean used = false;
            if (ct != null && ct.getTrangThaiSuDung() != null && ct.getTrangThaiSuDung() == 1) {
                used = true;
            }

            response.put("success", true);
            response.put("used", used);
            response.put("message", used ? "Bạn đã sử dụng voucher này rồi!" : "Voucher còn hiệu lực");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi kiểm tra: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Trong GiamGiaController.java

    @PostMapping("/api/mark-voucher-used")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markVoucherUsed(
            @RequestParam String maKhachHang,
            @RequestParam String maGiamGia) {

        Map<String, Object> response = new HashMap<>();
        try {
            giamGiaService.markVoucherAsUsed(maKhachHang, maGiamGia);

            response.put("success", true);
            response.put("message", "Đã đánh dấu voucher đã sử dụng");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Trong BanHangController.java
    @GetMapping("/get-voucher-info")
    @ResponseBody
    public Map<String, Object> getVoucherInfo(@RequestParam("mahd") String mahd) {
        Map<String, Object> response = new HashMap<>();
        try {
            HoaDon hoaDon = hoaDonService.findById(mahd);
            if (hoaDon == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy hóa đơn!");
                return response;
            }

            GiamGia voucher = hoaDon.getMaGiamGia();
            if (voucher == null) {
                response.put("success", true);
                response.put("voucher", null);
                return response;
            }

            // Tính tổng tiền
            List<HoaDonChiTiet> listhdct = hoaDonChiTietService.findById(mahd);
            BigDecimal tongTien = listhdct.stream()
                    .map(HoaDonChiTiet::getThanhTien)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal tienGiam = tinhMucGiamVoucher(voucher, tongTien);

            Map<String, Object> voucherInfo = new HashMap<>();
            voucherInfo.put("maGiamGia", voucher.getMaGiamGia());
            voucherInfo.put("tenGiamGia", voucher.getTenGiamGia());
            voucherInfo.put("tienGiam", tienGiam);
            voucherInfo.put("loaiGiamGia", voucher.getLoaiGiamGia());
            voucherInfo.put("giaTriGiam", voucher.getGiaTriGiam());

            response.put("success", true);
            response.put("voucher", voucherInfo);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }






    @GetMapping("/kiemtra-voucher-thanhtoan/{mahd}")
    @ResponseBody
    public ResponseEntity<?> kiemTraVoucherThanhToan(@PathVariable("mahd") String mahd) {
        try {
            System.out.println("========== KIỂM TRA VOUCHER TRƯỚC THANH TOÁN ==========");
            System.out.println("📋 Mã hóa đơn: " + mahd);

            HoaDon hoaDon = hoaDonService.findById(mahd);
            if (hoaDon == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy hóa đơn!"
                ));
            }

            List<HoaDonChiTiet> listhdct = hoaDonChiTietService.findById(mahd);
            if (listhdct == null || listhdct.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Hóa đơn chưa có sản phẩm!"
                ));
            }

            BigDecimal tongTien = listhdct.stream()
                    .map(HoaDonChiTiet::getThanhTien)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            GiamGia currentVoucher = hoaDon.getMaGiamGia();
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("hasVoucher", currentVoucher != null);
            result.put("tongTien", tongTien);
            result.put("voucherChanged", false);
            result.put("voucherRemoved", false);
            result.put("autoApplied", false);
            result.put("hasBetterVoucher", false); // ⭐ THÊM FLAG

            // ⭐ TÌM VOUCHER TỐT NHẤT
            GiamGia bestVoucher = timVoucherTotNhatChoHoaDon(hoaDon, tongTien);
            System.out.println("🔍 Best Voucher: " + (bestVoucher != null ? bestVoucher.getMaGiamGia() + " - " + bestVoucher.getTenGiamGia() : "KHÔNG CÓ"));

            // ===== TRƯỜNG HỢP 1: KHÔNG CÓ VOUCHER =====
            if (currentVoucher == null) {
                if (bestVoucher != null) {
                    hoaDon.setMaGiamGia(bestVoucher);
                    hoaDonService.save(hoaDon);

                    BigDecimal tienGiam = tinhMucGiamVoucher(bestVoucher, tongTien);
                    BigDecimal tongTienMoi = tongTien.subtract(tienGiam);

                    result.put("autoApplied", true);
                    result.put("bestVoucher", Map.of(
                            "maGiamGia", bestVoucher.getMaGiamGia(),
                            "tenGiamGia", bestVoucher.getTenGiamGia(),
                            "loaiGiamGia", bestVoucher.getLoaiGiamGia(),
                            "giaTriGiam", bestVoucher.getGiaTriGiam(),
                            "tienGiam", tienGiam
                    ));
                    result.put("tongTienMoi", tongTienMoi);
                    result.put("message", "Đã tự động áp dụng voucher tốt nhất: " + bestVoucher.getTenGiamGia());
                } else {
                    result.put("message", "Không có voucher khả dụng");
                    result.put("tongTienMoi", tongTien);
                }
                return ResponseEntity.ok(result);
            }

            // ===== KIỂM TRA VOUCHER HIỆN TẠI =====
            Map<String, Object> voucherCheck = kiemTraVoucherHienTai(currentVoucher, tongTien, hoaDon);

            // ===== VOUCHER HIỆN TẠI KHÔNG HỢP LỆ =====
            if (voucherCheck != null) {
                System.out.println("⚠️ Voucher không hợp lệ: " + voucherCheck.get("message"));

                hoaDon.setMaGiamGia(null);
                hoaDonService.save(hoaDon);
                result.put("voucherRemoved", true);
                result.put("removedVoucher", Map.of(
                        "maGiamGia", currentVoucher.getMaGiamGia(),
                        "tenGiamGia", currentVoucher.getTenGiamGia(),
                        "reason", voucherCheck.get("message")
                ));

                if (bestVoucher != null && !bestVoucher.getMaGiamGia().equals(currentVoucher.getMaGiamGia())) {
                    hoaDon.setMaGiamGia(bestVoucher);
                    hoaDonService.save(hoaDon);

                    BigDecimal tienGiam = tinhMucGiamVoucher(bestVoucher, tongTien);
                    BigDecimal tongTienMoi = tongTien.subtract(tienGiam);

                    result.put("hasReplacement", true);
                    result.put("voucherChanged", true);
                    result.put("replacementVoucher", Map.of(
                            "maGiamGia", bestVoucher.getMaGiamGia(),
                            "tenGiamGia", bestVoucher.getTenGiamGia(),
                            "tienGiam", tienGiam
                    ));
                    result.put("tongTienMoi", tongTienMoi);
                } else {
                    result.put("hasReplacement", false);
                    result.put("tongTienMoi", tongTien);
                }
                return ResponseEntity.ok(result);
            }

            // ===== ⭐ VOUCHER HIỆN TẠI HỢP LỆ - KIỂM TRA VOUCHER TỐT HƠN =====
            BigDecimal currentDiscount = tinhMucGiamVoucher(currentVoucher, tongTien);
            System.out.println("💰 Voucher hiện tại giảm: " + currentDiscount);

            // ⭐ VOUCHER TỐT NHẤT KHÁC VỚI VOUCHER HIỆN TẠI
            if (bestVoucher != null && !bestVoucher.getMaGiamGia().equals(currentVoucher.getMaGiamGia())) {
                BigDecimal bestDiscount = tinhMucGiamVoucher(bestVoucher, tongTien);
                System.out.println("💰 Voucher tốt nhất giảm: " + bestDiscount);

                // ⭐ SO SÁNH: VOUCHER TỐT HƠN NẾU GIẢM NHIỀU HƠN
                if (bestDiscount.compareTo(currentDiscount) > 0) {
                    System.out.println("✅ PHÁT HIỆN VOUCHER TỐT HƠN! Chênh lệch: " + (bestDiscount.subtract(currentDiscount)));

                    result.put("hasBetterVoucher", true);
                    result.put("betterVoucher", Map.of(
                            "maGiamGia", bestVoucher.getMaGiamGia(),
                            "tenGiamGia", bestVoucher.getTenGiamGia(),
                            "loaiGiamGia", bestVoucher.getLoaiGiamGia(),
                            "giaTriGiam", bestVoucher.getGiaTriGiam(),
                            "tienGiamHienTai", currentDiscount,
                            "tienGiamMoi", bestDiscount,
                            "chenhLech", bestDiscount.subtract(currentDiscount)
                    ));
                    result.put("message", "Có voucher tốt hơn! " + bestVoucher.getTenGiamGia());
                }
            }

            // Trả về voucher hiện tại nếu hợp lệ
            BigDecimal tienGiam = tinhMucGiamVoucher(currentVoucher, tongTien);
            BigDecimal tongTienMoi = tongTien.subtract(tienGiam);

            result.put("isValid", true);
            result.put("voucherInfo", Map.of(
                    "maGiamGia", currentVoucher.getMaGiamGia(),
                    "tenGiamGia", currentVoucher.getTenGiamGia(),
                    "tienGiam", tienGiam
            ));
            result.put("tongTienMoi", tongTienMoi);
            result.put("message", "Voucher hợp lệ");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi kiểm tra voucher: " + e.getMessage()
            ));
        }
    }



    @PostMapping("/apdungvouchertotnhat-ajax")
    @ResponseBody
    public ResponseEntity<?> apDungVoucherTotNhatAjax(@RequestParam("mahd") String mahd) {
        try {
            HoaDon hoaDon = hoaDonService.findById(mahd);
            if (hoaDon == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy hóa đơn!"
                ));
            }

            List<HoaDonChiTiet> listhdct = hoaDonChiTietService.findById(mahd);
            BigDecimal tongTien = listhdct.stream()
                    .map(HoaDonChiTiet::getThanhTien)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            GiamGia bestVoucher = timVoucherTotNhatChoHoaDon(hoaDon, tongTien);

            if (bestVoucher != null) {
                hoaDon.setMaGiamGia(bestVoucher);
                hoaDonService.save(hoaDon);

                BigDecimal tienGiam = tinhMucGiamVoucher(bestVoucher, tongTien);
                BigDecimal tongTienMoi = tongTien.subtract(tienGiam);

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Đã áp dụng voucher: " + bestVoucher.getTenGiamGia(),
                        "voucher", Map.of(
                                "maGiamGia", bestVoucher.getMaGiamGia(),
                                "tenGiamGia", bestVoucher.getTenGiamGia(),
                                "tienGiam", tienGiam
                        ),
                        "tongTienMoi", tongTienMoi
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "Không có voucher nào để áp dụng!"
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi: " + e.getMessage()
            ));
        }
    }

    // ⭐ Hàm format số tiền cho message
    private String formatSoTien(BigDecimal amount) {
        if (amount == null) return "0";
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }
    // ============================================================
// KIỂM TRA VOUCHER HIỆN TẠI (GIỮ NGUYÊN)
// ============================================================
    private Map<String, Object> kiemTraVoucherHienTai(GiamGia voucher, BigDecimal tongTien, HoaDon hoaDon) {
        if (voucher == null) return null;

        Map<String, Object> result = new HashMap<>();

        // 1. Kiểm tra trạng thái
        String trangThai = giamGiaService.tinhToanTrangThai(voucher);
        if (!"Hoạt động".equals(trangThai)) {
            result.put("type", "VOUCHER_STOPPED");
            result.put("message", "Voucher đã bị ngừng hoạt động!");
            result.put("status", trangThai);
            return result;
        }

        // 2. Kiểm tra số lượng
        if (voucher.getIsVoHan() == null || !voucher.getIsVoHan()) {
            if (voucher.getSoLuong() == null || voucher.getSoLuong() <= 0) {
                result.put("type", "VOUCHER_OUT_OF_STOCK");
                result.put("message", "Voucher đã hết số lượng!");
                result.put("quantity", voucher.getSoLuong());
                return result;
            }
        }

        // 3. Kiểm tra đơn tối thiểu
        if (voucher.getDonToiThieu() != null &&
                voucher.getDonToiThieu().compareTo(BigDecimal.ZERO) > 0 &&
                tongTien.compareTo(voucher.getDonToiThieu()) < 0) {
            result.put("type", "VOUCHER_MIN_ORDER");
            result.put("message", "Đơn hàng chưa đạt giá trị tối thiểu!");
            result.put("minOrder", voucher.getDonToiThieu());
            result.put("currentTotal", tongTien);
            result.put("needMore", voucher.getDonToiThieu().subtract(tongTien));
            return result;
        }

        // 4. Kiểm tra ngày hết hạn
        if (voucher.getNgayKetThuc() != null &&
                voucher.getNgayKetThuc().isBefore(LocalDateTime.now())) {
            result.put("type", "VOUCHER_EXPIRED");
            result.put("message", "Voucher đã hết hạn!");
            result.put("expiredDate", voucher.getNgayKetThuc());
            return result;
        }

        // 5. Kiểm tra ngày bắt đầu
        if (voucher.getNgayBatDau() != null &&
                voucher.getNgayBatDau().isAfter(LocalDateTime.now())) {
            result.put("type", "VOUCHER_NOT_STARTED");
            result.put("message", "Voucher chưa đến ngày áp dụng!");
            result.put("startDate", voucher.getNgayBatDau());
            return result;
        }

        // 6. Kiểm tra loại áp dụng
        if (voucher.getLoaiApDung() != null && voucher.getLoaiApDung() == 2) {
            KhachHang khachHang = hoaDon.getMaKhachHang();
            if (khachHang == null) {
                result.put("type", "VOUCHER_CUSTOMER_ONLY");
                result.put("message", "Voucher này chỉ dành cho khách hàng cụ thể! Vui lòng chọn khách hàng trước.");
                return result;
            }

            boolean isEligible = kiemTraVoucherChoKhachHang(voucher, khachHang);
            if (!isEligible) {
                result.put("type", "VOUCHER_NOT_FOR_CUSTOMER");
                result.put("message", "Voucher này không áp dụng cho khách hàng " + khachHang.getHoTen() + "!");
                return result;
            }
        }

        return null; // Voucher hợp lệ
    }



    @GetMapping("/kiem-tra-san-pham-ngung")
    @ResponseBody
    public ResponseEntity<?> kiemTraVaXoaSanPhamNgung(@RequestParam String mahd) {
        try {
            HoaDon hoaDon = hoaDonRepo.findById(mahd).orElse(null);

            if (hoaDon == null || "Đã thanh toán".equals(hoaDon.getTrangThai())) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "hasChanges", false,
                        "message", "Hóa đơn đã thanh toán hoặc không tồn tại"
                ));
            }

            List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByMaHoaDon_MaHoaDon(mahd);
            List<Map<String, Object>> sanPhamBiXoa = new ArrayList<>();
            boolean hasChanges = false;
            BigDecimal tongTienHang = BigDecimal.ZERO;

            // Danh sách sản phẩm còn lại
            List<Map<String, Object>> danhSachSanPhamConLai = new ArrayList<>();

            Iterator<HoaDonChiTiet> iterator = chiTiets.iterator();
            while (iterator.hasNext()) {
                HoaDonChiTiet chiTiet = iterator.next();
                SanPhamChiTiet spct = chiTiet.getSanPhamChiTiet();

                boolean isNgungBan = false;
                String lyDo = "";

                if (spct == null) {
                    isNgungBan = true;
                    lyDo = "Sản phẩm không tồn tại";
                } else {
                    String trangThai = spct.getTrangThai();
                    if ("Ngừng bán".equals(trangThai)) {
                        isNgungBan = true;
                        lyDo = "Đã ngừng bán";
                    }
                }

                if (isNgungBan) {
                    // ⭐ Lưu thông tin sản phẩm bị xóa - SỬA LẠI
                    Map<String, Object> info = new HashMap<>();

                    // ⭐ QUAN TRỌNG: Lấy mã sản phẩm chi tiết là STRING
                    String maSpct = chiTiet.getSanPhamChiTiet() != null ?
                            chiTiet.getSanPhamChiTiet().getMaSanPhamChiTiet() : null;
                    info.put("maSanPhamChiTiet", maSpct);

                    if (spct != null && spct.getSanPham() != null) {
                        info.put("tenSanPham", spct.getSanPham().getTenSanPham());
                        info.put("mauSac", spct.getMauSac() != null ? spct.getMauSac().getTenMauSac() : "N/A");
                        info.put("kichThuoc", spct.getKichThuoc() != null ? spct.getKichThuoc().getTenKichThuoc() : "N/A");
                    } else {
                        info.put("tenSanPham", "Sản phẩm không xác định");
                        info.put("mauSac", "N/A");
                        info.put("kichThuoc", "N/A");
                    }

                    info.put("soLuong", chiTiet.getSoLuong());
                    info.put("donGia", chiTiet.getDonGia());
                    info.put("lyDo", lyDo);
                    sanPhamBiXoa.add(info);

                    // Trả lại tồn kho
                    if (spct != null) {
                        int soLuongTraLai = chiTiet.getSoLuong();
                        int tonKhoHienTai = spct.getSoLuongTon() != null ? spct.getSoLuongTon() : 0;
                        spct.setSoLuongTon(tonKhoHienTai + soLuongTraLai);
                        sanPhamChiTietRepository.save(spct);
                    }

                    hoaDonChiTietRepository.delete(chiTiet);
                    iterator.remove();
                    hasChanges = true;
                } else {
                    // ⭐ Lưu sản phẩm còn lại - SỬA LẠI
                    if (spct != null && spct.getSanPham() != null) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("maSanPhamChiTiet", spct.getMaSanPhamChiTiet()); // String
                        item.put("tenSanPham", spct.getSanPham().getTenSanPham());
                        item.put("mauSac", spct.getMauSac() != null ? spct.getMauSac().getTenMauSac() : "N/A");
                        item.put("kichThuoc", spct.getKichThuoc() != null ? spct.getKichThuoc().getTenKichThuoc() : "N/A");
                        item.put("soLuong", chiTiet.getSoLuong());
                        item.put("donGia", chiTiet.getDonGia());
                        item.put("thanhTien", chiTiet.getThanhTien());
                        danhSachSanPhamConLai.add(item);
                    }
                    tongTienHang = tongTienHang.add(chiTiet.getThanhTien());
                }
            }

            // Cập nhật hóa đơn
            Map<String, Object> voucherInfo = new HashMap<>();
            BigDecimal tienGiam = BigDecimal.ZERO;

            if (hasChanges) {
                // Tính lại tiền giảm voucher
                if (hoaDon.getMaGiamGia() != null) {
                    tienGiam = tinhTienGiamVoucher(hoaDon, tongTienHang);
                    voucherInfo.put("maGiamGia", hoaDon.getMaGiamGia().getMaGiamGia());
                    voucherInfo.put("tenGiamGia", hoaDon.getMaGiamGia().getTenGiamGia());
                    voucherInfo.put("tienGiam", tienGiam);
                }

                BigDecimal tongTienMoi = tongTienHang.subtract(tienGiam);
                hoaDon.setTongTien(tongTienMoi);
                hoaDonRepo.save(hoaDon);
            }

            // TRẢ VỀ DỮ LIỆU ĐẦY ĐỦ
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("hasChanges", hasChanges);
            response.put("sanPhamBiXoa", sanPhamBiXoa);
            response.put("danhSachSanPham", danhSachSanPhamConLai);
            response.put("tongTienHang", tongTienHang);
            response.put("tongTienMoi", tongTienHang.subtract(tienGiam));
            response.put("voucherInfo", voucherInfo);

            // ⭐ LOG để debug
            System.out.println("=== KIEM TRA SAN PHAM NGUNG ===");
            System.out.println("hasChanges: " + hasChanges);
            System.out.println("sanPhamBiXoa: " + sanPhamBiXoa);
            System.out.println("danhSachSanPhamConLai: " + danhSachSanPhamConLai);
            System.out.println("==================================");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi: " + e.getMessage()
            ));
        }
    }

    // Trong BanHangController.java
    @GetMapping("/get-gio-hang")
    @ResponseBody
    public ResponseEntity<?> getGioHang(@RequestParam String mahd) {
        try {
            HoaDon hoaDon = hoaDonRepo.findById(mahd).orElse(null);
            if (hoaDon == null) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "Không tìm thấy hóa đơn"
                ));
            }

            List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByMaHoaDon_MaHoaDon(mahd);

            // ⭐ ĐỔI TÊN TỪ "chiTiet" THÀNH "danhSachSanPham"
            List<Map<String, Object>> danhSachSanPham = new ArrayList<>();
            BigDecimal tongTienHang = BigDecimal.ZERO;

            for (HoaDonChiTiet ct : chiTiets) {
                SanPhamChiTiet spct = ct.getSanPhamChiTiet();
                Map<String, Object> item = new HashMap<>();

                // ⭐ LẤY MÃ SẢN PHẨM CHI TIẾT
                item.put("maSanPhamChiTiet", ct.getSanPhamChiTiet().getMaSanPhamChiTiet());

                if (spct != null && spct.getSanPham() != null) {
                    // ⭐ TẠO TÊN SẢN PHẨM ĐẦY ĐỦ: Tên + [Màu - Size]
                    String tenSanPham = spct.getSanPham().getTenSanPham()
                            + " [" + spct.getMauSac().getTenMauSac()
                            + " - " + spct.getKichThuoc().getTenKichThuoc() + "]";
                    item.put("tenSanPham", tenSanPham);
                    item.put("mauSac", spct.getMauSac() != null ? spct.getMauSac().getTenMauSac() : "N/A");
                    item.put("kichThuoc", spct.getKichThuoc() != null ? spct.getKichThuoc().getTenKichThuoc() : "N/A");
                } else {
                    item.put("tenSanPham", "Sản phẩm không xác định");
                    item.put("mauSac", "N/A");
                    item.put("kichThuoc", "N/A");
                }

                // ⭐ THÊM CÁC THUỘC TÍNH KHÁC
                item.put("soLuong", ct.getSoLuong());
                item.put("donGia", ct.getDonGia());
                item.put("thanhTien", ct.getThanhTien());
                item.put("tonKho", spct.getSoLuongTon() != null ? spct.getSoLuongTon() : 0);

                danhSachSanPham.add(item);
                tongTienHang = tongTienHang.add(ct.getThanhTien());
            }

            // ⭐ TRẢ VỀ DỮ LIỆU VỚI TÊN FIELD ĐÚNG
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("danhSachSanPham", danhSachSanPham); // ⭐ TÊN NÀY
            response.put("tongTienHang", tongTienHang);
            response.put("tongTienMoi", hoaDon.getTongTien());
            response.put("maHoaDon", mahd);

            // ⭐ THÊM THÔNG TIN VOUCHER NẾU CÓ
            if (hoaDon.getMaGiamGia() != null) {
                Map<String, Object> voucher = new HashMap<>();
                voucher.put("maGiamGia", hoaDon.getMaGiamGia().getMaGiamGia());
                voucher.put("tenGiamGia", hoaDon.getMaGiamGia().getTenGiamGia());
                voucher.put("tienGiam", hoaDon.getTongTien() != null ?
                        tongTienHang.subtract(hoaDon.getTongTien()) : BigDecimal.ZERO);
                response.put("voucherInfo", voucher);
            }

            System.out.println("📦 API get-gio-hang trả về: " + danhSachSanPham.size() + " sản phẩm");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }





}

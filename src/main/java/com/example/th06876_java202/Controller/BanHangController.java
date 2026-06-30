package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.*;
import com.example.th06876_java202.Repository.HoaDonChiTietRepository;
import com.example.th06876_java202.Service.*;
import com.example.th06876_java202.config.CustomUserDetails;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/banhang")
public class BanHangController {

    private final SanPhamService sanPhamService;
    private final SanPhamChiTietService sanPhamChiTietService;
    private final KhachHangService khachHangService;
    private final HoaDonService hoaDonService;
    private final HoaDonChiTietService hoaDonChiTietService;
    private final DotGiamGiaService dotGiamGiaService;
    private final TaiKhoanService taiKhoanService;
    private final GiamGiaService giamGiaService;


    public BanHangController( SanPhamChiTietService sanPhamChiTietService, KhachHangService khachHangService
    , HoaDonService hoaDonService, HoaDonChiTietService hoaDonChiTietService, DotGiamGiaService dotGiamGiaService
    , SanPhamService sanPhamService, TaiKhoanService taiKhoanService ,GiamGiaService giamGiaService) {
        this.sanPhamChiTietService = sanPhamChiTietService;
        this.khachHangService = khachHangService;
        this.hoaDonService = hoaDonService;
        this.hoaDonChiTietService = hoaDonChiTietService;
        this.dotGiamGiaService = dotGiamGiaService;
        this.sanPhamService = sanPhamService;
        this.taiKhoanService = taiKhoanService;
        this.giamGiaService = giamGiaService;
    }

    @GetMapping("/index")
    public String index(@RequestParam(value = "mahd", required = false) Integer mahd, Model model) {
        List<HoaDonChiTiet> hdct = hoaDonChiTietService.findById(mahd);

        HoaDon hoadonHienTai = null;
        BigDecimal tongTienGioHang = BigDecimal.ZERO;
        BigDecimal tienGiamVoucher = BigDecimal.ZERO;
        BigDecimal tongThanhToan = BigDecimal.ZERO;
        BigDecimal tienShip = BigDecimal.ZERO; // Khởi tạo tiền ship mặc định

        if (mahd != null) {
            hoadonHienTai = hoaDonService.findById(mahd).orElse(null);

            if (hdct != null && !hdct.isEmpty()) {
                for (HoaDonChiTiet dc : hdct) {
                    if (dc.getThanhTien() != null) {
                        tongTienGioHang = tongTienGioHang.add(dc.getThanhTien());
                    }
                }
            }

            if (hoadonHienTai != null && hoadonHienTai.getMaGiamGia() != null) {
                GiamGia gg = hoadonHienTai.getMaGiamGia();
                if ("PhanTram".equalsIgnoreCase(gg.getLoaiGiamGia())) {
                    tienGiamVoucher = tongTienGioHang.multiply(gg.getGiaTriGiam()).divide(BigDecimal.valueOf(100));
                    if (gg.getGiamToiDa() != null && gg.getGiamToiDa().compareTo(BigDecimal.ZERO) > 0) {
                        if (tienGiamVoucher.compareTo(gg.getGiamToiDa()) > 0) {
                            tienGiamVoucher = gg.getGiamToiDa();
                        }
                    }
                } else {
                    tienGiamVoucher = gg.getGiaTriGiam();
                }
            }

            if (hoadonHienTai != null && "Online".equalsIgnoreCase(hoadonHienTai.getLoaiBan())) {
                tienShip = hoadonHienTai.getTienShip() != null ? hoadonHienTai.getTienShip() : BigDecimal.valueOf(30000);
            }
        }


        tongThanhToan = tongTienGioHang.subtract(tienGiamVoucher).add(tienShip);
        if (tongThanhToan.compareTo(BigDecimal.ZERO) < 0) {
            tongThanhToan = BigDecimal.ZERO;
        }

        model.addAttribute("hoadonHienTai", hoadonHienTai);
        model.addAttribute("tongTienGioHang", tongTienGioHang);
        model.addAttribute("tienGiamVoucher", tienGiamVoucher);
        model.addAttribute("tongThanhToan", tongThanhToan);
        model.addAttribute("tienShip", tienShip); // Đẩy tiền ship sang HTML hiển thị

        model.addAttribute("listhdct", hdct);
        model.addAttribute("hoadonct", new HoaDonChiTiet());
        model.addAttribute("kh", new KhachHang());
        model.addAttribute("hoadon", new HoaDon());
        model.addAttribute("listgg", giamGiaService.getGiamGia1());
        model.addAttribute("listkh", khachHangService.getAllKhachHang());
        model.addAttribute("listsanpham", sanPhamChiTietService.getalll());
        model.addAttribute("listsanphamms", sanPhamChiTietService.getMsac());
        model.addAttribute("listsanphams", sanPhamChiTietService.getSize());

        return "banhang/index";
    }

    @PostMapping("/chongg")
    public String chonGiamGia(@RequestParam("mahd") Integer mahd,
                              @RequestParam("magg") Integer magg,
                              RedirectAttributes redirectAttributes) {
        try {
            HoaDon hoaDon = hoaDonService.findById(mahd).orElse(null);
            GiamGia giamGia = giamGiaService.getGiamGiaById(magg).orElse(null);

            if (hoaDon != null && giamGia != null) {
                hoaDon.setMaGiamGia(giamGia);
                hoaDonService.save(hoaDon);
                redirectAttributes.addFlashAttribute("mess", "Đã áp dụng mã giảm giá: " + giamGia.getTenGiamGia());
            } else {
                redirectAttributes.addFlashAttribute("mess", "Áp dụng mã giảm giá thất bại!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("mess", "Có lỗi xảy ra khi áp dụng giảm giá!");
        }

        // ĐÃ SỬA: Thêm ?mahd= vào sau index để giữ lại hóa đơn hiện tại trên giao diện
        return "redirect:/banhang/index?mahd=" + mahd;
    }

    @GetMapping("/khachhang")
    public String khachhang(@RequestParam("sdt") String sdt, Model model) {
        List<KhachHang> kh;

        if (sdt == null || sdt.trim().isEmpty()) {
            kh = khachHangService.getAllKhachHang();
        }else {
            kh = khachHangService.findBySdt(sdt);
        }
        model.addAttribute("listkh", kh);
        model.addAttribute("kh", new KhachHang());
        return "banhang/index";
    }

    @GetMapping("/hang")
    public String hangkhachhang(@RequestParam("hang") String hang, Model model) {
        model.addAttribute("kh", new KhachHang());
        return "banhang/index";
    }

    @PostMapping("/themkh")
    public String themkhachhang(
            @Valid @ModelAttribute("kh") KhachHang kh,
            BindingResult bindingResult,
            @RequestParam(value = "mahd", required = false) Integer mahd,
            @RequestParam(value = "ghiChuGiaoHang", required = false) String ghiChuGiaoHang,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("listgg", giamGiaService.getGiamGia1());
            model.addAttribute("listkh", khachHangService.getAllKhachHang());
            model.addAttribute("listsanpham", sanPhamChiTietService.getalll());
            model.addAttribute("listsanphamms", sanPhamChiTietService.getMsac());
            model.addAttribute("listsanphams", sanPhamChiTietService.getSize());
            String lỗiĐầuTiên = bindingResult.getFieldError().getDefaultMessage();
            redirectAttributes.addFlashAttribute("mess", "Lỗi: " + lỗiĐầuTiên);

            return mahd != null ? "redirect:/banhang/index?mahd=" + mahd : "redirect:/banhang/index";
        }

        if (khachHangService.existsBySoDienThoai(kh.getSdt())){
            redirectAttributes.addFlashAttribute("mess", "Số điện thoại đã tồn tại trên hệ thống!");
            return mahd != null ? "redirect:/banhang/index?mahd=" + mahd : "redirect:/banhang/index";
        }

        if (kh.getNgayDangKy() == null) kh.setNgayDangKy(LocalDate.now());

        khachHangService.save(kh);

        List<KhachHang> danhSachTimDuoc = khachHangService.findBySdt(kh.getSdt());

        if (mahd != null && danhSachTimDuoc != null && !danhSachTimDuoc.isEmpty()) {
            KhachHang khachHangVuaThem = danhSachTimDuoc.get(0);

            HoaDon hd = hoaDonService.findById(mahd).orElse(null);
            if (hd != null) {
                hd.setMaKhachHang(khachHangVuaThem);
                if (ghiChuGiaoHang != null && !ghiChuGiaoHang.trim().isEmpty()) {
                    hd.setGhiChu(ghiChuGiaoHang);
                }
                hoaDonService.save(hd);
                redirectAttributes.addFlashAttribute("mess", "Thêm mới và áp dụng khách hàng thành công!");
            }
            return "redirect:/banhang/index?mahd=" + mahd;
        }

        redirectAttributes.addFlashAttribute("mess", "Thêm khách hàng thành công!");
        return "redirect:/banhang/index";
    }

    @GetMapping("/inhoadon/{id}")
    public String inHoaDon(@PathVariable("id") Integer id, Model model) {
        HoaDon hoaDon = hoaDonService.findById(id).orElse(null);


        List<HoaDonChiTiet> listHdct = hoaDonChiTietService.findByHoaDOn(hoaDon);


        BigDecimal tongTien = listHdct.stream()
                .map(item -> item.getThanhTien() != null ? item.getThanhTien() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("hd", hoaDon);
        model.addAttribute("listHdct", listHdct);
        model.addAttribute("tongTien", tongTien);

        return "inhoadon";
    }

    @PostMapping("/taohd")
    public String taohd(@ModelAttribute("hoadon") HoaDon hoaDon,
                        @RequestParam("loaiBan") String loaiBan,
                        Model model,
                        Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) principal;
            TaiKhoan account = userDetails.getTaiKhoan();

            hoaDon.setNgayTao(LocalDate.now());
            hoaDon.setTrangThai("Đang xử lý");
            hoaDon.setLoaiBan(loaiBan);


            if ("Online".equalsIgnoreCase(loaiBan)) {
                hoaDon.setTienShip(BigDecimal.valueOf(30000));
            } else {
                hoaDon.setTienShip(BigDecimal.ZERO);
            }

            if (account.getNhanVien() != null) {
                hoaDon.setMaNhanVien(account.getNhanVien());
            } else {
                return "redirect:/banhang/index?error=no_employee_profile";
            }

            HoaDon hdVuaLuu = hoaDonService.save(hoaDon);
            return "redirect:/banhang/index?mahd=" + hdVuaLuu.getMaHoaDon();
        }

        return "redirect:/login";
    }

    @GetMapping("/sanpham")
    public String sp(Model model){
        List<SanPhamChiTiet> sanPhamChiTiet = sanPhamChiTietService.getalll();
        List<String> sanPhamChiTietms = sanPhamChiTietService.getMsac();
        List<String> sanPhamChiTiets = sanPhamChiTietService.getSize();
        model.addAttribute("listsanpham", sanPhamChiTiet);
        model.addAttribute("listsanphamms", sanPhamChiTietms);
        model.addAttribute("listsanphams", sanPhamChiTiets);
        model.addAttribute("sanpham", new SanPhamChiTiet());
        return "banhang/index";
    }

    @GetMapping("/mausac")
    public String mausac(@RequestParam("mausac") String mausac,Model model){
        List<SanPhamChiTiet> sanPhamChiTiets = sanPhamChiTietService.getByMauSac(mausac);
        model.addAttribute("listsanpham", sanPhamChiTiets);
        model.addAttribute("sanpham", new SanPhamChiTiet());
        return "banhang/index";
    }

    @GetMapping("/size")
    public String size(@RequestParam("size") String size,Model model){
        List<SanPhamChiTiet> sanPhamChiTiets = sanPhamChiTietService.getBySize(size);
        model.addAttribute("listsanpham", sanPhamChiTiets);
        model.addAttribute("listsanphamms", sanPhamChiTietService.getMsac());
        model.addAttribute("listsanphams", sanPhamChiTietService.getSize());
        model.addAttribute("sanpham", new SanPhamChiTiet());
        return "banhang/index";
    }

    @GetMapping("/tt")
    public String tt(@RequestParam("tt") String tt,Model model){
        List<SanPhamChiTiet> sanPhamChiTiets = sanPhamChiTietService.getByTT(tt);
        model.addAttribute("listsanpham", sanPhamChiTiets);
        model.addAttribute("listsanphamms", sanPhamChiTietService.getMsac());
        model.addAttribute("listsanphams", sanPhamChiTietService.getSize());
        model.addAttribute("sanpham", new SanPhamChiTiet());
        return "banhang/index";
    }

    @PostMapping("/themsphd")
    public String tsphd(@RequestParam("mahd") Integer mahd,
                        @RequestParam("mactsp") Integer mactsp,
                        @RequestParam("sluong") Integer sluong,
                        @RequestParam("masp") Integer masp,
                        Model model,
                        RedirectAttributes redirectAttributes) {

        HoaDon hdd = hoaDonService.findById(mahd).orElse(null);
        SanPhamChiTiet spct = sanPhamChiTietService.findbyId(mactsp).orElse(null);

        // 1. Kiểm tra các điều kiện đầu vào
        if (hdd == null) {
            redirectAttributes.addFlashAttribute("mess", "Vui lòng tạo hoá đơn");
            return "redirect:/banhang/index";
        }

        if (spct == null) {
            redirectAttributes.addFlashAttribute("mess", "Vui lòng chọn sản phẩm");
            return "redirect:/banhang/index";
        }

        if (sluong <= 0) {
            redirectAttributes.addFlashAttribute("mess", "Số lượng phải lớn hơn 0");
            return "redirect:/banhang/index";
        }

        if (sluong > spct.getSoLuongTon()) {
            redirectAttributes.addFlashAttribute("mess", "Số lượng tồn không đủ");
            return "redirect:/banhang/index";
        }

        // 2. Xử lý cộng dồn số lượng nếu sản phẩm đã có trong giỏ hàng
        HoaDonChiTiet hdct = hoaDonChiTietService.findAll(mahd, mactsp);
        if (hdct == null) {
            hdct = new HoaDonChiTiet();
            hdct.setMaHoaDon(hdd);
            hdct.setSanPhamChiTiet(spct);
            hdct.setSoLuong(sluong);
            hdct.setDonGia(spct.getGiaBan());
        } else {
            int sluongm = hdct.getSoLuong() + sluong;

            if (sluongm > spct.getSoLuongTon()) {
                redirectAttributes.addFlashAttribute("mess", "Số lượng vượt quá tồn kho");
                return "redirect:/banhang/index?mahd=" + mahd;
            }
            hdct.setSoLuong(sluongm);
        }

        // 3. LOGIC TÍNH TOÁN GIẢM GIÁ VÀ THÀNH TIỀN THEO SỐ LƯỢNG

        // Tính tổng tiền gốc trước giảm giá (Đơn giá x Tổng số lượng mới)
        BigDecimal thanhtiengoc = hdct.getDonGia().multiply(BigDecimal.valueOf(hdct.getSoLuong()));

        // Tìm số tiền giảm giá LỚN NHẤT tính trên 1 SẢN PHẨM
        BigDecimal giamLonNhatCuaMotSp = BigDecimal.ZERO;
        List<DotGiamGia> listdgg = dotGiamGiaService.getBymasp(spct.getSanPham().getMaSanPham());


        BigDecimal soLuongBd = BigDecimal.valueOf(hdct.getSoLuong());
        BigDecimal tongTienGiam = giamLonNhatCuaMotSp.multiply(soLuongBd);

        // 4. Đồng bộ dữ liệu xuống Entity và lưu vào database
        hdct.setTienGiam(tongTienGiam);
        hdct.setThanhTien(thanhtiengoc.subtract(tongTienGiam));

        hoaDonChiTietService.luu(hdct);

        return "redirect:/banhang/index?mahd=" + mahd;
    }

    @PostMapping("/huyhd")
    public String huyhd(@RequestParam("mahd") Integer mahd, RedirectAttributes redirectAttributes) {
        HoaDon hd = hoaDonService.findById(mahd).orElse(null);

        if (hd == null) {
            redirectAttributes.addFlashAttribute("mess", "Chua tạo hoá đơn");
            return "redirect:/banhang/index";
        }

        hd.setTrangThai("Đã huỷ");
        hoaDonService.save(hd);
        redirectAttributes.addFlashAttribute("mess", "Hủy hóa đơn thành công");
        return "redirect:/banhang/index";
    }

    @PostMapping("/thanhtoanhd")
    public String thanhtoanhd(@RequestParam("mahd") Integer mahd,
                              @RequestParam("tienkhachdua") BigDecimal tienkhacdua,
                              @RequestParam("phuongthuc") String phuongthuc,
                              RedirectAttributes redirectAttributes) {
        HoaDon hd = hoaDonService.findById(mahd).orElse(null);


        if (hd == null) {
            redirectAttributes.addFlashAttribute("mess", "Chưa tạo hoá đơn!");
            return "redirect:/banhang/index";
        }

        if ("Online".equalsIgnoreCase(hd.getLoaiBan()) && hd.getMaKhachHang() == null) {
            redirectAttributes.addFlashAttribute("mess", "Không thể giao hàng! Hóa đơn Online bắt buộc phải có thông tin khách hàng và địa chỉ.");
            return "redirect:/banhang/index?mahd=" + mahd;
        }

        List<HoaDonChiTiet> listhdct = hoaDonChiTietService.findById(mahd);

        if (listhdct == null || listhdct.isEmpty()) {
            redirectAttributes.addFlashAttribute("mess", "Không thể thanh toán! Hoá đơn chưa có sản phẩm nào.");
            return "redirect:/banhang/index?mahd=" + mahd;
        }

        BigDecimal tongtien = BigDecimal.ZERO;
        for (HoaDonChiTiet dc : listhdct) {
            if (dc.getThanhTien() != null) {
                tongtien = tongtien.add(dc.getThanhTien());
            }
        }

        if (hd.getMaGiamGia() != null) {
            GiamGia gg = hd.getMaGiamGia();
            BigDecimal tienGiamVoucher = BigDecimal.ZERO;
            if ("PhanTram".equalsIgnoreCase(gg.getLoaiGiamGia())) {
                tienGiamVoucher = tongtien.multiply(gg.getGiaTriGiam()).divide(BigDecimal.valueOf(100));
                if (gg.getGiamToiDa() != null && gg.getGiamToiDa().compareTo(BigDecimal.ZERO) > 0) {
                    if (tienGiamVoucher.compareTo(gg.getGiamToiDa()) > 0) {
                        tienGiamVoucher = gg.getGiamToiDa();
                    }
                }
            } else {
                tienGiamVoucher = gg.getGiaTriGiam();
            }
            tongtien = tongtien.subtract(tienGiamVoucher);
        }

        // CỘNG THÊM TIỀN SHIP VÀO TỔNG TIỀN PHẢI TRẢ NẾU LÀ ĐƠN ONLINE
        if ("Online".equalsIgnoreCase(hd.getLoaiBan())) {
            BigDecimal ship = hd.getTienShip() != null ? hd.getTienShip() : BigDecimal.valueOf(30000);
            tongtien = tongtien.add(ship);
        }

        if (tongtien.compareTo(BigDecimal.ZERO) < 0) {
            tongtien = BigDecimal.ZERO;
        }

        if (tienkhacdua.compareTo(tongtien) < 0) {
            redirectAttributes.addFlashAttribute("mess", "Tiền khách đưa còn thiếu: " + tongtien.subtract(tienkhacdua) + " VNĐ");
            return "redirect:/banhang/index?mahd=" + mahd;
        }

        for (HoaDonChiTiet dc : listhdct) {
            SanPhamChiTiet spct = dc.getSanPhamChiTiet();
            if (dc.getSoLuong() > spct.getSoLuongTon()) {
                redirectAttributes.addFlashAttribute("mess", "Sản phẩm: " + spct.getSanPham().getTenSanPham() + " không đủ hàng tồn kho!");
                return "redirect:/banhang/index?mahd=" + mahd;
            }
        }

        for (HoaDonChiTiet dc : listhdct) {
            SanPhamChiTiet spct = dc.getSanPhamChiTiet();
            spct.setSoLuongTon(spct.getSoLuongTon() - dc.getSoLuong());
            sanPhamChiTietService.them(spct);
        }

        if (hd.getMaGiamGia() != null) {
            GiamGia gg = hd.getMaGiamGia();
            if (gg.getSoLuong() != null && gg.getSoLuong() > 0) {
                giamGiaService.giamSoLuongVoucher(gg.getMaGiamGia());
            } else {
                redirectAttributes.addFlashAttribute("mess", "Mã giảm giá này đã hết lượt sử dụng!");
                return "redirect:/banhang/index?mahd=" + mahd;
            }
        }

        hd.setTongTien(tongtien);
        hd.setTienKhachDua(tienkhacdua);
        hd.setTienThua(tienkhacdua.subtract(tongtien));
        hd.setPhuongThucThanhToan(phuongthuc);

        if ("Online".equalsIgnoreCase(hd.getLoaiBan())) {
            hd.setTrangThai("Đang giao");
            redirectAttributes.addFlashAttribute("mess", "Đơn hàng đã chuyển sang trạng thái: Đang giao!");
        } else {
            hd.setTrangThai("Đã thanh toán");
            redirectAttributes.addFlashAttribute("mess", "Thanh toán thành công!");
        }

        hoaDonService.save(hd);
        return "redirect:/banhang/index";
    }

    @PostMapping("/chonkh")
    public String chonkh(@RequestParam("mahd") Integer mahd,
                         @RequestParam("makh") Integer makh,
                         RedirectAttributes redirectAttributes) {
        HoaDon hd = hoaDonService.findById(mahd).orElse(null);
        KhachHang kh = khachHangService.getKhachHangById(makh);

        if (hd == null) {
            redirectAttributes.addFlashAttribute("mess", "Vui lòng tạo hoá đơn");
            return "redirect:/banhang/index";
        }

        hd.setMaKhachHang(kh);

        hoaDonService.save(hd);

        return "redirect:/banhang/index?mahd=" + mahd;
    }

    @PostMapping("/giamsp")
    public String giamSanPham(@RequestParam("mahd") Integer mahd, @RequestParam("mactsp") Integer mactsp, RedirectAttributes redirectAttributes) {

        HoaDonChiTiet hdct = hoaDonChiTietService.findAll(mahd, mactsp);

        if (hdct == null) {
            redirectAttributes.addFlashAttribute("mess", "Không tìm thấy sản phẩm trong hóa đơn");
            return "redirect:/banhang/index?mahd=" + mahd;
        }

        int slMoi = hdct.getSoLuong() - 1;

        if (slMoi <= 0) {
            hoaDonChiTietService.xoa(hdct);
        } else {
            // Tính lại tiền giảm bình quân trên 1 sản phẩm trước đó
            BigDecimal slCu = BigDecimal.valueOf(hdct.getSoLuong());
            BigDecimal tienGiamMotSp = hdct.getTienGiam().divide(slCu, 2, java.math.RoundingMode.HALF_UP);

            // Gán số lượng mới
            hdct.setSoLuong(slMoi);
            BigDecimal slMoiBd = BigDecimal.valueOf(slMoi);

            // Tính toán lại tổng tiền gốc và tổng tiền giảm mới
            BigDecimal tongTienGocMoi = hdct.getDonGia().multiply(slMoiBd);
            BigDecimal tongTienGiamMoi = tienGiamMotSp.multiply(slMoiBd);

            hdct.setTienGiam(tongTienGiamMoi);
            hdct.setThanhTien(tongTienGocMoi.subtract(tongTienGiamMoi));

            hoaDonChiTietService.luu(hdct);
        }

        return "redirect:/banhang/index?mahd=" + mahd;
    }

    @PostMapping("/xoasp")
    public String xoaSanPham(
            @RequestParam("mahd") Integer mahd,
            @RequestParam("mactsp") Integer mactsp,
            RedirectAttributes redirectAttributes
    ) {

        HoaDonChiTiet hdct = hoaDonChiTietService.findAll(mahd, mactsp);

        if (hdct == null) {
            redirectAttributes.addFlashAttribute("mess", "Không tìm thấy sản phẩm");
            return "redirect:/banhang/index?mahd=" + mahd;
        }

        hoaDonChiTietService.xoa(hdct);

        return "redirect:/banhang/index?mahd=" + mahd;
    }

}

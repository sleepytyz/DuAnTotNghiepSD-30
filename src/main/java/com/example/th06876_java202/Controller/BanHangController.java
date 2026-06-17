package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.*;
import com.example.th06876_java202.Repository.HoaDonChiTietRepository;
import com.example.th06876_java202.Service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    public BanHangController( SanPhamChiTietService sanPhamChiTietService, KhachHangService khachHangService
    , HoaDonService hoaDonService, HoaDonChiTietService hoaDonChiTietService, DotGiamGiaService dotGiamGiaService
    , SanPhamService sanPhamService) {
        this.sanPhamChiTietService = sanPhamChiTietService;
        this.khachHangService = khachHangService;
        this.hoaDonService = hoaDonService;
        this.hoaDonChiTietService = hoaDonChiTietService;
        this.dotGiamGiaService = dotGiamGiaService;
        this.sanPhamService = sanPhamService;
    }

    @GetMapping("/index")
    public String index( @RequestParam(value = "mahd", required = false) Integer mahd, Model model){
        List<HoaDonChiTiet> hdct = hoaDonChiTietService.findById(mahd);

        HoaDon hoadonHienTai = null;
        BigDecimal tongTienGioHang = BigDecimal.ZERO;

        if (mahd != null) {
            hoadonHienTai = hoaDonService.findById(mahd).orElse(null);

            if (hdct != null && !hdct.isEmpty()) {
                for (HoaDonChiTiet dc : hdct) {
                    if (dc.getThanhTien() != null) {
                        tongTienGioHang = tongTienGioHang.add(dc.getThanhTien());
                    }
                }
            }
        }

        // Đẩy 2 biến quan trọng này ra ngoài View
        model.addAttribute("hoadonHienTai", hoadonHienTai);
        model.addAttribute("tongTienGioHang", tongTienGioHang);
        // -----------------------------

        model.addAttribute("listhdct", hdct);
        model.addAttribute("hoadonct", new HoaDonChiTiet());
        model.addAttribute("kh", new KhachHang());
        model.addAttribute("hoadon", new HoaDon());

        model.addAttribute("listkh", khachHangService.getKhachHang());

        model.addAttribute("listsanpham",
                sanPhamChiTietService.getall());

        model.addAttribute("listsanphamms",
                sanPhamChiTietService.getMsac());

        model.addAttribute("listsanphams",
                sanPhamChiTietService.getSize());

        return "banhang/index";
    }

    @GetMapping("/khachhang")
    public String khachhang(@RequestParam("sdt") String sdt, Model model) {
        List<KhachHang> kh;

        if (sdt == null || sdt.trim().isEmpty()) {
            kh = khachHangService.getKhachHang();
        }else {
            kh = khachHangService.findBySdt(sdt);
        }
        model.addAttribute("listkh", kh);
        model.addAttribute("kh", new KhachHang());
        return "banhang/index";
    }

    @GetMapping("/hang")
    public String hangkhachhang(@RequestParam("hang") String hang, Model model) {
        List<KhachHang> kh = khachHangService.findByHangKH(hang);
        model.addAttribute("listkh", kh);
        model.addAttribute("kh", new KhachHang());
        return "banhang/index";
    }

    @PostMapping("/themkh")
    public String themkhachhang(
            @ModelAttribute("kh") KhachHang kh,
            @RequestParam(value = "mahd", required = false) Integer mahd, // Đón mã hóa đơn từ giao diện gửi lên
            Model model,
            RedirectAttributes redirectAttributes) {

        // 1. Kiểm tra trùng số điện thoại
        if (khachHangService.existsBySoDienThoai(kh.getSdt())){
            redirectAttributes.addFlashAttribute("mess", "Số điện thoại đã tồn tại");

            // Nếu có hóa đơn thì quay lại đúng hóa đơn đó, không thì về index chung
            return mahd != null ? "redirect:/banhang/index?mahd=" + mahd : "redirect:/banhang/index";
        }

        // 2. Thêm mới khách hàng vào database bằng hàm của bạn
        khachHangService.them(kh);

        // Vì hàm 'them(kh)' thường không trả về đối tượng có ID, ta sẽ dùng SĐT vừa thêm để tìm lại khách hàng này từ DB
        List<KhachHang> danhSachTimDuoc = khachHangService.findBySdt(kh.getSdt());

        // 3. Nếu tìm thấy khách hàng vừa tạo và đang có hóa đơn thao tác, tiến hành gán luôn
        if (mahd != null && danhSachTimDuoc != null && !danhSachTimDuoc.isEmpty()) {
            KhachHang khachHangVuaThem = danhSachTimDuoc.get(0); // Lấy khách hàng vừa tạo

            HoaDon hd = hoaDonService.findById(mahd).orElse(null);
            if (hd != null) {
                hd.setMaKhachHang(khachHangVuaThem); // Gán khách hàng vào hóa đơn
                hoaDonService.save(hd);             // Lưu cập nhật hóa đơn
                redirectAttributes.addFlashAttribute("mess", "Thêm mới và áp dụng khách hàng thành công!");
            }

            // Quay trở lại đúng hóa đơn hiện tại để không bị mất dữ liệu màn hình
            return "redirect:/banhang/index?mahd=" + mahd;
        }

        redirectAttributes.addFlashAttribute("mess", "Thêm khách hàng thành công!");
        return "redirect:/banhang/index";
    }

    @GetMapping("/inhoadon/{id}")
    public String inHoaDon(@PathVariable("id") Integer id, Model model) {
        // 1. Tìm hóa đơn theo mã hóa đơn (thay đổi Method tìm kiếm tùy thuộc vào Repository/Service của bạn)
        HoaDon hoaDon = hoaDonService.findById(id).orElse(null);

        // 2. Lấy danh sách sản phẩm chi tiết của hóa đơn này (Hàm vừa viết ở trên)
        List<HoaDonChiTiet> listHdct = hoaDonChiTietService.findByHoaDOn(hoaDon);

// 3. Tính tổng tiền bằng BigDecimal (Chuẩn, không lỗi convert, không sai số)
        BigDecimal tongTien = listHdct.stream()
                .map(item -> item.getThanhTien() != null ? item.getThanhTien() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

// 4. Đẩy dữ liệu ra giao diện Thymeleaf
        model.addAttribute("hd", hoaDon);
        model.addAttribute("listHdct", listHdct);
        model.addAttribute("tongTien", tongTien); // Thymeleaf vẫn nhận và hiển thị bình thường

        // 5. Trả về file HTML giao diện bản in (nằm trong thư mục src/main/resources/templates/inhoadon.html)
        return "inhoadon";
    }

    @PostMapping("/taohd")
    public String taohd(@ModelAttribute("hoadon") HoaDon hoaDon, Model model, HttpSession session) {
        hoaDon.setNgayTao(LocalDate.now());
        Account account = (Account) session.getAttribute("user");
        if (account != null) {
            hoaDon.setMaNhanVien(account.getMaNhanVien());
        }
        hoaDon.setTrangThai("Đang xử lý");
        HoaDon hdVuaLuu = hoaDonService.save(hoaDon);

        return "redirect:/banhang/index?mahd=" + hdVuaLuu.getMaHoaDon();
    }

    @GetMapping("/sanpham")
    public String sp(Model model){
        List<SanPhamChiTiet> sanPhamChiTiet = sanPhamChiTietService.getall();
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

        for (DotGiamGia dg : listdgg) {
            BigDecimal tienGiamCuaMotSp = BigDecimal.ZERO;

            if (dg.getLoaiGiamGia().equalsIgnoreCase("Phần trăm")) {
                // Tiền giảm 1 SP = Đơn giá gốc x % Giảm / 100
                tienGiamCuaMotSp = hdct.getDonGia().multiply(dg.getGiaTriGiam()).divide(BigDecimal.valueOf(100));

                // Kiểm tra điều kiện giảm tối đa của 1 sản phẩm nếu có cấu hình
                if (dg.getGiamToiDa() != null) {
                    BigDecimal giamtoida = dg.getGiamToiDa();
                    if (tienGiamCuaMotSp.compareTo(giamtoida) > 0) {
                        tienGiamCuaMotSp = giamtoida;
                    }
                }
            } else {
                // Nếu giảm theo số tiền mặt cố định (Ví dụ: Giảm thẳng 20k/sản phẩm)
                tienGiamCuaMotSp = dg.getGiaTriGiam();
            }

            // Giữ lại chương trình ưu đãi lớn nhất cho 1 sản phẩm
            if (tienGiamCuaMotSp.compareTo(giamLonNhatCuaMotSp) > 0) {
                giamLonNhatCuaMotSp = tienGiamCuaMotSp;
            }
        }

        // Tổng tiền giảm thực tế = Tiền giảm của 1 SP x Tổng số lượng khách mua
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
            redirectAttributes.addFlashAttribute("mess", "Chua tạo hoá đơn");
            return "redirect:/banhang/index";
        }

        List<HoaDonChiTiet> listhdct = hoaDonChiTietService.findById(mahd);

        if (listhdct == null) {
            redirectAttributes.addFlashAttribute("mess", "Hoá đơn chưa có sản phẩm");
            return "redirect:/banhang/index";
        }

        BigDecimal tongtien = BigDecimal.ZERO;

        for (HoaDonChiTiet dc : listhdct) {
            tongtien = tongtien.add(dc.getThanhTien());
        }

        if(tienkhacdua.compareTo(tongtien) < 0){
            redirectAttributes.addFlashAttribute("mess", "Tiền khách đưa còn thiếu : " + tongtien.subtract(tienkhacdua) + "VNĐ");
            return "redirect:/banhang/index";
        }

        for (HoaDonChiTiet dc : listhdct) {
            SanPhamChiTiet spct = dc.getSanPhamChiTiet();

            if (dc.getSoLuong() > spct.getSoLuongTon()) {
                redirectAttributes.addFlashAttribute("mess", "Sản phẩm : " + spct.getSanPham().getTenSanPham() + "không đủ hàng tồn kho");
                return "redirect:/banhang/index";
            }

        }

        for (HoaDonChiTiet dc : listhdct) {
            SanPhamChiTiet spct = dc.getSanPhamChiTiet();

            spct.setSoLuongTon(spct.getSoLuongTon() - dc.getSoLuong());

            sanPhamChiTietService.them(spct);

        }

        hd.setTongTien(tongtien);

        hd.setTienKhachDua(tienkhacdua);

        hd.setTienThua(
                tienkhacdua.subtract(tongtien)
        );

        hd.setPhuongThucThanhToan(phuongthuc);

        hd.setTrangThai("Đã thanh toán");

        hoaDonService.save(hd);

        redirectAttributes.addFlashAttribute(
                "mess",
                "Thanh toán thành công"
        );

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

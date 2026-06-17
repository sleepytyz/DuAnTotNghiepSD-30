package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.*;
import com.example.th06876_java202.Service.*;
import com.example.th06876_java202.Entity.*;
import com.example.th06876_java202.Service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
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
    public String themkhachhang(@ModelAttribute("kh") KhachHang kh, Model model , RedirectAttributes redirectAttributes) {

        if (khachHangService.existsBySoDienThoai(kh.getSdt())){
            redirectAttributes.addFlashAttribute("kh", "Số điện thoại đã tồn tại");
            return "redirect:/banhang/index";
        }
        khachHangService.save(kh);
        return "redirect:/banhang/index";
    }



//    @PostMapping("/taohd")
//    public String taohd(@ModelAttribute("hoadon") HoaDon hoaDon, Model model, HttpSession session) {
//        hoaDon.setNgayTao(LocalDate.now());
//        TaiKhoan account = (TaiKhoan) session.getAttribute("user");
//        hoaDon.setMaNhanVien(account.getMaNhanVien());
//        hoaDon.setTrangThai("Đang xử lý");
//        hoaDonService.save(hoaDon);
//        return "redirect:/banhang/index";
//    }

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
    public String tsphd(@RequestParam("mahd") Integer mahd, @RequestParam("mactsp") Integer mactsp, @RequestParam("sluong") Integer sluong,
            @RequestParam("masp") Integer masp ,Model model, RedirectAttributes redirectAttributes){

        HoaDon hdd = hoaDonService.findById(mahd).orElse(null);
        SanPhamChiTiet spct = sanPhamChiTietService.findbyId(mactsp).orElse(null);


        if(hdd == null){
            redirectAttributes.addFlashAttribute("mess", "Vui lòng tạo hoá đơn");
            return "redirect:/banhang/index";
        }

        if (spct == null) {
            redirectAttributes.addFlashAttribute("mess", "Vui lòng chọn sản phẩm");
            return "redirect:/banhang/index";
        }

        if(sluong <= 0){
            redirectAttributes.addFlashAttribute("mess", "Số lượng phải lớn hơn 0");
            return "redirect:/banhang/index";
        }

        if (sluong > spct.getSoLuongTon()){
            redirectAttributes.addFlashAttribute("mess", "Số lượng tồn không đủ");
            return "redirect:/banhang/index";
        }

        HoaDonChiTiet hdct = hoaDonChiTietService.findAll(mahd,mactsp);
        if (hdct == null){
            hdct =  new HoaDonChiTiet();
            hdct.setMaHoaDon(hdd);
            hdct.setSanPhamChiTiet(spct);
            hdct.setSoLuong(sluong);
            hdct.setDonGia(spct.getGiaBan());
        }else{
            int sluongm = hdct.getSoLuong() + sluong;

            if (sluongm > spct.getSoLuongTon()){
                redirectAttributes.addFlashAttribute("mess", "Số lượng vượt quá tồn kho");
                return "redirect:/banhang/index";
            }
            hdct.setSoLuong(sluongm);
        }

        BigDecimal thanhtiengoc = hdct.getDonGia().multiply(BigDecimal.valueOf(hdct.getSoLuong()));

        BigDecimal giamlonnhat = BigDecimal.ZERO;

        List<DotGiamGia> listdgg = dotGiamGiaService.getBymasp(spct.getSanPham().getMaSanPham());

        hdct.setThanhTien(hdct.getDonGia().multiply(java.math.BigDecimal.valueOf(hdct.getSoLuong())));

        for (DotGiamGia dg : listdgg) {
            BigDecimal tiengiam = BigDecimal.ZERO;

            if (dg.getLoaiGiamGia().equalsIgnoreCase("Phần trăm")){

                tiengiam = thanhtiengoc.multiply(dg.getGiaTriGiam()).divide(BigDecimal.valueOf(100));

                if(dg.getGiamToiDa() != null){

                    BigDecimal giamtoida = (dg.getGiamToiDa());

                    if(tiengiam.compareTo(giamtoida) > 0){
                        tiengiam = giamtoida;
                    }

                }
            }else {
                tiengiam = dg.getGiaTriGiam();
            }

            if (tiengiam.compareTo(giamlonnhat) > 0) {
                giamlonnhat = tiengiam;
            }

        }

        hdct.setTienGiam(giamlonnhat);

        hdct.setThanhTien(
                thanhtiengoc.subtract(giamlonnhat)
        );

        hoaDonChiTietService.luu(hdct);

        return "redirect:/banhang/index";
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

        return "redirect:/banhang/index";
    }



}

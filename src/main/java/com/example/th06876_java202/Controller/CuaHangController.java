package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.*;
import com.example.th06876_java202.Service.*;
import com.example.th06876_java202.Storefront.BienTheVM;
import com.example.th06876_java202.Storefront.SanPhamCardVM;
import com.example.th06876_java202.Storefront.SanPhamHienThiService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Trang xem sản phẩm của website bán hàng FS Shoes (công khai, không cần đăng nhập).
 */
@Controller
@RequiredArgsConstructor
public class CuaHangController {

    private final SanPhamService sanPhamService;
    private final SanPhamHienThiService sanPhamHienThiService;
    private final DanhMucSanPhamService danhMucSanPhamService;
    private final ThuongHieuService thuongHieuService;
    private final KieuGiayService kieuGiayService;

    @GetMapping("/san-pham")
    public String danhSach(
            @RequestParam(required = false) Integer danhMuc,
            @RequestParam(required = false) Integer thuongHieu,
            @RequestParam(required = false) Integer kieuGiay,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        int size = 12;
        Page<SanPham> trang = sanPhamService.searchSanPham(danhMuc, true, thuongHieu, kieuGiay, q, PageRequest.of(Math.max(page, 0), size));
        List<SanPhamCardVM> sanPhams = sanPhamHienThiService.taoDanhSachCard(trang.getContent());

        model.addAttribute("sanPhams", sanPhams);
        model.addAttribute("trang", trang);
        model.addAttribute("danhMucs", danhMucSanPhamService.getAll().stream().filter(DanhMucSanPham::isTrangThai).toList());
        model.addAttribute("thuongHieus", thuongHieuService.findAll().stream().filter(ThuongHieu::isTrangThai).toList());
        model.addAttribute("kieuGiays", kieuGiayService.findAll().stream().filter(KieuGiay::isTrangThai).toList());

        model.addAttribute("fDanhMuc", danhMuc);
        model.addAttribute("fThuongHieu", thuongHieu);
        model.addAttribute("fKieuGiay", kieuGiay);
        model.addAttribute("fQ", q);

        return "cuahang/danh-sach";
    }

    @GetMapping("/san-pham/{id}")
    public String chiTiet(@PathVariable Integer id, Model model) {
        SanPham sp = sanPhamService.findById(id).orElse(null);
        if (sp == null || sp.getTrangThai() == null || !sp.getTrangThai()) {
            return "redirect:/san-pham";
        }

        List<BienTheVM> bienThe = sanPhamHienThiService.taoDanhSachBienThe(id);

        List<SanPhamCardVM> lienQuan = List.of();
        if (sp.getDanhMucSanPham() != null) {
            Page<SanPham> trangLienQuan = sanPhamService.searchSanPham(
                    sp.getDanhMucSanPham().getMaDanhMuc(), true, null, null, null, PageRequest.of(0, 5));
            lienQuan = sanPhamHienThiService.taoDanhSachCard(
                    trangLienQuan.getContent().stream().filter(s -> !s.getMaSanPham().equals(id)).limit(4).toList());
        }

        model.addAttribute("sp", sp);
        model.addAttribute("bienThe", bienThe);
        model.addAttribute("lienQuan", lienQuan);
        return "cuahang/chi-tiet";
    }
}

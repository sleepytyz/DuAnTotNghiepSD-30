package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.*;
import com.example.th06876_java202.Entity.*;
import com.example.th06876_java202.Service.*;
import com.example.th06876_java202.Service.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/sanphamct")
public class SanPhamChiTietController {

    private final SanPhamChiTietService sanPhamChiTietService;
    private final DanhMucSanPhamService danhMucSanPhamService;
    private final SanPhamService sanPhamService;
    private final SanPhamHinhAnhService sanPhamHinhAnhService;
    private final MauSacService mauSacService;
    private final KichThuocService kichThuocService;

    public SanPhamChiTietController( SanPhamChiTietService sanPhamChiTietService, DanhMucSanPhamService danhMucSanPhamService,
                                     SanPhamService sanPhamService, SanPhamHinhAnhService sanPhamHinhAnhService,
                                     MauSacService mauSacService, KichThuocService kichThuocService) {
        this.sanPhamChiTietService = sanPhamChiTietService;
        this.danhMucSanPhamService = danhMucSanPhamService;
        this.sanPhamService = sanPhamService;
        this.sanPhamHinhAnhService = sanPhamHinhAnhService;
        this.mauSacService = mauSacService;
        this.kichThuocService = kichThuocService;
    }

    @GetMapping("/image/{id}")
    @ResponseBody
    public byte[] image(@PathVariable Integer id){
        return sanPhamHinhAnhService
                .findById(id)
                .orElseThrow()
                .getHinhAnh();
    }

    @GetMapping("/index")
    public String index( Model model ) {
        List<SanPhamChiTiet> listspct = sanPhamChiTietService.getall();
        List<SanPham> Listsp = sanPhamService.getAll();
        List<DanhMucSanPham> listdmsp = danhMucSanPhamService.getAll();
        model.addAttribute("listspct", listspct);
        model.addAttribute("listsp", Listsp);
        model.addAttribute("listdmsp", listdmsp);
        model.addAttribute("listms", mauSacService.findAll());
        model.addAttribute("lists", kichThuocService.getAllKichThuoc());
        model.addAttribute("sanphamct", new SanPhamChiTiet());
        model.addAttribute("isEdit", null);
        return "sanphamct/index";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") int id, Model model ) {
        SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietService.findbyId(id).orElse(null);
        model.addAttribute("sanphamct", sanPhamChiTiet);
        List<SanPhamChiTiet> listspct = sanPhamChiTietService.getall();
        List<SanPham> Listsp = sanPhamService.getAll();
        List<DanhMucSanPham> listdmsp = danhMucSanPhamService.getAll();
        model.addAttribute("listspct", listspct);
        model.addAttribute("listsp", Listsp);
        model.addAttribute("listdmsp", listdmsp);
        model.addAttribute("listms", mauSacService.findAll());
        model.addAttribute("lists", kichThuocService.getAllKichThuoc());
        model.addAttribute("showModal", true);
        model.addAttribute("isEdit", true);
        return "sanphamct/index";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("sanphamct") @Valid SanPhamChiTiet sanPhamChiTiet, Errors errors, Model model) {
        if (errors.hasErrors()) {
            prepareModel(model);
            model.addAttribute("showModal", true);
            model.addAttribute("isEdit", null);
            return "sanphamct/index";
        }


        sanPhamChiTiet.setSanPham(sanPhamService.findById(sanPhamChiTiet.getSanPham().getMaSanPham()).orElseThrow());
        sanPhamChiTiet.setKichThuoc(kichThuocService.getKichThuocById(sanPhamChiTiet.getKichThuoc().getMaKichThuoc()).orElseThrow());
        sanPhamChiTiet.setMauSac(mauSacService.findById(sanPhamChiTiet.getMauSac().getMaMauSac()).orElseThrow());


        sanPhamChiTietService.capNhatTrangThaii(sanPhamChiTiet);
        sanPhamChiTiet.setNgayTao(LocalDate.now());

        sanPhamChiTietService.them(sanPhamChiTiet);
        return "redirect:/sanphamct/index";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute("sanphamct") @Valid SanPhamChiTiet sanPhamChiTiet, Errors errors, Model model) {
        if (errors.hasErrors()) {
            prepareModel(model);
            model.addAttribute("showModal", true);
            model.addAttribute("isEdit", true);
            return "sanphamct/index";
        }


        SanPhamChiTiet old = sanPhamChiTietService.findbyId(sanPhamChiTiet.getMaSanPhamChiTiet()).orElseThrow();


        old.setSanPham(sanPhamService.findById(sanPhamChiTiet.getSanPham().getMaSanPham()).orElseThrow());
        old.setKichThuoc(kichThuocService.getKichThuocById(sanPhamChiTiet.getKichThuoc().getMaKichThuoc()).orElseThrow());
        old.setMauSac(mauSacService.findById(sanPhamChiTiet.getMauSac().getMaMauSac()).orElseThrow());
        old.setGiaBan(sanPhamChiTiet.getGiaBan());
        old.setGiaNhap(sanPhamChiTiet.getGiaNhap());
        old.setSoLuongTon(sanPhamChiTiet.getSoLuongTon());
        old.setNgayCapNhat(LocalDate.now());


        sanPhamChiTietService.capNhatTrangThaii(old);

        // Chỉ lưu 1 lần duy nhất
        sanPhamChiTietService.them(old);

        return "redirect:/sanphamct/index";
    }

    private void prepareModel(Model model) {
        model.addAttribute("listspct", sanPhamChiTietService.getall());
        model.addAttribute("listsp", sanPhamService.getAll());
        model.addAttribute("listdmsp", danhMucSanPhamService.getAll());
        model.addAttribute("listms", mauSacService.findAll());
        model.addAttribute("lists", kichThuocService.getAllKichThuoc());
    }

    @GetMapping("/locsize")
    public String locsize(@RequestParam(value = "size", required = false) String size, Model model) {
        if (size == null || size.trim().isEmpty()) {
            return "redirect:/sanphamct/index";
        }

        List<SanPhamChiTiet> listspct = sanPhamChiTietService.getBySize(size);
        model.addAttribute("listspct", listspct);

        model.addAttribute("selectedSize", size);

        List<SanPham> Listsp = sanPhamService.getAll();
        List<DanhMucSanPham> listdmsp = danhMucSanPhamService.getAll();
        model.addAttribute("listsp", Listsp);
        model.addAttribute("listdmsp", listdmsp);
        model.addAttribute("listms", mauSacService.findAll());
        model.addAttribute("lists", kichThuocService.getAllKichThuoc());
        model.addAttribute("sanphamct", new SanPhamChiTiet());
        return "sanphamct/index";
    }

    @GetMapping("/locmsac")
    public String locmsac(@RequestParam(value = "msac", required = false) String msac, Model model) {
        if (msac == null || msac.trim().isEmpty()) {
            return "redirect:/sanphamct/index";
        }

        List<SanPhamChiTiet> listspct = sanPhamChiTietService.getByMauSac(msac);
        model.addAttribute("listspct", listspct);

        model.addAttribute("selectedMauSac", msac);

        List<SanPham> Listsp = sanPhamService.getAll();
        List<DanhMucSanPham> listdmsp = danhMucSanPhamService.getAll();
        model.addAttribute("listsp", Listsp);
        model.addAttribute("listdmsp", listdmsp);
        model.addAttribute("listms", mauSacService.findAll());
        model.addAttribute("lists", kichThuocService.getAllKichThuoc());
        model.addAttribute("sanphamct", new SanPhamChiTiet());
        return "sanphamct/index";
    }

    @GetMapping("/loctt")
    public String loctt(@RequestParam(value = "tt", required = false) String tt, Model model) {
        if (tt == null || tt.trim().isEmpty()) {
            return "redirect:/sanphamct/index";
        }

        List<SanPhamChiTiet> listspct = sanPhamChiTietService.getByTT(tt);
        model.addAttribute("listspct", listspct);

        model.addAttribute("selectedStatus", tt);

        List<SanPham> Listsp = sanPhamService.getAll();
        List<DanhMucSanPham> listdmsp = danhMucSanPhamService.getAll();
        model.addAttribute("listsp", Listsp);
        model.addAttribute("listdmsp", listdmsp);
        model.addAttribute("listms", mauSacService.findAll());
        model.addAttribute("lists", kichThuocService.getAllKichThuoc());
        model.addAttribute("sanphamct", new SanPhamChiTiet());
        return "sanphamct/index";
    }

    @GetMapping("/locgia")
    public String locgia(@RequestParam("gia") BigDecimal gia, @RequestParam("gia2") BigDecimal gia2 , Model model) {
        if (gia == null)
            gia = BigDecimal.ZERO;
        if (gia2 == null)
            gia2 = BigDecimal.ZERO;
        List<SanPhamChiTiet> listspct = sanPhamChiTietService.getBygia(gia,gia2);
        model.addAttribute("listspct", listspct);
        List<SanPham> Listsp = sanPhamService.getAll();
        List<DanhMucSanPham> listdmsp = danhMucSanPhamService.getAll();
        model.addAttribute("listsp", Listsp);
        model.addAttribute("listdmsp", listdmsp);
        model.addAttribute("listms", mauSacService.findAll());
        model.addAttribute("lists", kichThuocService.getAllKichThuoc());
        model.addAttribute("sanphamct", new SanPhamChiTiet());
        return "sanphamct/index";
    }

}

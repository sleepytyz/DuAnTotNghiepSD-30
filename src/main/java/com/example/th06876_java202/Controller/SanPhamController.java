package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.*;
import com.example.th06876_java202.Repository.TaiKhoanRepository;
import com.example.th06876_java202.Service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.awt.print.Pageable;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/sanpham")
public class SanPhamController {

    @Autowired
    private ThuongHieuService thuongHieuService;

    @Autowired
    private KieuGiayService kieuGiayService;

    @Autowired
    private MauSacService mauSacService;

    @Autowired
    private KichThuocService kichThuocService;

    private final DanhMucSanPhamService danhMucSanPhamService;
    private final SanPhamService sanPhamService;
    private final ChatLieuService chatLieuService;
    private final SanPhamChiTietService sanPhamChiTietService;

    public SanPhamController(DanhMucSanPhamService danhMucSanPhamService, SanPhamService sanPhamService, ChatLieuService chatLieuService,
                             SanPhamChiTietService sanPhamChiTietService) {
        this.danhMucSanPhamService = danhMucSanPhamService;
        this.sanPhamService = sanPhamService;
        this.chatLieuService = chatLieuService;
        this.sanPhamChiTietService = sanPhamChiTietService;
    }

    @GetMapping("/index")
    public String index(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(required = false) Integer maDanhMuc,
                        @RequestParam(required = false) Boolean tt,
                        @RequestParam(required = false) Integer maTH,
                        @RequestParam(required = false) Integer maKG,
                        @RequestParam(required = false) String t,
                        Model model) {

        Page<SanPham> pageSanPham = sanPhamService.searchSanPham(maDanhMuc, tt, maTH, maKG, t, PageRequest.of(page, 5));
        model.addAttribute("listsp", pageSanPham.getContent());
        model.addAttribute("listsp", pageSanPham.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageSanPham.getTotalPages());
        model.addAttribute("totalItems", pageSanPham.getTotalElements());

        model.addAttribute("listctsp", sanPhamChiTietService.gi1a());
        model.addAttribute("listdmsp", danhMucSanPhamService.getAll());
        model.addAttribute("listcl", chatLieuService.findAll());
        model.addAttribute("listkg", kieuGiayService.findAll());
        model.addAttribute("listth", thuongHieuService.findAll());

        return "sanpham/index";
    }

    @GetMapping("/add-view")
    public String addView(Model model) {
        model.addAttribute("activeMenu", "sanpham");

        SanPhamDTO formObject = new SanPhamDTO();
        model.addAttribute("form", formObject);

        model.addAttribute("listdmsp", danhMucSanPhamService.getAll());
        model.addAttribute("listcl", chatLieuService.findAll());
        model.addAttribute("listth", thuongHieuService.findAll());
        model.addAttribute("listkg", kieuGiayService.findAll());
        model.addAttribute("listmausac", mauSacService.findAll());
        model.addAttribute("listkichthuoc", kichThuocService.getAllKichThuoc());

        return "sanpham/add";
    }


    @PostMapping("/api/upload-anh")
    @ResponseBody
    public String uploadAnh(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = FileUploadUtil.saveFile(file);
            return fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }

    @GetMapping("/api/check-trung")
    @ResponseBody
    public ResponseEntity<Boolean> checkTrung(@RequestParam("ten") String ten) {
        boolean exists = sanPhamService.isTenSanPhamDuplicate(ten);
        return ResponseEntity.ok(exists);
    }

    @PostMapping(value = "/api/save-all", consumes = "application/json")
    @ResponseBody
    public ResponseEntity<?> saveAll(@RequestBody SanPhamWrapperDTO wrapperDto) {

        String tenMoi = wrapperDto.getSanPham().getTenSanPham();
        if (sanPhamService.isTenSanPhamDuplicate(tenMoi)) {
            return ResponseEntity.badRequest().body("Tên sản phẩm đã tồn tại trong hệ thống!");
        }
        try {
            SanPhamDTO spDto = wrapperDto.getSanPham();

            SanPham sp = new SanPham();
            sp.setTenSanPham(spDto.getTenSanPham());
            sp.setMoTa(spDto.getMoTa());

            sp.setDanhMucSanPham(danhMucSanPhamService.findById(spDto.getMaDanhMuc()).orElse(null));
            sp.setThuongHieu(thuongHieuService.findById(spDto.getMaThuongHieu()).orElse(null));
            sp.setKieuGiay(kieuGiayService.findById(spDto.getMaKieuGiay()).orElse(null));
            sp.setChatLieu(chatLieuService.findById(spDto.getMaChatLieu()).orElse(null));

            sp.setTrangThai(true);
            sp.setNgayTao(LocalDate.now());
            sanPhamService.save(sp);

            for (SanPhamChiTietDTO ctDto : wrapperDto.getChiTietList()) {
                SanPhamChiTiet ct = new SanPhamChiTiet();
                ct.setSanPham(sp);
                ct.setGiaNhap(ctDto.getGiaNhap());
                ct.setGiaBan(ctDto.getGiaBan());
                ct.setSoLuongTon(ctDto.getSoLuongTon());
                ct.setDuongDanAnh(ctDto.getDuongDanAnh());

                ct.setMauSac(mauSacService.findById(ctDto.getMaMauSac()).orElse(null));
                ct.setKichThuoc(kichThuocService.getKichThuocById(ctDto.getMaKichThuoc()).orElse(null));
                sanPhamChiTietService.capNhatTrangThaii(ct);
                sanPhamChiTietService.them(ct);
            }
            return ResponseEntity.ok("Thêm thành công");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") int id,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "source", defaultValue = "index") String source,
                       Model model) {

        SanPham sp = sanPhamService.findById(id).orElseThrow();
        model.addAttribute("sanpham", sp);

        model.addAttribute("listdmsp", danhMucSanPhamService.getAll());
        model.addAttribute("listcl", chatLieuService.findAll());
        model.addAttribute("listth", thuongHieuService.findAll());
        model.addAttribute("listkg", kieuGiayService.findAll());

        model.addAttribute("showModal", true);
        model.addAttribute("isEdit", true);
        model.addAttribute("source", source);
        if ("detail".equals(source)) {
            model.addAttribute("listBienThe", sanPhamChiTietService.getallsp(id));
            return "sanpham/detail";
        }

        Page<SanPham> pageSanPham = sanPhamService.getallpage(PageRequest.of(page, 5));
        model.addAttribute("listsp", pageSanPham.getContent());
        return "sanpham/index";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") int id,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(required = false) Integer maDanhMuc,
                         @RequestParam(required = false) Boolean tt,
                         @RequestParam(required = false) Integer maTH,
                         @RequestParam(required = false) Integer maKG,
                         @RequestParam(required = false) String t,
                         Model model) {


        SanPham sp = sanPhamService.findById(id).orElseThrow();
        model.addAttribute("sanpham", sp);
        model.addAttribute("listBienThe", sanPhamChiTietService.getallsp(id));
        model.addAttribute("sanphamct", new SanPhamChiTiet());
        model.addAttribute("listsp", sanPhamService.getAll());
        model.addAttribute("listms", mauSacService.findAll());
        model.addAttribute("lists", kichThuocService.getall());
        model.addAttribute("currentPage", page);
        model.addAttribute("maDanhMuc", maDanhMuc);
        model.addAttribute("tt", tt);
        model.addAttribute("maTH", maTH);
        model.addAttribute("maKG", maKG);
        model.addAttribute("t", t);

        return "sanpham/detail";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute SanPham sanpham,
                         @RequestParam(required = false) Integer maDanhMuc,
                         @RequestParam(required = false) Integer maThuongHieu,
                         @RequestParam(required = false) Integer maChatLieu,
                         @RequestParam(required = false) Integer maKieuGiay,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(required = false) String source,
                         RedirectAttributes redirectAttributes) {

        SanPham spOld = sanPhamService.findById(sanpham.getMaSanPham())
                .orElseThrow();

        spOld.setTenSanPham(sanpham.getTenSanPham());
        spOld.setMoTa(sanpham.getMoTa());
        spOld.setNgayCapNhat(LocalDate.now());

        if (maDanhMuc != null) {
            spOld.setDanhMucSanPham(
                    danhMucSanPhamService.findById(maDanhMuc).orElse(null)
            );
        }

        if (maThuongHieu != null) {
            spOld.setThuongHieu(
                    thuongHieuService.findById(maThuongHieu).orElse(null)
            );
        }

        if (maChatLieu != null) {
            spOld.setChatLieu(
                    chatLieuService.findById(maChatLieu).orElse(null)
            );
        }

        if (maKieuGiay != null) {
            spOld.setKieuGiay(
                    kieuGiayService.findById(maKieuGiay).orElse(null)
            );
        }

        sanPhamService.save(spOld);

        redirectAttributes.addFlashAttribute("successMess",
                "Cập nhật sản phẩm thành công!");

        if ("detail".equals(source)) {
            return "redirect:/sanpham/detail/" + sanpham.getMaSanPham();
        }

        return "redirect:/sanpham/index?page=" + page;
    }

    @GetMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable("id") int id,
                               @RequestParam("status") boolean status, // Nhận true/false từ giao diện
                               RedirectAttributes redirectAttributes) {

        sanPhamService.updateTrangThai(id, status);

        if (!status) {
            sanPhamChiTietService.suaSanPham2(id);
            redirectAttributes.addFlashAttribute("successMess", "Đã tắt sản phẩm!");
        } else {
            sanPhamChiTietService.suaSanPham3(id);
            redirectAttributes.addFlashAttribute("successMess", "Đã bật sản phẩm!");
        }

        return "redirect:/sanpham/index";
    }



}
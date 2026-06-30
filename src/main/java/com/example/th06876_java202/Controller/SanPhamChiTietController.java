package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.*;
import com.example.th06876_java202.Entity.*;
import com.example.th06876_java202.Service.*;
import com.example.th06876_java202.Service.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/sanphamct")
public class SanPhamChiTietController {

    private final SanPhamChiTietService sanPhamChiTietService;
    private final DanhMucSanPhamService danhMucSanPhamService;
    private final SanPhamService sanPhamService;
    private final MauSacService mauSacService;
    private final KichThuocService kichThuocService;

    public SanPhamChiTietController( SanPhamChiTietService sanPhamChiTietService, DanhMucSanPhamService danhMucSanPhamService,
                                     SanPhamService sanPhamService,
                                     MauSacService mauSacService, KichThuocService kichThuocService) {
        this.sanPhamChiTietService = sanPhamChiTietService;
        this.danhMucSanPhamService = danhMucSanPhamService;
        this.sanPhamService = sanPhamService;
        this.mauSacService = mauSacService;
        this.kichThuocService = kichThuocService;
    }

    @GetMapping("/index")
    public String index(Model model,
                        @PageableDefault(size = 5, sort = "maSanPhamChiTiet", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<SanPhamChiTiet> page = sanPhamChiTietService.getall(pageable);

        model.addAttribute("listspct", page.getContent());

        setupPageModel(model, page, null, null);

        Double maxGia = sanPhamChiTietService.gia();
        model.addAttribute("maxGiaBan", maxGia != null ? maxGia : 1000000000);

        return "sanphamct/index";
    }

    @GetMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable("id") int id,
                               @RequestParam("status") String status,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               RedirectAttributes redirectAttributes) {

        SanPhamChiTiet spct = sanPhamChiTietService.findbyId(id).orElseThrow();

        if (Boolean.FALSE.equals(spct.getSanPham().getTrangThai())) {
            redirectAttributes.addFlashAttribute("errorMess", "Không thể thay đổi trạng thái biến thể khi sản phẩm cha đang ngừng bán!");
            return "redirect:/sanphamct/index?page=" + page;
        }
        if ("Ngừng bán".equals(status)) {
            spct.setTrangThai("Ngừng bán");
        } else {
            spct.setTrangThai("Còn hàng");
            sanPhamChiTietService.capNhatTrangThaii(spct);
        }

        sanPhamChiTietService.them(spct);
        return "redirect:/sanphamct/index?page=" + page;
    }


    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") int id,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       // Nhận thêm các tham số lọc
                       @RequestParam(value = "size", required = false) String size,
                       @RequestParam(value = "msac", required = false) String msac,
                       @RequestParam(value = "tt", required = false) String tt,
                       Model model) {

        Pageable pageable = PageRequest.of(page, 5, Sort.by("maSanPhamChiTiet").descending());
        Page<SanPhamChiTiet> p = sanPhamChiTietService.getall(pageable);
        model.addAttribute("listspct", p.getContent());
        setupPageModel(model, p, null, null);

        SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietService.findbyId(id).orElse(null);
        model.addAttribute("sanphamct", sanPhamChiTiet);

        model.addAttribute("selectedSize", size);
        model.addAttribute("selectedMauSac", msac);
        model.addAttribute("selectedStatus", tt);

        model.addAttribute("showModal", true);
        model.addAttribute("isEdit", true);
        Double maxGia = sanPhamChiTietService.gia();
        model.addAttribute("maxGiaBan", maxGia != null ? maxGia : 1000000000);
        return "sanphamct/index";
    }

    @PostMapping("/api/toggle-status/{id}")
    @ResponseBody
    public ResponseEntity<String> toggleStatus(@PathVariable int id, @RequestParam boolean active) {

        SanPhamChiTiet spct = sanPhamChiTietService.findbyId(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));


        spct.setTrangThai(active ? "Còn hàng" : "Ngừng bán");

        // 3. Lưu lại
        sanPhamChiTietService.them(spct);

        return ResponseEntity.ok(spct.getTrangThai());
    }

    @PostMapping("/update")
    public String update(@ModelAttribute("sanphamct") @Valid SanPhamChiTiet sanPhamChiTiet,
                         @RequestParam(value = "fileAnh", required = false) MultipartFile file,
                         @RequestParam(value = "page", defaultValue = "0") int page,
                         @RequestParam(value = "source", defaultValue = "index") String source,
                         Errors errors, Model model) {
        if (errors.hasErrors()) {
            errors.getAllErrors().forEach(e -> System.out.println(e.getDefaultMessage()));
            prepareModel(model);
            model.addAttribute("showModal", true);
            model.addAttribute("isEdit", true);
            model.addAttribute("currentPage", page);
            return "sanphamct/index";
        }

        SanPhamChiTiet old = sanPhamChiTietService.findbyId(sanPhamChiTiet.getMaSanPhamChiTiet()).orElseThrow();
        if (file != null && !file.isEmpty()) {
            try {
                if (old.getDuongDanAnh() != null) {
                    java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get("D:/AnhSP/" + old.getDuongDanAnh()));
                }
                String newFileName = FileUploadUtil.saveFile(file);
                old.setDuongDanAnh(newFileName);
            } catch (IOException e) { e.printStackTrace(); }
        }
        old.setSanPham(sanPhamService.findById(sanPhamChiTiet.getSanPham().getMaSanPham()).orElseThrow());
        old.setKichThuoc(kichThuocService.getKichThuocById(sanPhamChiTiet.getKichThuoc().getMaKichThuoc()).orElseThrow());
        old.setMauSac(mauSacService.findById(sanPhamChiTiet.getMauSac().getMaMauSac()).orElseThrow());
        old.setGiaBan(sanPhamChiTiet.getGiaBan());
        old.setGiaNhap(sanPhamChiTiet.getGiaNhap());
        old.setSoLuongTon(sanPhamChiTiet.getSoLuongTon());
        old.setNgayCapNhat(LocalDate.now());
        Double maxGia = sanPhamChiTietService.gia();
        model.addAttribute("maxGiaBan", maxGia != null ? maxGia : 1000000000);
        sanPhamChiTietService.capNhatTrangThaii(old);
        sanPhamChiTietService.them(old);


        if ("detail".equals(source)) {
            return "redirect:/sanpham/detail/" + old.getSanPham().getMaSanPham();
        } else {
            // Redirect về trang index quản lý biến thể
            return "redirect:/sanphamct/index?page=" + page;
        }
    }

    private void prepareModel(Model model) {
        model.addAttribute("listsp", sanPhamService.getAll());
        model.addAttribute("listdmsp", danhMucSanPhamService.getAll());
        model.addAttribute("listms", mauSacService.findAll());
        model.addAttribute("lists", kichThuocService.getall());
    }

    @GetMapping("/locsize")
    public String locsize(@RequestParam(value = "size", required = false) String size,
                          @PageableDefault(size = 5) Pageable pageable, Model model) {
        if (size == null || size.trim().isEmpty()) return "redirect:/sanphamct/index";

        Page<SanPhamChiTiet> page = sanPhamChiTietService.getBySize(size, getSortedPageable(pageable));
        model.addAttribute("listspct", page.getContent());
        setupPageModel(model, page, "selectedSize", size);
        Double maxGia = sanPhamChiTietService.gia();
        model.addAttribute("maxGiaBan", maxGia != null ? maxGia : 1000000000);
        return "sanphamct/index";
    }

    @GetMapping("/locmsac")
    public String locmsac(@RequestParam(value = "msac", required = false) String msac,
                          @PageableDefault(size = 5) Pageable pageable, Model model) {
        if (msac == null || msac.trim().isEmpty()) return "redirect:/sanphamct/index";

        Page<SanPhamChiTiet> page = sanPhamChiTietService.getByMauSac(msac, getSortedPageable(pageable));
        model.addAttribute("listspct", page.getContent());
        setupPageModel(model, page, "selectedMauSac", msac);
        Double maxGia = sanPhamChiTietService.gia();
        model.addAttribute("maxGiaBan", maxGia != null ? maxGia : 1000000000);
        return "sanphamct/index";
    }

    @GetMapping("/loctt")
    public String loctt(@RequestParam(value = "tt", required = false) String tt,
                        @PageableDefault(size = 5) Pageable pageable, Model model) {
        if (tt == null || tt.trim().isEmpty()) return "redirect:/sanphamct/index";

        Page<SanPhamChiTiet> page = sanPhamChiTietService.getByTT(tt, getSortedPageable(pageable));
        model.addAttribute("listspct", page.getContent());
        setupPageModel(model, page, "selectedStatus", tt);
        Double maxGia = sanPhamChiTietService.gia();
        model.addAttribute("maxGiaBan", maxGia != null ? maxGia : 1000000000);
        return "sanphamct/index";
    }

    @GetMapping("/locgia")
    public String locgia(@RequestParam(value = "gia", required = false) BigDecimal gia,
                         @RequestParam(value = "gia2", required = false) BigDecimal gia2,
                         @PageableDefault(size = 5) Pageable pageable, Model model) {
        BigDecimal min = (gia == null) ? BigDecimal.ZERO : gia;
        BigDecimal max = (gia2 == null) ? BigDecimal.valueOf(1000000000) : gia2;

        Page<SanPhamChiTiet> page = sanPhamChiTietService.getBygia(min, max, getSortedPageable(pageable));
        model.addAttribute("listspct", page.getContent());
        setupPageModel(model, page, null, null);
        model.addAttribute("selectedGia", min);
        model.addAttribute("selectedGia2", max);
        Double maxGia = sanPhamChiTietService.gia();
        model.addAttribute("maxGiaBan", maxGia != null ? maxGia : 1000000000);
        return "sanphamct/index";
    }

    private void setupPageModel(Model model, Page<SanPhamChiTiet> page, String attrName, Object attrValue) {
        model.addAttribute("currentPage", page.getNumber());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        if (attrName != null) model.addAttribute(attrName, attrValue);
        prepareModel(model);
        model.addAttribute("sanphamct", new SanPhamChiTiet());
    }

    private Pageable getSortedPageable(Pageable pageable) {
        return org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                org.springframework.data.domain.Sort.by("maSanPhamChiTiet").descending()
        );
    }

}

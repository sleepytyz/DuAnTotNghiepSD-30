package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.*;
import com.example.th06876_java202.Entity.*;
import com.example.th06876_java202.Repository.SanPhamChiTietRepository;
import com.example.th06876_java202.Service.*;
import com.example.th06876_java202.Service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/sanphamct")
public class SanPhamChiTietController {

    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private ExcelExportService excelExportService;

    @Autowired
    private ChiTietDotGiamGiaService chiTietDotGiamGiaService;

    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;


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

    private BigDecimal getMaxDiscountForVariant(String maSanPhamChiTiet) {
        try {
            // Lấy danh sách đợt giảm giá đang hoạt động có chứa biến thể này
            List<ChiTietDotGiamGia> list = chiTietDotGiamGiaService.findBySanPhamChiTiet_MaSanPhamChiTiet(maSanPhamChiTiet);

            if (list == null || list.isEmpty()) {
                return BigDecimal.ZERO;
            }

            BigDecimal maxDiscount = BigDecimal.ZERO;
            LocalDate today = LocalDate.now();

            for (ChiTietDotGiamGia ct : list) {
                DotGiamGia dgg = ct.getDotGiamGia();
                if (dgg == null) continue;

                // Chỉ tính các đợt giảm giá đang hoạt động
                if (!"Hoạt động".equals(dgg.getTrangThai())) continue;

                // Kiểm tra ngày hiệu lực
                if (dgg.getNgayBatDau() != null && dgg.getNgayKetThuc() != null) {
                    if (today.isBefore(dgg.getNgayBatDau()) || today.isAfter(dgg.getNgayKetThuc())) {
                        continue;
                    }
                }

                // Kiểm tra biến thể có trong đợt giảm giá không
                if (ct.getSanPhamChiTiet() != null &&
                        maSanPhamChiTiet.equals(ct.getSanPhamChiTiet().getMaSanPhamChiTiet())) {

                    BigDecimal giaTriGiam = dgg.getGiaTriGiam() != null ?
                            dgg.getGiaTriGiam() : BigDecimal.ZERO;

                    if (giaTriGiam.compareTo(maxDiscount) > 0) {
                        maxDiscount = giaTriGiam;
                    }
                }
            }
            return maxDiscount;
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy giảm giá cho biến thể " + maSanPhamChiTiet + ": " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    // ===== TÍNH GIÁ SAU GIẢM =====
    private BigDecimal calculatePriceAfterDiscount(BigDecimal giaBan, BigDecimal discountPercent) {
        if (giaBan == null || discountPercent == null || discountPercent.compareTo(BigDecimal.ZERO) == 0) {
            return giaBan;
        }
        BigDecimal discountAmount = giaBan.multiply(discountPercent).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        return giaBan.subtract(discountAmount);
    }


    @GetMapping("/index")
    public String index(Model model,
                        @PageableDefault(size = 5, sort = "maSanPhamChiTiet", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<SanPhamChiTiet> page = sanPhamChiTietService.getall(pageable);

        // Lấy danh sách biến thể và thêm thông tin giảm giá
        List<SanPhamChiTietDTOWithDiscount> listWithDiscount = new ArrayList<>();
        for (SanPhamChiTiet spct : page.getContent()) {
            SanPhamChiTietDTOWithDiscount dto = new SanPhamChiTietDTOWithDiscount();
            dto.setSanPhamChiTiet(spct);

            BigDecimal maxDiscount = getMaxDiscountForVariant(spct.getMaSanPhamChiTiet());
            dto.setMaxDiscount(maxDiscount);
            dto.setPriceAfterDiscount(calculatePriceAfterDiscount(spct.getGiaBan(), maxDiscount));
            dto.setHasDiscount(maxDiscount.compareTo(BigDecimal.ZERO) > 0);

            listWithDiscount.add(dto);
        }

        model.addAttribute("listspct", page.getContent());
        model.addAttribute("listWithDiscount", listWithDiscount);

        setupPageModel(model, page, null, null);

        Double maxGia = sanPhamChiTietService.gia();
        model.addAttribute("maxGiaBan", maxGia != null ? maxGia : 1000000000);

        return "sanphamct/index";
    }
    @GetMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable("id") String id,
                               @RequestParam("status") String status,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(required = false) String size,
                               @RequestParam(required = false) String msac,
                               @RequestParam(required = false) String tt,
                               @RequestParam(required = false) String gia,
                               @RequestParam(required = false) String gia2,
                               RedirectAttributes redirectAttributes) {

        SanPhamChiTiet spct = sanPhamChiTietService.findbyId(id).orElseThrow();

        if (Boolean.FALSE.equals(spct.getSanPham().getTrangThai())) {
            redirectAttributes.addFlashAttribute("errorMess", "Không thể thay đổi trạng thái biến thể khi sản phẩm cha đang ngừng bán!");
            return "redirect:/sanphamct/index?page=" + page;
        }

        String message = "";
        if ("Ngừng bán".equals(status)) {
            spct.setTrangThai("Ngừng bán");
            message = "Đã tắt sản phẩm!";
        } else {
            spct.setTrangThai("Còn hàng");
            sanPhamChiTietService.capNhatTrangThaii(spct);
            message = "Đã bật sản phẩm!";
        }

        sanPhamChiTietService.them(spct);

        redirectAttributes.addFlashAttribute("successMess", message);


        String redirectUrl = "redirect:/sanphamct/index?page=" + page;
        if (size != null && !size.isEmpty()) redirectUrl += "&size=" + size;
        if (msac != null && !msac.isEmpty()) redirectUrl += "&msac=" + msac;
        if (tt != null && !tt.isEmpty()) redirectUrl += "&tt=" + tt;
        if (gia != null && !gia.isEmpty()) redirectUrl += "&gia=" + gia;
        if (gia2 != null && !gia2.isEmpty()) redirectUrl += "&gia2=" + gia2;

        return redirectUrl;
    }


    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") String id,
                       @RequestParam(value = "page", defaultValue = "0") int page,
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
    public ResponseEntity<String> toggleStatus(@PathVariable String id, @RequestParam boolean active) {
        try {
            SanPhamChiTiet spct = sanPhamChiTietService.findbyId(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            spct.setTrangThai(active ? "Còn hàng" : "Ngừng bán");
            sanPhamChiTietService.capNhatTrangThaii(spct);
            sanPhamChiTietService.them(spct);

            return ResponseEntity.ok(spct.getTrangThai());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
    @PostMapping("/update-ajax")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateAjax(
            @ModelAttribute SanPhamChiTiet sanPhamChiTiet,
            @RequestParam(value = "fileAnh", required = false) MultipartFile file,
            @RequestParam(value = "source", defaultValue = "detail") String source) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Lấy bản ghi cũ
            SanPhamChiTiet old = sanPhamChiTietService.findbyId(sanPhamChiTiet.getMaSanPhamChiTiet())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể!"));

            // Xử lý upload ảnh mới
            if (file != null && !file.isEmpty()) {
                if (old.getDuongDanAnh() != null && !old.getDuongDanAnh().isEmpty()) {
                    Path oldFilePath = Paths.get("D:\\AnhSP\\" + old.getDuongDanAnh());
                    try {
                        Files.deleteIfExists(oldFilePath);
                    } catch (IOException e) {
                        System.err.println("Không thể xóa file cũ: " + e.getMessage());
                    }
                }
                String newFileName = FileUploadUtil.saveFile(file);
                old.setDuongDanAnh(newFileName);
            }

            // Cập nhật thông tin
            old.setSanPham(sanPhamService.findById(sanPhamChiTiet.getSanPham().getMaSanPham())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!")));
            old.setKichThuoc(kichThuocService.getKichThuocById(sanPhamChiTiet.getKichThuoc().getMaKichThuoc())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy kích thước!")));
            old.setMauSac(mauSacService.findById(sanPhamChiTiet.getMauSac().getMaMauSac())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy màu sắc!")));
            old.setGiaBan(sanPhamChiTiet.getGiaBan());
            old.setSoLuongTon(sanPhamChiTiet.getSoLuongTon());


            // Cập nhật trạng thái
            sanPhamChiTietService.capNhatTrangThaii(old);

            // Lưu
            SanPhamChiTiet updated = sanPhamChiTietService.them(old);

            // Trả về JSON với đầy đủ thông tin
            response.put("success", true);
            response.put("message", "Cập nhật biến thể thành công!");
            response.put("maBienThe", updated.getMaSanPhamChiTiet());
            response.put("maKichThuoc", updated.getKichThuoc() != null ? updated.getKichThuoc().getMaKichThuoc() : "");
            response.put("maMauSac", updated.getMauSac() != null ? updated.getMauSac().getMaMauSac() : "");
            response.put("giaBan", updated.getGiaBan());
            response.put("soLuongTon", updated.getSoLuongTon());
            response.put("trangThai", updated.getTrangThai());
            response.put("duongDanAnh", updated.getDuongDanAnh() != null ? updated.getDuongDanAnh() : "");
            response.put("tenKichThuoc", updated.getKichThuoc() != null ? updated.getKichThuoc().getTenKichThuoc() : "");
            response.put("tenMauSac", updated.getMauSac() != null ? updated.getMauSac().getTenMauSac() : "");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/update")
    public String update(@ModelAttribute("sanphamct") @Valid SanPhamChiTiet sanPhamChiTiet,
                         @RequestParam(value = "fileAnh", required = false) MultipartFile file,
                         @RequestParam(value = "page", defaultValue = "0") int page,
                         @RequestParam(value = "source", defaultValue = "index") String source,
                         @RequestParam(required = false) String size,
                         @RequestParam(required = false) String msac,
                         @RequestParam(required = false) String tt,
                         @RequestParam(required = false) String gia,
                         @RequestParam(required = false) String gia2,
                         @RequestParam(required = false) String tonKho, // THÊM NÀY
                         Errors errors,
                         RedirectAttributes redirectAttributes,
                         Model model) {

        if (errors.hasErrors()) {
            errors.getAllErrors().forEach(e -> System.out.println(e.getDefaultMessage()));
            prepareModel(model);
            model.addAttribute("showModal", true);
            model.addAttribute("isEdit", true);
            model.addAttribute("currentPage", page);
            return "sanphamct/index";
        }

        try {
            SanPhamChiTiet old = sanPhamChiTietService.findbyId(sanPhamChiTiet.getMaSanPhamChiTiet()).orElseThrow();

            // Xử lý file ảnh
            if (file != null && !file.isEmpty()) {
                try {
                    if (old.getDuongDanAnh() != null && !old.getDuongDanAnh().isEmpty()) {
                        Path oldFilePath = Paths.get("D:\\AnhSP\\" + old.getDuongDanAnh());
                        try {
                            Files.deleteIfExists(oldFilePath);
                            System.out.println("Đã xóa file cũ: " + old.getDuongDanAnh());
                        } catch (IOException e) {
                            System.err.println("Không thể xóa file cũ: " + e.getMessage());
                        }
                    }
                    String newFileName = FileUploadUtil.saveFile(file);
                    old.setDuongDanAnh(newFileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Cập nhật thông tin
            old.setSanPham(sanPhamService.findById(sanPhamChiTiet.getSanPham().getMaSanPham()).orElseThrow());
            old.setKichThuoc(kichThuocService.getKichThuocById(sanPhamChiTiet.getKichThuoc().getMaKichThuoc()).orElseThrow());
            old.setMauSac(mauSacService.findById(sanPhamChiTiet.getMauSac().getMaMauSac()).orElseThrow());
            old.setGiaBan(sanPhamChiTiet.getGiaBan());
            old.setSoLuongTon(sanPhamChiTiet.getSoLuongTon());


            sanPhamChiTietService.capNhatTrangThaii(old);
            sanPhamChiTietService.them(old);

            if ("detail".equals(source)) {
                return "redirect:/sanpham/detail/" + old.getSanPham().getMaSanPham();
            } else {
                // Xây dựng URL redirect với tất cả tham số filter
                String redirectUrl = "redirect:/sanphamct/index?page=" + page;
                if (size != null && !size.isEmpty()) redirectUrl += "&size=" + size;
                if (msac != null && !msac.isEmpty()) redirectUrl += "&msac=" + msac;
                if (tt != null && !tt.isEmpty()) redirectUrl += "&tt=" + tt;
                if (gia != null && !gia.isEmpty()) redirectUrl += "&gia=" + gia;
                if (gia2 != null && !gia2.isEmpty()) redirectUrl += "&gia2=" + gia2;
                if (tonKho != null && !tonKho.isEmpty()) redirectUrl += "&tonKho=" + tonKho;

                return redirectUrl;
            }

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMess", "Lỗi: " + e.getMessage());
            return "redirect:/sanphamct/index?page=" + page;
        }
    }

    @GetMapping("/loctonkho")
    public String loctonkho(@RequestParam(value = "tonKho", required = false) String tonKho,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(required = false) String size,
                            @RequestParam(required = false) String msac,
                            @RequestParam(required = false) String tt,
                            @RequestParam(required = false) String gia,
                            @RequestParam(required = false) String gia2,
                            Model model) {

        if (tonKho == null || tonKho.trim().isEmpty()) {
            return "redirect:/sanphamct/index";
        }

        Pageable pageable = PageRequest.of(page, 5, Sort.by("maSanPhamChiTiet").descending());
        Page<SanPhamChiTiet> pageResult = sanPhamChiTietService.getByTonKho(tonKho, pageable);

        List<SanPhamChiTietDTOWithDiscount> listWithDiscount = new ArrayList<>();
        for (SanPhamChiTiet spct : pageResult.getContent()) {
            SanPhamChiTietDTOWithDiscount dto = new SanPhamChiTietDTOWithDiscount();
            dto.setSanPhamChiTiet(spct);
            BigDecimal maxDiscount = getMaxDiscountForVariant(spct.getMaSanPhamChiTiet());
            dto.setMaxDiscount(maxDiscount);
            dto.setPriceAfterDiscount(calculatePriceAfterDiscount(spct.getGiaBan(), maxDiscount));
            dto.setHasDiscount(maxDiscount.compareTo(BigDecimal.ZERO) > 0);
            listWithDiscount.add(dto);
        }

        model.addAttribute("listspct", pageResult.getContent());
        model.addAttribute("listWithDiscount", listWithDiscount);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("totalItems", pageResult.getTotalElements());
        model.addAttribute("selectedTonKho", tonKho);
        model.addAttribute("selectedSize", size);
        model.addAttribute("selectedMauSac", msac);
        model.addAttribute("selectedStatus", tt);
        model.addAttribute("selectedGia", gia);
        model.addAttribute("selectedGia2", gia2);

        Double maxGia = sanPhamChiTietService.gia();
        model.addAttribute("maxGiaBan", maxGia != null ? maxGia : 1000000000);

        prepareModel(model);
        model.addAttribute("sanphamct", new SanPhamChiTiet());

        return "sanphamct/index";
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

        List<SanPhamChiTietDTOWithDiscount> listWithDiscount = new ArrayList<>();
        for (SanPhamChiTiet spct : page.getContent()) {
            SanPhamChiTietDTOWithDiscount dto = new SanPhamChiTietDTOWithDiscount();
            dto.setSanPhamChiTiet(spct);
            BigDecimal maxDiscount = getMaxDiscountForVariant(spct.getMaSanPhamChiTiet());
            dto.setMaxDiscount(maxDiscount);
            dto.setPriceAfterDiscount(calculatePriceAfterDiscount(spct.getGiaBan(), maxDiscount));
            dto.setHasDiscount(maxDiscount.compareTo(BigDecimal.ZERO) > 0);
            listWithDiscount.add(dto);
        }

        model.addAttribute("listspct", page.getContent());
        model.addAttribute("listWithDiscount", listWithDiscount);
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

        List<SanPhamChiTietDTOWithDiscount> listWithDiscount = new ArrayList<>();
        for (SanPhamChiTiet spct : page.getContent()) {
            SanPhamChiTietDTOWithDiscount dto = new SanPhamChiTietDTOWithDiscount();
            dto.setSanPhamChiTiet(spct);
            BigDecimal maxDiscount = getMaxDiscountForVariant(spct.getMaSanPhamChiTiet());
            dto.setMaxDiscount(maxDiscount);
            dto.setPriceAfterDiscount(calculatePriceAfterDiscount(spct.getGiaBan(), maxDiscount));
            dto.setHasDiscount(maxDiscount.compareTo(BigDecimal.ZERO) > 0);
            listWithDiscount.add(dto);
        }

        model.addAttribute("listspct", page.getContent());
        model.addAttribute("listWithDiscount", listWithDiscount);
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

        List<SanPhamChiTietDTOWithDiscount> listWithDiscount = new ArrayList<>();
        for (SanPhamChiTiet spct : page.getContent()) {
            SanPhamChiTietDTOWithDiscount dto = new SanPhamChiTietDTOWithDiscount();
            dto.setSanPhamChiTiet(spct);
            BigDecimal maxDiscount = getMaxDiscountForVariant(spct.getMaSanPhamChiTiet());
            dto.setMaxDiscount(maxDiscount);
            dto.setPriceAfterDiscount(calculatePriceAfterDiscount(spct.getGiaBan(), maxDiscount));
            dto.setHasDiscount(maxDiscount.compareTo(BigDecimal.ZERO) > 0);
            listWithDiscount.add(dto);
        }

        model.addAttribute("listspct", page.getContent());
        model.addAttribute("listWithDiscount", listWithDiscount);
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

        List<SanPhamChiTietDTOWithDiscount> listWithDiscount = new ArrayList<>();
        for (SanPhamChiTiet spct : page.getContent()) {
            SanPhamChiTietDTOWithDiscount dto = new SanPhamChiTietDTOWithDiscount();
            dto.setSanPhamChiTiet(spct);
            BigDecimal maxDiscount = getMaxDiscountForVariant(spct.getMaSanPhamChiTiet());
            dto.setMaxDiscount(maxDiscount);
            dto.setPriceAfterDiscount(calculatePriceAfterDiscount(spct.getGiaBan(), maxDiscount));
            dto.setHasDiscount(maxDiscount.compareTo(BigDecimal.ZERO) > 0);
            listWithDiscount.add(dto);
        }

        model.addAttribute("listspct", page.getContent());
        model.addAttribute("listWithDiscount", listWithDiscount);
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


    @GetMapping("/generate-qr/{maBienThe}")
    public ResponseEntity<?> generateQR(@PathVariable("maBienThe") String maBienThe) {
        try {
            SanPhamChiTiet spct = sanPhamChiTietService.findbyId(maBienThe)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể!"));


            String qrPath = qrCodeService.generateVariantQRCode(maBienThe);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tạo QR Code thành công!");
            response.put("qrPath", qrPath);
            response.put("maBienThe", maBienThe);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/download-qr/{maBienThe}")
    public ResponseEntity<?> downloadQR(@PathVariable("maBienThe") String maBienThe) {
        try {
            // Kiểm tra biến thể tồn tại
            SanPhamChiTiet spct = sanPhamChiTietService.findbyId(maBienThe)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể!"));

            // 📁 KIỂM TRA FILE QR ĐÃ TỒN TẠI CHƯA
            String fileName = "QR_" + maBienThe + ".png";
            Path savePath = Paths.get("D:\\QRSanPham", fileName);

            if (Files.exists(savePath)) {
                // File đã tồn tại, trả về thông báo
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("duplicate", true);
                response.put("message", "Mã QR của biến thể '" + maBienThe + "' đã tồn tại trong thư mục D:\\QRSanPham!");
                response.put("filePath", savePath.toString());
                return ResponseEntity.ok(response);
            }

            // Nội dung QR
            String qrContent = "https://fsshop.com/sanpham/detail/" + maBienThe;

            // Tạo QR Code dưới dạng byte[]
            byte[] qrBytes = qrCodeService.generateQRCodeAsBytes(qrContent);

            // Tạo thư mục nếu chưa tồn tại
            Files.createDirectories(savePath.getParent());

            // Ghi file
            Files.write(savePath, qrBytes);

            // Trả về thông báo thành công
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("duplicate", false);
            response.put("message", "QR Code đã được lưu vào D:\\QRSanPham\\" + fileName);
            response.put("filePath", savePath.toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/download-qr-batch")
    public ResponseEntity<?> downloadQRBatch(@RequestBody List<String> maBienTheList) {
        try {
            List<String> savedFiles = new ArrayList<>();
            List<String> duplicateFiles = new ArrayList<>();
            List<String> notFoundFiles = new ArrayList<>();

            for (String maBienThe : maBienTheList) {
                SanPhamChiTiet spct = sanPhamChiTietService.findbyId(maBienThe)
                        .orElse(null);

                if (spct == null) {
                    notFoundFiles.add(maBienThe);
                    continue;
                }

                String fileName = "QR_" + maBienThe + ".png";
                Path savePath = Paths.get("D:\\QRSanPham", fileName);

                if (Files.exists(savePath)) {
                    // File đã tồn tại
                    duplicateFiles.add(maBienThe);
                    continue;
                }

                // Tạo QR mới
                String qrContent = "https://fsshop.com/sanpham/detail/" + maBienThe;
                byte[] qrBytes = qrCodeService.generateQRCodeAsBytes(qrContent);

                Files.createDirectories(savePath.getParent());
                Files.write(savePath, qrBytes);

                savedFiles.add(maBienThe);
            }

            // Xây dựng thông báo
            String message = "";
            if (!savedFiles.isEmpty()) {
                message += "Đã lưu " + savedFiles.size() + " QR Code mới: " + String.join(", ", savedFiles) + ". ";
            }
            if (!duplicateFiles.isEmpty()) {
                message += "QR Code đã tồn tại cho biến thể: " + String.join(", ", duplicateFiles) + ". ";
            }
            if (!notFoundFiles.isEmpty()) {
                message += " Không tìm thấy biến thể: " + String.join(", ", notFoundFiles) + ". ";
            }

            if (savedFiles.isEmpty() && duplicateFiles.isEmpty() && notFoundFiles.isEmpty()) {
                message = "Không có biến thể nào được chọn!";
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", !savedFiles.isEmpty() || !duplicateFiles.isEmpty());
            response.put("message", message);
            response.put("saved", savedFiles);
            response.put("duplicate", duplicateFiles);
            response.put("notFound", notFoundFiles);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/generate-qr-product/{maSanPham}")
    public ResponseEntity<?> generateQRForProduct(@PathVariable("maSanPham") String maSanPham) {
        try {
            List<SanPhamChiTiet> variants = sanPhamChiTietService.getallsp(maSanPham);
            if (variants.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Sản phẩm không có biến thể nào!");
                return ResponseEntity.badRequest().body(response);
            }

            List<Map<String, String>> qrResults = new ArrayList<>();
            for (SanPhamChiTiet variant : variants) {
                String qrPath = qrCodeService.generateVariantQRCode(variant.getMaSanPhamChiTiet());

                Map<String, String> result = new HashMap<>();
                result.put("maBienThe", variant.getMaSanPhamChiTiet());
                result.put("qrPath", qrPath);
                qrResults.add(result);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tạo QR Code cho " + qrResults.size() + " biến thể thành công!");
            response.put("data", qrResults);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/export-excel")
    public ResponseEntity<InputStreamResource> exportExcel(
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String msac,
            @RequestParam(required = false) String tt,
            @RequestParam(required = false) BigDecimal gia,
            @RequestParam(required = false) BigDecimal gia2,
            @RequestParam(required = false) String tonKho) {

        try {
            System.out.println("========== EXPORT EXCEL ==========");
            System.out.println("size: [" + size + "]");
            System.out.println("msac: [" + msac + "]");
            System.out.println("tt: [" + tt + "]");
            System.out.println("gia: [" + gia + "]");
            System.out.println("gia2: [" + gia2 + "]");
            System.out.println("tonKho: [" + tonKho + "]");

            List<SanPhamChiTiet> list = sanPhamChiTietService.findAllWithFilters(size, msac, tt, gia, gia2, tonKho);

            System.out.println("✅ Số lượng bản ghi tìm thấy: " + (list != null ? list.size() : 0));

            ByteArrayInputStream in = excelExportService.exportSanPhamChiTietToExcel(list);

            if (in == null) {
                System.err.println("❌ InputStream bị null!");
                return ResponseEntity.badRequest().build();
            }

            // 🔥 SỬ DỤNG METHOD readAllBytes Ở ĐÂY
            byte[] excelBytes = readAllBytes(in);

            if (excelBytes == null || excelBytes.length == 0) {
                System.err.println("❌ Dữ liệu Excel rỗng!");
                return ResponseEntity.badRequest().build();
            }

            System.out.println("📊 Dung lượng file: " + excelBytes.length + " bytes");

            String fileName = "Danh_sach_bien_the_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=" + fileName);
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentLength(excelBytes.length);

            // Thêm header để tránh cache
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(new ByteArrayInputStream(excelBytes)));

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Lỗi export Excel: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    private byte[] readAllBytes(ByteArrayInputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = in.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }


    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllProducts() {
        List<SanPhamChiTiet> products = sanPhamChiTietRepository.findAll();

        List<Map<String, Object>> result = products.stream()
                .map(sp -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("maSanPhamChiTiet", sp.getMaSanPhamChiTiet());
                    map.put("tenSanPham", sp.getSanPham().getTenSanPham());
                    map.put("giaBan", sp.getGiaBan());
                    map.put("soLuongTon", sp.getSoLuongTon());
                    map.put("mauSac", sp.getMauSac().getTenMauSac());
                    map.put("kichThuoc", sp.getKichThuoc().getTenKichThuoc());

                    BigDecimal giaSauGiam = sp.getGiaBan();
                    map.put("giaSauGiam", giaSauGiam);

                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

}

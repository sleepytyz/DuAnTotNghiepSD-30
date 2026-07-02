package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.*;
import com.example.th06876_java202.Repository.SanPhamChiTietRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    public SanPhamChiTietController(SanPhamChiTietService sanPhamChiTietService,
                                    DanhMucSanPhamService danhMucSanPhamService,
                                    SanPhamService sanPhamService,
                                    MauSacService mauSacService,
                                    KichThuocService kichThuocService) {
        this.sanPhamChiTietService = sanPhamChiTietService;
        this.danhMucSanPhamService = danhMucSanPhamService;
        this.sanPhamService = sanPhamService;
        this.mauSacService = mauSacService;
        this.kichThuocService = kichThuocService;
    }

    private BigDecimal getMaxDiscountForVariant(String maSanPhamChiTiet) {
        try {
            List<ChiTietDotGiamGia> list = chiTietDotGiamGiaService.findBySanPhamChiTiet_MaSanPhamChiTiet(maSanPhamChiTiet);
            if (list == null || list.isEmpty()) {
                return BigDecimal.ZERO;
            }

            BigDecimal maxDiscount = BigDecimal.ZERO;
            LocalDate today = LocalDate.now();

            for (ChiTietDotGiamGia ct : list) {
                DotGiamGia dgg = ct.getDotGiamGia();
                if (dgg == null) continue;
                if (!"Hoạt động".equals(dgg.getTrangThai())) continue;

                if (dgg.getNgayBatDau() != null && dgg.getNgayKetThuc() != null) {
                    if (today.isBefore(dgg.getNgayBatDau()) || today.isAfter(dgg.getNgayKetThuc())) {
                        continue;
                    }
                }

                if (ct.getSanPhamChiTiet() != null &&
                        maSanPhamChiTiet.equals(ct.getSanPhamChiTiet().getMaSanPhamChiTiet())) {
                    BigDecimal giaTriGiam = dgg.getGiaTriGiam() != null ? dgg.getGiaTriGiam() : BigDecimal.ZERO;
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

    private BigDecimal calculatePriceAfterDiscount(BigDecimal giaBan, BigDecimal discountPercent) {
        if (giaBan == null || discountPercent == null || discountPercent.compareTo(BigDecimal.ZERO) == 0) {
            return giaBan;
        }
        BigDecimal discountAmount = giaBan.multiply(discountPercent).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        return giaBan.subtract(discountAmount);
    }

    // ===== MAIN INDEX - XỬ LÝ TẤT CẢ FILTER =====
    @GetMapping("/index")
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String msac,
            @RequestParam(required = false) String tt,
            @RequestParam(required = false) BigDecimal gia,
            @RequestParam(required = false) BigDecimal gia2,
            @RequestParam(required = false) String tonKho,
            Model model) {

        // Tạo Pageable với sorting
        Pageable pageable = PageRequest.of(page, 5, Sort.by("maSanPhamChiTiet").descending());

        // Lấy dữ liệu với các filter
        Page<SanPhamChiTiet> pageResult = sanPhamChiTietService.findAllWithFilters(
                size, msac, tt, gia, gia2, tonKho, pageable);

        // Lấy danh sách biến thể và thêm thông tin giảm giá
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

        // Đưa dữ liệu vào model
        model.addAttribute("listspct", pageResult.getContent());
        model.addAttribute("listWithDiscount", listWithDiscount);
        model.addAttribute("currentPage", pageResult.getNumber());
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("totalItems", pageResult.getTotalElements());

        // === GIỮ LẠI CÁC THAM SỐ LỌC ===
        model.addAttribute("selectedSize", size);
        model.addAttribute("selectedMauSac", msac);
        model.addAttribute("selectedStatus", tt);
        model.addAttribute("selectedGia", gia);
        model.addAttribute("selectedGia2", gia2);
        model.addAttribute("selectedTonKho", tonKho);

        // Các danh sách cho combobox
        model.addAttribute("listms", mauSacService.findAll());
        model.addAttribute("lists", kichThuocService.getall());

        Double maxGia = sanPhamChiTietService.gia();
        model.addAttribute("maxGiaBan", maxGia != null ? maxGia : 1000000000);

        model.addAttribute("sanphamct", new SanPhamChiTiet());

        return "sanphamct/index";
    }

    // ===== TOGGLE STATUS (FORM SUBMIT) =====
    @GetMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable("id") String id,
                               @RequestParam("status") String status,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(required = false) String size,
                               @RequestParam(required = false) String msac,
                               @RequestParam(required = false) String tt,
                               @RequestParam(required = false) String gia,
                               @RequestParam(required = false) String gia2,
                               @RequestParam(required = false) String tonKho,
                               RedirectAttributes redirectAttributes) {

        SanPhamChiTiet spct = sanPhamChiTietService.findbyId(id).orElseThrow();

        if (Boolean.FALSE.equals(spct.getSanPham().getTrangThai())) {
            redirectAttributes.addFlashAttribute("errorMess", "Không thể thay đổi trạng thái biến thể khi sản phẩm cha đang ngừng bán!");
            return buildRedirectUrl(page, size, msac, tt, gia, gia2, tonKho);
        }

        // Cập nhật trạng thái
        if ("Ngừng bán".equals(status)) {
            spct.setTrangThai("Ngừng bán");
        } else {
            spct.setTrangThai("Còn hàng");
            sanPhamChiTietService.capNhatTrangThaii(spct);
        }

        sanPhamChiTietService.them(spct);
        return buildRedirectUrl(page, size, msac, tt, gia, gia2, tonKho);
    }

    // ===== TOGGLE STATUS (AJAX) =====
    @PostMapping("/api/toggle-status/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleStatusApi(
            @PathVariable String id,
            @RequestParam boolean active) {

        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("=== TOGGLE STATUS API ===");
            System.out.println("ID: " + id);
            System.out.println("Active: " + active);

            Optional<SanPhamChiTiet> spctOpt = sanPhamChiTietService.findbyId(id);
            if (spctOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Không tìm thấy biến thể!");
                return ResponseEntity.badRequest().body(response);
            }

            SanPhamChiTiet spct = spctOpt.get();

            if (spct.getSanPham() != null && !spct.getSanPham().getTrangThai()) {
                response.put("success", false);
                response.put("message", "Sản phẩm cha đang ngừng bán, không thể thay đổi!");
                return ResponseEntity.badRequest().body(response);
            }

            if (active) {
                sanPhamChiTietService.capNhatTrangThaii(spct);
            } else {
                spct.setTrangThai("Ngừng bán");
            }

            SanPhamChiTiet saved = sanPhamChiTietService.them(spct);

            response.put("success", true);
            response.put("message", active ? "Đã bật sản phẩm!" : "Đã tắt sản phẩm!");
            response.put("trangThai", saved.getTrangThai());
            response.put("soLuongTon", saved.getSoLuongTon());
            response.put("maBienThe", saved.getMaSanPhamChiTiet());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") String id,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(required = false) String size,
                       @RequestParam(required = false) String msac,
                       @RequestParam(required = false) String tt,
                       @RequestParam(required = false) String gia,
                       @RequestParam(required = false) String gia2,
                       @RequestParam(required = false) String tonKho,
                       Model model) {

        System.out.println("=== EDIT ===");
        System.out.println("ID: " + id);

        // Lấy danh sách phân trang
        Pageable pageable = PageRequest.of(page, 5, Sort.by("maSanPhamChiTiet").descending());
        Page<SanPhamChiTiet> p = sanPhamChiTietService.getall(pageable);
        model.addAttribute("listspct", p.getContent());
        setupPageModel(model, p, null, null);

        // Lấy biến thể cần chỉnh sửa - QUAN TRỌNG: LẤY ĐẦY ĐỦ THÔNG TIN
        SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietService.findbyIid(id).orElse(null);

        // DEBUG
        if (sanPhamChiTiet != null) {
            System.out.println("SanPhamChiTiet found: " + sanPhamChiTiet.getMaSanPhamChiTiet());
            if (sanPhamChiTiet.getSanPham() != null) {
                System.out.println("SanPham: " + sanPhamChiTiet.getSanPham().getMaSanPham() + " - " + sanPhamChiTiet.getSanPham().getTenSanPham());
            } else {
                System.out.println("SanPham is NULL!");
                // Nếu SanPham bị null, thử load lại từ database
                if (sanPhamChiTiet.getSanPham() == null) {
                    // Lấy SanPham từ MaSanPham nếu có
                    String maSanPham = sanPhamChiTiet.getSanPham() != null ? sanPhamChiTiet.getSanPham().getMaSanPham() : null;
                    if (maSanPham != null) {
                        sanPhamService.findById(maSanPham).ifPresent(sanPhamChiTiet::setSanPham);
                    }
                }
            }
        }

        model.addAttribute("sanphamct", sanPhamChiTiet);

        // === ĐẢM BẢO TRUYỀN LISTSP VÀO MODEL ===
        model.addAttribute("listsp", sanPhamService.getAll());
        model.addAttribute("listms", mauSacService.findAll());
        model.addAttribute("lists", kichThuocService.getall());

        model.addAttribute("selectedSize", size);
        model.addAttribute("selectedMauSac", msac);
        model.addAttribute("selectedStatus", tt);
        model.addAttribute("selectedGia", gia);
        model.addAttribute("selectedGia2", gia2);
        model.addAttribute("selectedTonKho", tonKho);

        model.addAttribute("showModal", true);
        model.addAttribute("isEdit", true);
        Double maxGia = sanPhamChiTietService.gia();
        model.addAttribute("maxGiaBan", maxGia != null ? maxGia : 1000000000);

        return "sanphamct/index";
    }

    @PostMapping("/update-ajax")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateAjax(
            @ModelAttribute SanPhamChiTiet sanPhamChiTiet,
            @RequestParam(value = "fileAnh", required = false) MultipartFile file,
            @RequestParam(value = "source", defaultValue = "detail") String source) {

        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("=== UPDATE AJAX ===");
            System.out.println("MaSanPhamChiTiet: " + sanPhamChiTiet.getMaSanPhamChiTiet());

            // Lấy bản ghi cũ
            SanPhamChiTiet old = sanPhamChiTietService.findbyId(sanPhamChiTiet.getMaSanPhamChiTiet())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể!"));

            // DEBUG: In ra thông tin
            System.out.println("Old SanPham: " + (old.getSanPham() != null ? old.getSanPham().getMaSanPham() : "NULL"));

            // Xử lý upload ảnh
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

            // === QUAN TRỌNG: LẤY MÃ SẢN PHẨM TỪ FORM HOẶC GIỮ NGUYÊN ===
            String maSanPham = null;

            // Cách 1: Lấy từ sanPhamChiTiet (nếu có)
            if (sanPhamChiTiet.getSanPham() != null) {
                maSanPham = sanPhamChiTiet.getSanPham().getMaSanPham();
                System.out.println("MaSanPham from form: " + maSanPham);
            }

            // Cách 2: Nếu không có, giữ nguyên sản phẩm cũ
            if (maSanPham == null || maSanPham.trim().isEmpty()) {
                if (old.getSanPham() != null) {
                    maSanPham = old.getSanPham().getMaSanPham();
                    System.out.println("MaSanPham from old: " + maSanPham);
                } else {
                    throw new RuntimeException("Không tìm thấy sản phẩm cha!");
                }
            }

            // Cập nhật sản phẩm
            Optional<SanPham> sanPhamOpt = sanPhamService.findById(maSanPham);
            if (sanPhamOpt.isPresent()) {
                old.setSanPham(sanPhamOpt.get());
            } else {
                throw new RuntimeException("Không tìm thấy sản phẩm với mã: " + maSanPham);
            }

            // Cập nhật kích thước
            if (sanPhamChiTiet.getKichThuoc() != null && sanPhamChiTiet.getKichThuoc().getMaKichThuoc() != null) {
                Optional<KichThuoc> ktOpt = kichThuocService.getKichThuocById(sanPhamChiTiet.getKichThuoc().getMaKichThuoc());
                ktOpt.ifPresent(old::setKichThuoc);
            }

            // Cập nhật màu sắc
            if (sanPhamChiTiet.getMauSac() != null && sanPhamChiTiet.getMauSac().getMaMauSac() != null) {
                Optional<MauSac> msOpt = mauSacService.findById(sanPhamChiTiet.getMauSac().getMaMauSac());
                msOpt.ifPresent(old::setMauSac);
            }

            // Cập nhật giá và số lượng
            if (sanPhamChiTiet.getGiaBan() != null) {
                old.setGiaBan(sanPhamChiTiet.getGiaBan());
            }
            if (sanPhamChiTiet.getSoLuongTon() != null) {
                old.setSoLuongTon(sanPhamChiTiet.getSoLuongTon());
            }

            // Cập nhật trạng thái
            sanPhamChiTietService.capNhatTrangThaii(old);

            // Lưu
            SanPhamChiTiet updated = sanPhamChiTietService.them(old);

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
                         @RequestParam(required = false) String tonKho,
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

            if (file != null && !file.isEmpty()) {
                try {
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

            // === THÊM SUCCESS MESS VÀO REDIRECT ===
            redirectAttributes.addFlashAttribute("successMess", "Cập nhật biến thể thành công!");

            if ("detail".equals(source)) {
                return "redirect:/sanpham/detail/" + old.getSanPham().getMaSanPham();
            } else {
                return buildRedirectUrl(page, size, msac, tt, gia, gia2, tonKho);
            }

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMess", "Lỗi: " + e.getMessage());
            return buildRedirectUrl(page, size, msac, tt, gia, gia2, tonKho);
        }
    }

    // ===== PHƯƠNG THỨC HỖ TRỢ XÂY DỰNG URL REDIRECT =====
    private String buildRedirectUrl(int page, String size, String msac, String tt, String gia, String gia2, String tonKho) {
        StringBuilder url = new StringBuilder("redirect:/sanphamct/index?page=" + page);
        if (size != null && !size.isEmpty()) url.append("&size=").append(size);
        if (msac != null && !msac.isEmpty()) url.append("&msac=").append(msac);
        if (tt != null && !tt.isEmpty()) url.append("&tt=").append(tt);
        if (gia != null && !gia.isEmpty()) url.append("&gia=").append(gia);
        if (gia2 != null && !gia2.isEmpty()) url.append("&gia2=").append(gia2);
        if (tonKho != null && !tonKho.isEmpty()) url.append("&tonKho=").append(tonKho);
        return url.toString();
    }

    // ===== PREPARE MODEL =====
    private void prepareModel(Model model) {
        model.addAttribute("listsp", sanPhamService.getAll());
        model.addAttribute("listdmsp", danhMucSanPhamService.getAll());
        model.addAttribute("listms", mauSacService.findAll());
        model.addAttribute("lists", kichThuocService.getall());
    }

    private void setupPageModel(Model model, Page<SanPhamChiTiet> page, String attrName, Object attrValue) {
        model.addAttribute("currentPage", page.getNumber());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        if (attrName != null) model.addAttribute(attrName, attrValue);
        prepareModel(model);
        model.addAttribute("sanphamct", new SanPhamChiTiet());
    }

    // ===== QR CODE =====
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
            SanPhamChiTiet spct = sanPhamChiTietService.findbyId(maBienThe)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể!"));

            String fileName = "QR_" + maBienThe + ".png";
            Path savePath = Paths.get("D:\\QRSanPham", fileName);

            if (Files.exists(savePath)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("duplicate", true);
                response.put("message", "Mã QR của biến thể '" + maBienThe + "' đã tồn tại!");
                response.put("filePath", savePath.toString());
                return ResponseEntity.ok(response);
            }

            String qrContent = "https://fsshop.com/sanpham/detail/" + maBienThe;
            byte[] qrBytes = qrCodeService.generateQRCodeAsBytes(qrContent);

            Files.createDirectories(savePath.getParent());
            Files.write(savePath, qrBytes);

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
                SanPhamChiTiet spct = sanPhamChiTietService.findbyId(maBienThe).orElse(null);
                if (spct == null) {
                    notFoundFiles.add(maBienThe);
                    continue;
                }

                String fileName = "QR_" + maBienThe + ".png";
                Path savePath = Paths.get("D:\\QRSanPham", fileName);

                if (Files.exists(savePath)) {
                    duplicateFiles.add(maBienThe);
                    continue;
                }

                String qrContent = "https://fsshop.com/sanpham/detail/" + maBienThe;
                byte[] qrBytes = qrCodeService.generateQRCodeAsBytes(qrContent);

                Files.createDirectories(savePath.getParent());
                Files.write(savePath, qrBytes);
                savedFiles.add(maBienThe);
            }

            String message = "";
            if (!savedFiles.isEmpty()) {
                message += "Đã lưu " + savedFiles.size() + " QR Code mới. ";
            }
            if (!duplicateFiles.isEmpty()) {
                message += "QR Code đã tồn tại cho " + duplicateFiles.size() + " biến thể. ";
            }
            if (!notFoundFiles.isEmpty()) {
                message += "Không tìm thấy " + notFoundFiles.size() + " biến thể. ";
            }
            if (message.isEmpty()) {
                message = "Không có biến thể nào được chọn!";
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", !savedFiles.isEmpty() || !duplicateFiles.isEmpty());
            response.put("message", message.trim());
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

    // ===== EXPORT EXCEL =====
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

            List<SanPhamChiTiet> list = sanPhamChiTietService.findAllWithFilters(size, msac, tt, gia, gia2, tonKho);

            if (list == null || list.isEmpty()) {
                System.out.println("⚠️ Không có dữ liệu để xuất!");
                return ResponseEntity.badRequest().build();
            }

            System.out.println("✅ Số lượng bản ghi tìm thấy: " + list.size());

            ByteArrayInputStream in = excelExportService.exportSanPhamChiTietToExcel(list);

            if (in == null) {
                System.err.println("❌ InputStream bị null!");
                return ResponseEntity.badRequest().build();
            }

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

    // ===== API GET ALL PRODUCTS =====
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
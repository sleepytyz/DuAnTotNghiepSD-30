package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.*;
import com.example.th06876_java202.Repository.SanPhamRepository;
import com.example.th06876_java202.Service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/sanpham")
public class SanPhamController {

    @Autowired
    private ThuongHieuService thuongHieuService;

    @Autowired
    SanPhamRepository sanPhamRepository;

    @Autowired
    private KieuGiayService kieuGiayService;

    @Autowired
    private MauSacService mauSacService;

    @Autowired
    private ExcelExportService excelExportService;

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
                        @RequestParam(required = false) String maDanhMuc,
                        @RequestParam(required = false) Boolean tt,
                        @RequestParam(required = false) String maTH,
                        @RequestParam(required = false) String maKG,
                        @RequestParam(required = false) String t,
                        Model model) {

        // Chuyển chuỗi rỗng thành null
        if (maDanhMuc != null && maDanhMuc.trim().isEmpty()) {
            maDanhMuc = null;
        }
        if (maTH != null && maTH.trim().isEmpty()) {
            maTH = null;
        }
        if (maKG != null && maKG.trim().isEmpty()) {
            maKG = null;
        }
        if (t != null && t.trim().isEmpty()) {
            t = null;
        }

        System.out.println("=== FILTER PARAMS (AFTER CLEAN) ===");
        System.out.println("maDanhMuc: '" + maDanhMuc + "'");
        System.out.println("tt: " + tt);
        System.out.println("maTH: '" + maTH + "'");
        System.out.println("maKG: '" + maKG + "'");
        System.out.println("t: '" + t + "'");

        Page<SanPham> pageSanPham = sanPhamService.searchSanPham(maDanhMuc, tt, maTH, maKG, t, PageRequest.of(page, 5));

        System.out.println("Total elements: " + pageSanPham.getTotalElements());
        System.out.println("Content size: " + pageSanPham.getContent().size());

        // In ra danh sách sản phẩm tìm được
        for (SanPham sp : pageSanPham.getContent()) {
            System.out.println("Found: " + sp.getMaSanPham() + " - " + sp.getTenSanPham() + " - TH: " +
                    (sp.getThuongHieu() != null ? sp.getThuongHieu().getMaThuongHieu() : "NULL"));
        }

        Optional<SanPham> sp0020 = sanPhamService.findById("SP0020");
        if (sp0020.isPresent()) {
            SanPham sp = sp0020.get();
            System.out.println("=== SP0020 ===");
            System.out.println("MaSanPham: " + sp.getMaSanPham());
            System.out.println("TenSanPham: " + sp.getTenSanPham());
            System.out.println("TrangThai: " + sp.getTrangThai());
            System.out.println("MaThuongHieu: " + (sp.getThuongHieu() != null ? sp.getThuongHieu().getMaThuongHieu() : "NULL"));
            System.out.println("DanhMuc: " + (sp.getDanhMucSanPham() != null ? sp.getDanhMucSanPham().getMaDanhMuc() : "NULL"));
            System.out.println("KieuGiay: " + (sp.getKieuGiay() != null ? sp.getKieuGiay().getMaKieuGiay() : "NULL"));
        } else {
            System.out.println("=== SP0020 NOT FOUND ===");
        }

        // ===== CHUYỂN ĐỔI SANG DTO VỚI THÔNG TIN GIÁ =====
        List<SanPhamDTO> sanPhamDTOList = new ArrayList<>();
        for (SanPham sp : pageSanPham.getContent()) {
            SanPhamDTO dto = new SanPhamDTO();
            dto.setMaSanPham(sp.getMaSanPham());
            dto.setTenSanPham(sp.getTenSanPham());
            dto.setMoTa(sp.getMoTa());
            dto.setTrangThai(sp.getTrangThai());
            dto.setMaDanhMuc(sp.getDanhMucSanPham() != null ? sp.getDanhMucSanPham().getMaDanhMuc() : null);
            dto.setMaThuongHieu(sp.getThuongHieu() != null ? sp.getThuongHieu().getMaThuongHieu() : null);
            dto.setMaKieuGiay(sp.getKieuGiay() != null ? sp.getKieuGiay().getMaKieuGiay() : null);
            dto.setMaChatLieu(sp.getChatLieu() != null ? sp.getChatLieu().getMaChatLieu() : null);

            // ===== LẤY TÊN THƯƠNG HIỆU =====
            if (sp.getThuongHieu() != null) {
                dto.setTenThuongHieu(sp.getThuongHieu().getTenThuongHieu());
            } else {
                dto.setTenThuongHieu("");
            }

            // Lấy tổng tồn kho
            dto.setTongTon(sp.getTongTon());

            // Lấy khoảng giá
            String maSanPham = sp.getMaSanPham();
            BigDecimal minPrice = sanPhamChiTietService.getGiaMin(maSanPham);
            BigDecimal maxPrice = sanPhamChiTietService.getGiaMax(maSanPham);

            dto.setGiaMin(minPrice);
            dto.setGiaMax(maxPrice);

            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            if (minPrice.compareTo(maxPrice) == 0) {
                dto.setGiaBanDisplay(formatter.format(minPrice) + "₫");
            } else {
                dto.setGiaBanDisplay(formatter.format(minPrice) + "₫ - " + formatter.format(maxPrice) + "₫");
            }

            sanPhamDTOList.add(dto);
        }

        model.addAttribute("listsp", sanPhamDTOList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageSanPham.getTotalPages());
        model.addAttribute("totalItems", pageSanPham.getTotalElements());

        // Thống kê
        model.addAttribute("totalActive", sanPhamService.countByTrangThai(true));
        model.addAttribute("totalInactive", sanPhamService.countByTrangThai(false));

        // Các danh sách cho filter
        model.addAttribute("listdmsp", danhMucSanPhamService.getAll());
        model.addAttribute("listcl", chatLieuService.findAll());
        model.addAttribute("listkg", kieuGiayService.findAll());
        model.addAttribute("listth", thuongHieuService.findAll());

        return "sanpham/index";
    }

    @GetMapping("/export-excel")
    public ResponseEntity<InputStreamResource> exportExcel(
            @RequestParam(required = false) String maDanhMuc,
            @RequestParam(required = false) Boolean tt,
            @RequestParam(required = false) String maTH,
            @RequestParam(required = false) String maKG,
            @RequestParam(required = false) String t) {

        try {
            // Lấy tất cả sản phẩm theo bộ lọc (không phân trang)
            List<SanPham> sanPhamList = sanPhamService.findAllWithFilters(maDanhMuc, tt, maTH, maKG, t);

            // Chuyển đổi sang DTO
            List<SanPhamDTO> sanPhamDTOList = convertToDTO(sanPhamList);

            // Xuất Excel
            ByteArrayInputStream in = excelExportService.exportSanPhamToExcel(sanPhamDTOList);

            if (in == null) {
                return ResponseEntity.badRequest().build();
            }

            // Tạo tên file
            String fileName = "Danh_sach_san_pham_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=" + fileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(in));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // ===== PHƯƠNG THỨC CHUYỂN ĐỔI SANG DTO =====
    private List<SanPhamDTO> convertToDTO(List<SanPham> sanPhamList) {
        List<SanPhamDTO> dtoList = new ArrayList<>();
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

        for (SanPham sp : sanPhamList) {
            SanPhamDTO dto = new SanPhamDTO();
            dto.setMaSanPham(sp.getMaSanPham());
            dto.setTenSanPham(sp.getTenSanPham());
            dto.setMoTa(sp.getMoTa());
            dto.setTrangThai(sp.getTrangThai());
            dto.setMaDanhMuc(sp.getDanhMucSanPham() != null ? sp.getDanhMucSanPham().getMaDanhMuc() : null);
            dto.setMaThuongHieu(sp.getThuongHieu() != null ? sp.getThuongHieu().getMaThuongHieu() : null);
            dto.setMaKieuGiay(sp.getKieuGiay() != null ? sp.getKieuGiay().getMaKieuGiay() : null);
            dto.setMaChatLieu(sp.getChatLieu() != null ? sp.getChatLieu().getMaChatLieu() : null);

            // Lấy tên thương hiệu
            if (sp.getThuongHieu() != null) {
                dto.setTenThuongHieu(sp.getThuongHieu().getTenThuongHieu());
            }

            // Lấy tổng tồn kho
            dto.setTongTon(sp.getTongTon());

            // Lấy khoảng giá
            String maSanPham = sp.getMaSanPham();
            BigDecimal minPrice = sanPhamChiTietService.getGiaMin(maSanPham);
            BigDecimal maxPrice = sanPhamChiTietService.getGiaMax(maSanPham);

            dto.setGiaMin(minPrice);
            dto.setGiaMax(maxPrice);

            // Format hiển thị giá
            if (minPrice.compareTo(maxPrice) == 0) {
                dto.setGiaBanDisplay(formatter.format(minPrice) + "₫");
            } else {
                dto.setGiaBanDisplay(formatter.format(minPrice) + "₫ - " + formatter.format(maxPrice) + "₫");
            }

            dtoList.add(dto);
        }

        return dtoList;
    }

    @GetMapping("/add-view")
    public String addView(Model model) {
        model.addAttribute("activeMenu", "sanpham");

        SanPhamDTO formObject = new SanPhamDTO();
        model.addAttribute("form", formObject);

        formObject.setMaSanPham(taoMaSanPham());


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
            sp.setMaSanPham(spDto.getMaSanPham());
            sp.setTenSanPham(spDto.getTenSanPham());
            sp.setMoTa(spDto.getMoTa());

            sp.setDanhMucSanPham(danhMucSanPhamService.findById(spDto.getMaDanhMuc()).orElse(null));
            sp.setThuongHieu(thuongHieuService.findById(spDto.getMaThuongHieu()).orElse(null));
            sp.setKieuGiay(kieuGiayService.findById(spDto.getMaKieuGiay()).orElse(null));
            sp.setChatLieu(chatLieuService.findById(spDto.getMaChatLieu()).orElse(null));

            sp.setTrangThai(true);
            sp.setNgayTao(LocalDateTime.now());
            sanPhamService.save(sp);

            // ===== XỬ LÝ CHI TIẾT SẢN PHẨM VỚI NHIỀU ẢNH =====
            for (SanPhamChiTietDTO ctDto : wrapperDto.getChiTietList()) {

                SanPhamChiTiet ct = new SanPhamChiTiet();

                ct.setSanPham(sp);

                String maSP = sp.getMaSanPham();

                MauSac mauSac = mauSacService.findById(ctDto.getMaMauSac()).orElse(null);
                KichThuoc kichThuoc = kichThuocService.getKichThuocById(ctDto.getMaKichThuoc()).orElse(null);

                String maMau = (mauSac != null) ? mauSac.getMaMauSac() : "";
                String tenSize = (kichThuoc != null) ? kichThuoc.getTenKichThuoc() : "";

                String maBienThe = maSP + "-" + maMau + "-" + tenSize;
                ct.setMaSanPhamChiTiet(maBienThe);

                // Set thông tin cơ bản
                ct.setGiaBan(ctDto.getGiaBan());
                ct.setSoLuongTon(ctDto.getSoLuongTon());
                ct.setMauSac(mauSac);
                ct.setKichThuoc(kichThuoc);
                ct.setNgayTao(LocalDateTime.now());

                // ===== XỬ LÝ ẢNH =====
                List<String> danhSachAnh = ctDto.getDanhSachAnh();

                // 1. Lấy ảnh đại diện
                String anhDaiDien = ctDto.getDuongDanAnh();
                if (anhDaiDien == null || anhDaiDien.isEmpty()) {
                    if (danhSachAnh != null && !danhSachAnh.isEmpty()) {
                        anhDaiDien = danhSachAnh.get(0);
                    }
                }
                ct.setDuongDanAnh(anhDaiDien);

                // 2. Lưu danh sách ảnh dạng JSON
                if (danhSachAnh != null && !danhSachAnh.isEmpty()) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        String json = mapper.writeValueAsString(danhSachAnh);
                        ct.setDanhSachAnh(json);
                    } catch (Exception e) {
                        ct.setDanhSachAnh(String.join(",", danhSachAnh));
                    }
                }

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
    public String edit(@PathVariable("id") String id,
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
    public String detail(@PathVariable("id") String id,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(required = false) String maDanhMuc,
                         @RequestParam(required = false) Boolean tt,
                         @RequestParam(required = false) String maTH,
                         @RequestParam(required = false) String maKG,
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

    public String taoMaSanPham() {
        Random random = new Random();
        String maSP;

        do {
            int so = random.nextInt(10000);
            maSP = "SP" + String.format("%04d", so);
        } while (sanPhamRepository.existsByMaSanPham(maSP));

        return maSP;
    }

    @PostMapping("/update")
    public String update(@ModelAttribute SanPham sanpham,
                         @RequestParam(required = false) String maDanhMuc,
                         @RequestParam(required = false) String maThuongHieu,
                         @RequestParam(required = false) String maChatLieu,
                         @RequestParam(required = false) String maKieuGiay,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(required = false) String source,
                         RedirectAttributes redirectAttributes) {

        SanPham spOld = sanPhamService.findById(sanpham.getMaSanPham())
                .orElseThrow();

        spOld.setTenSanPham(sanpham.getTenSanPham());
        spOld.setMoTa(sanpham.getMoTa());

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
    public String toggleStatus(@PathVariable("id") String id,
                               @RequestParam("status") boolean status,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(required = false) String maDanhMuc,
                               @RequestParam(required = false) Boolean tt,
                               @RequestParam(required = false) String maTH,
                               @RequestParam(required = false) String maKG,
                               @RequestParam(required = false) String t,
                               RedirectAttributes redirectAttributes) {

        sanPhamService.updateTrangThai(id, status);

        if (!status) {
            sanPhamChiTietService.suaSanPham2(id);
        } else {
            sanPhamChiTietService.suaSanPham3(id);
        }

        // Xây dựng URL redirect với tất cả tham số filter
        String redirectUrl = "redirect:/sanpham/index?page=" + page;
        if (maDanhMuc != null && !maDanhMuc.isEmpty()) redirectUrl += "&maDanhMuc=" + maDanhMuc;
        if (tt != null) redirectUrl += "&tt=" + tt;
        if (maTH != null && !maTH.isEmpty()) redirectUrl += "&maTH=" + maTH;
        if (maKG != null && !maKG.isEmpty()) redirectUrl += "&maKG=" + maKG;
        if (t != null && !t.isEmpty()) redirectUrl += "&t=" + t;

        return redirectUrl;
    }

    // Trong SanPhamController.java - Sửa API check-and-add-variants
    @PostMapping("/api/check-and-add-variants")
    @ResponseBody
    public ResponseEntity<?> checkAndAddVariants(@RequestBody SanPhamWrapperDTO wrapperDto) {
        try {
            String tenSanPham = wrapperDto.getSanPham().getTenSanPham();

            boolean exists = sanPhamService.isTenSanPhamDuplicate(tenSanPham);

            if (!exists) {
                Map<String, Object> response = new HashMap<>();
                response.put("exists", false);
                response.put("message", "Tên sản phẩm chưa tồn tại, có thể tạo mới");
                return ResponseEntity.ok(response);
            }

            SanPham existingProduct = sanPhamService.findByTenSanPham(tenSanPham);
            if (existingProduct == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("exists", true);
                response.put("canAdd", false);
                response.put("message", "Không tìm thấy sản phẩm dù tên bị trùng!");
                return ResponseEntity.ok(response);
            }

            List<SanPhamChiTiet> existingVariants = sanPhamChiTietService.getallsp(existingProduct.getMaSanPham());
            Set<String> existingVariantCodes = existingVariants.stream()
                    .map(SanPhamChiTiet::getMaSanPhamChiTiet)
                    .collect(Collectors.toSet());

            List<String> newVariants = new ArrayList<>();
            List<String> duplicateVariants = new ArrayList<>();

            // ===== SỬA LỖI Ở ĐÂY =====
            for (SanPhamChiTietDTO ctDto : wrapperDto.getChiTietList()) {
                // Lấy mã biến thể từ DTO
                String maBienThe = ctDto.getMaBienThe();

                // Nếu không có maBienThe, tự tạo từ màu sắc và kích thước
                if (maBienThe == null || maBienThe.isEmpty()) {
                    String maMau = ctDto.getMaMauSac();
                    String maKichThuoc = ctDto.getMaKichThuoc();
                    String tenSize = kichThuocService.getKichThuocById(maKichThuoc)
                            .map(KichThuoc::getTenKichThuoc)
                            .orElse(maKichThuoc);
                    maBienThe = existingProduct.getMaSanPham() + "-" + maMau + "-" + tenSize;
                }

                if (existingVariantCodes.contains(maBienThe)) {
                    duplicateVariants.add(maBienThe);
                } else {
                    newVariants.add(maBienThe);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("exists", true);
            response.put("canAdd", !newVariants.isEmpty());
            response.put("productCode", existingProduct.getMaSanPham());
            response.put("productName", existingProduct.getTenSanPham());
            response.put("existingVariantCount", existingVariants.size());
            response.put("newVariantCount", newVariants.size());
            response.put("duplicateCount", duplicateVariants.size());
            response.put("duplicateVariants", duplicateVariants);
            response.put("newVariants", newVariants);
            response.put("message", "Sản phẩm đã tồn tại! Bạn có muốn thêm " + newVariants.size() + " biến thể mới vào sản phẩm này không?");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/api/add-variants-to-existing")
    @ResponseBody
    public ResponseEntity<?> addVariantsToExisting(@RequestBody SanPhamWrapperDTO wrapperDto) {
        try {
            String tenSanPham = wrapperDto.getSanPham().getTenSanPham();

            // Tìm sản phẩm theo tên
            SanPham existingProduct = sanPhamService.findByTenSanPham(tenSanPham);
            if (existingProduct == null) {
                return ResponseEntity.badRequest().body("Không tìm thấy sản phẩm!");
            }

            // Lấy danh sách biến thể hiện có
            List<SanPhamChiTiet> existingVariants = sanPhamChiTietService.getallsp(existingProduct.getMaSanPham());
            Set<String> existingVariantCodes = existingVariants.stream()
                    .map(SanPhamChiTiet::getMaSanPhamChiTiet)
                    .collect(Collectors.toSet());

            int addedCount = 0;
            int skippedCount = 0;

            for (SanPhamChiTietDTO ctDto : wrapperDto.getChiTietList()) {
                MauSac mauSac = mauSacService.findById(ctDto.getMaMauSac()).orElse(null);
                KichThuoc kichThuoc = kichThuocService.getKichThuocById(ctDto.getMaKichThuoc()).orElse(null);

                if (mauSac == null || kichThuoc == null) {
                    continue;
                }

                String maBienThe = existingProduct.getMaSanPham() + "-" + mauSac.getMaMauSac() + "-" + kichThuoc.getTenKichThuoc();

                // Kiểm tra biến thể đã tồn tại
                if (existingVariantCodes.contains(maBienThe)) {
                    skippedCount++;
                    continue;
                }

                // Tạo biến thể mới
                SanPhamChiTiet ct = new SanPhamChiTiet();
                ct.setMaSanPhamChiTiet(maBienThe);
                ct.setSanPham(existingProduct);
                ct.setMauSac(mauSac);
                ct.setKichThuoc(kichThuoc);
                ct.setGiaBan(ctDto.getGiaBan());
                ct.setSoLuongTon(ctDto.getSoLuongTon());
                ct.setNgayTao(LocalDateTime.now());

                // Xử lý ảnh
                List<String> danhSachAnh = ctDto.getDanhSachAnh();
                String anhDaiDien = ctDto.getDuongDanAnh();
                if (anhDaiDien == null || anhDaiDien.isEmpty()) {
                    if (danhSachAnh != null && !danhSachAnh.isEmpty()) {
                        anhDaiDien = danhSachAnh.get(0);
                    }
                }
                ct.setDuongDanAnh(anhDaiDien);

                if (danhSachAnh != null && !danhSachAnh.isEmpty()) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        String json = mapper.writeValueAsString(danhSachAnh);
                        ct.setDanhSachAnh(json);
                    } catch (Exception e) {
                        ct.setDanhSachAnh(String.join(",", danhSachAnh));
                    }
                }

                sanPhamChiTietService.capNhatTrangThaii(ct);
                sanPhamChiTietService.them(ct);
                addedCount++;
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã thêm " + addedCount + " biến thể mới vào sản phẩm " + existingProduct.getTenSanPham() +
                    (skippedCount > 0 ? " (bỏ qua " + skippedCount + " biến thể đã tồn tại)" : ""));
            response.put("addedCount", addedCount);
            response.put("skippedCount", skippedCount);
            response.put("productCode", existingProduct.getMaSanPham());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

}
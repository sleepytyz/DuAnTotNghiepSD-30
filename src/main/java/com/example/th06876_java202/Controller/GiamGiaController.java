    package com.example.th06876_java202.Controller;

    import com.example.th06876_java202.Entity.*;
    import com.example.th06876_java202.Repository.GiamGiaChiTietRepo;
    import com.example.th06876_java202.Repository.KhachHangRepository;
    import com.example.th06876_java202.Service.EmailService;
    import com.example.th06876_java202.Service.GiamGiaChiTietService;
    import com.example.th06876_java202.Service.GiamGiaService;
    import com.example.th06876_java202.Service.KhachHangService;
    import jakarta.validation.Valid;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.PageRequest;
    import org.springframework.format.annotation.DateTimeFormat;
    import org.springframework.stereotype.Controller;
    import org.springframework.transaction.annotation.Transactional;
    import org.springframework.ui.Model;
    import org.springframework.validation.BindingResult;
    import org.springframework.validation.Errors;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.bind.support.SessionStatus;
    import org.springframework.web.servlet.mvc.support.RedirectAttributes;

    import java.math.BigDecimal;
    import java.time.LocalDateTime;
    import java.util.List;

    @Controller
    @RequestMapping("/giamgia")
    @SessionAttributes("giamGia")
    public class GiamGiaController {

        @Autowired
        private GiamGiaService giamGiaService;

        @Autowired
        KhachHangService khachHangService;

        @Autowired
        KhachHangRepository khachHangRepo;

        @Autowired
        EmailService emailService;

        @Autowired
        GiamGiaChiTietRepo giamGiaChiTietRepo;

        @GetMapping("/api/khachhang/suggest")
        @ResponseBody
        public List<KhachHang> suggest(@RequestParam String sdt) {
            return khachHangRepo.findTop10BySdtContaining(sdt);
        }

        @GetMapping("/index")
        public String index(
                @RequestParam(required = false) String keyword,
                @RequestParam(required = false) String tt,
                @RequestParam(required = false) String lg,
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ngaybdau,
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ngaykthuc,
                @RequestParam(defaultValue = "0") int page,
                Model model) {

            Page<GiamGia> pageData = giamGiaService.getFilteredGiamGia(keyword, tt, lg, ngaybdau, ngaykthuc, page);

            model.addAttribute("list", pageData.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", pageData.getTotalPages());
            model.addAttribute("totalItems", pageData.getTotalElements());

            model.addAttribute("keyword", keyword);
            model.addAttribute("tt", tt);
            model.addAttribute("lg", lg);
            model.addAttribute("ngaybdau", ngaybdau);
            model.addAttribute("ngaykthuc", ngaykthuc);

            return "giamgia/index";
        }
        @GetMapping("/create")
        public String create(@RequestParam(required = false) String sdt,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(required = false) String action,
                             SessionStatus status,
                             Model model) {

            if ("new".equals(action)) {
                status.setComplete();
            }

            Page<KhachHang> pageKh = khachHangService.searchByPhone(sdt, page);

            model.addAttribute("giamGia", new GiamGia());
            model.addAttribute("isEdit", false);
            model.addAttribute("listKhachHang", pageKh.getContent());
            model.addAttribute("currentPage", pageKh.getNumber());
            model.addAttribute("totalPages", pageKh.getTotalPages());
            model.addAttribute("totalItems", pageKh.getTotalElements());
            model.addAttribute("sdt", sdt);
            return "giamgia/add";
        }

        @GetMapping("/edit/{id}")
        public String edit(@PathVariable("id") Integer id,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(required = false) String sdt,
                           Model model,
                           RedirectAttributes ra) {

            GiamGia giamGia = giamGiaService.getGiamGiaById(id).orElse(null);

            String trangThai = giamGia.getTrangThai();
            if (giamGia == null || trangThai == null || !"Sắp hoạt động".equals(trangThai.trim())) {
                ra.addAttribute("error", "Chỉ có thể chỉnh sửa chương trình ở trạng thái 'Sắp hoạt động'!");
                return "redirect:/giamgia/index";
            }

            model.addAttribute("giamGia", giamGia);
            model.addAttribute("isEdit", true);
            model.addAttribute("maGiamGia", id);

            Page<KhachHang> pageKh = khachHangService.searchByPhone(sdt, page);

            model.addAttribute("listKhachHang", pageKh.getContent());
            model.addAttribute("currentPage", pageKh.getNumber());
            model.addAttribute("totalPages", pageKh.getTotalPages());
            model.addAttribute("totalItems", pageKh.getTotalElements());
            model.addAttribute("sdt", sdt);

            List<Integer> selectedIds = giamGiaChiTietRepo.findMaKhachHangByMaGiamGia(id);
            model.addAttribute("selectedKhachHangIds", selectedIds);

            long totalSelectedCount = giamGiaChiTietRepo.countByGiamGia_MaGiamGia(id);
            model.addAttribute("totalSelectedCount", totalSelectedCount);

            return "giamgia/add";
        }
        @PostMapping("/add")
        @Transactional
        public String add(@Valid @ModelAttribute("giamGia") GiamGia giamGia,
                          BindingResult result,
                          SessionStatus status,
                          @RequestParam(value = "selectedKhachHang", required = false) List<Integer> selectedKhachHang,
                          @RequestParam(defaultValue = "0") int page,
                          Model model,
                          RedirectAttributes ra) {

            if (giamGia.getLoaiApDung() == 2 && (selectedKhachHang == null || selectedKhachHang.isEmpty())) {
                result.rejectValue("loaiApDung", "error.giamGia", "Vui lòng chọn ít nhất một khách hàng cho chương trình cá nhân!");
            }

            if (result.hasErrors()) {
                Page<KhachHang> pageKh = khachHangService.searchByPhone(null, page);
                model.addAttribute("listKhachHang", pageKh.getContent());
                model.addAttribute("currentPage", pageKh.getNumber());
                model.addAttribute("totalPages", pageKh.getTotalPages());
                model.addAttribute("isEdit", false);
                return "giamgia/add";
            }

            try {
                giamGia.setTrangThai(giamGiaService.tinhToanTrangThai(giamGia));
                GiamGia saved = giamGiaService.save(giamGia);

                if (giamGia.getLoaiApDung() == 2 && selectedKhachHang != null) {
                    VoucherEmailDTO dto = new VoucherEmailDTO();
                    dto.setTenGiamGia(saved.getTenGiamGia());
                    dto.setLoaiGiamGia(saved.getLoaiGiamGia());
                    dto.setGiaTri(saved.getGiaTriGiam());
                    dto.setNgayBatDau(saved.getNgayBatDau());
                    dto.setNgayKetThuc(saved.getNgayKetThuc());
                    dto.setMaGiamGia(saved.getMaGiamGia());
                    dto.setDonToiThieu(saved.getDonToiThieu());
                    dto.setGiamToiDa(saved.getGiamToiDa());

                    for (Integer maKH : selectedKhachHang) {
                        var kh = khachHangService.getKhachHangById(maKH);
                        if (kh == null) continue;

                        GiamGiaChiTiet ct = new GiamGiaChiTiet(new GiamGiaChiTietId(maKH, saved.getMaGiamGia()), kh, saved, LocalDateTime.now(), 0);
                        giamGiaChiTietRepo.save(ct);

                        try {
                            emailService.sendVoucherEmail(kh.getEmail(), dto);
                        } catch (Exception e) {
                            System.out.println("Send mail fail: " + kh.getEmail());
                            e.printStackTrace();
                        }
                    }
                }

                status.setComplete();
                ra.addFlashAttribute("mess", "Thêm mới thành công!");
                return "redirect:/giamgia/index";
            } catch (Exception e) {
                model.addAttribute("error", "Lỗi: " + e.getMessage());
                return "giamgia/add";
            }
        }


        @GetMapping("/delete/{id}")
        public String delete(@PathVariable("id") Integer id) {
            giamGiaService.suattt(id);
            return "redirect:/giamgia/index";
        }

        @PostMapping("/update")
        @Transactional
        public String update(@ModelAttribute("giamGia") @Valid GiamGia giamGia,
                             SessionStatus status,
                             BindingResult result,
                             @RequestParam(value = "selectedKhachHang", required = false) List<Integer> selectedKhachHang,
                             @RequestParam(defaultValue = "0") int page,
                             RedirectAttributes ra, Model model) {

            if (giamGia.getLoaiApDung() == 2 && (selectedKhachHang == null || selectedKhachHang.isEmpty())) {
                result.rejectValue("loaiApDung", "error.giamGia", "Vui lòng chọn ít nhất một khách hàng cho chương trình cá nhân!");
            }

            if (result.hasErrors()) {
                model.addAttribute("isEdit", true);
                Page<KhachHang> pageKh = khachHangService.searchByPhone(null, page);
                model.addAttribute("listKhachHang", pageKh.getContent());
                model.addAttribute("currentPage", pageKh.getNumber());
                model.addAttribute("totalPages", pageKh.getTotalPages());
                return "giamgia/add";
            }

            giamGiaService.save(giamGia);
            giamGiaChiTietRepo.updateTrangThaiByMaGiamGia(giamGia.getMaGiamGia(), 0);

            if (giamGia.getLoaiApDung() == 2 && selectedKhachHang != null) {
                for (Integer maKH : selectedKhachHang) {
                    GiamGiaChiTiet existing = giamGiaChiTietRepo.findById(new GiamGiaChiTietId(maKH, giamGia.getMaGiamGia())).orElse(null);
                    if (existing != null) {
                        existing.setTrangThaiSuDung(1);
                        giamGiaChiTietRepo.save(existing);
                    } else {
                        giamGiaChiTietRepo.save(new GiamGiaChiTiet(new GiamGiaChiTietId(maKH, giamGia.getMaGiamGia()),
                                khachHangService.getKhachHangById(maKH), giamGia, LocalDateTime.now(), 1));
                    }
                }
            }

            status.setComplete();
            ra.addFlashAttribute("mess", "Cập nhật thành công!");
            return "redirect:/giamgia/index";
        }

        @GetMapping("loctt")
        public String loctt(@RequestParam("tt") String tt,Model model) {
            List<GiamGia> list = giamGiaService.loctt(tt);
            model.addAttribute("list", list); model.addAttribute("giamGia", new GiamGia());
            return "giamgia/index";
        }

        @GetMapping("loclg")
        public String loctloai(@RequestParam("lg") String tt,Model model) {
            List<GiamGia> list = giamGiaService.loclg(tt);
            model.addAttribute("list", list); model.addAttribute("giamGia", new GiamGia());
            return "giamgia/index";
        }

        @GetMapping("locten")
        public String locten(@RequestParam("ten") String tt,Model model) {
            List<GiamGia> list = giamGiaService.timkiem(tt);
            model.addAttribute("list", list); model.addAttribute("giamGia", new GiamGia());
            return "giamgia/index";
        }

        @GetMapping("locngay")
        public String loctt(@RequestParam("ngaybdau")LocalDateTime ngay1, @RequestParam("ngaykthuc")LocalDateTime ngay2 ,  Model model) {
            List<GiamGia> list = giamGiaService.locng(ngay1,ngay2);
            model.addAttribute("list", list); model.addAttribute("giamGia", new GiamGia());
            return "giamgia/index";
        }


    }

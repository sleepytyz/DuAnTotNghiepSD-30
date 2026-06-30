    package com.example.th06876_java202.Controller;


    import com.example.th06876_java202.Entity.*;
    import com.example.th06876_java202.Repository.DotGiamGiaRepo;
    import com.example.th06876_java202.Repository.NhanVienRepository;
    import com.example.th06876_java202.Service.*;
    import jakarta.servlet.http.HttpSession;
    import jakarta.validation.Valid;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.cglib.core.Local;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.PageRequest;
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.domain.Sort;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.validation.BindingResult;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.servlet.mvc.support.RedirectAttributes;

    import java.math.BigDecimal;
    import java.time.LocalDate;
    import java.util.List;
    import java.util.stream.Collectors;

    @Controller
    public class DotGiamGiaCon {
        @Autowired
        private DotGiamGiaService dotGiamGiaService;

        @Autowired
        private SanPhamChiTietService sanPhamChiTietService;

        @Autowired
        private ChiTietDotGiamGiaService chiTietDotGiamGiaService;

        @Autowired
        private SanPhamService sanPhamservice;


        @GetMapping("/dot-giam-gia/hien-thi")
        public String hienThi(
                @RequestParam(required = false) String keyword,
                @RequestParam(required = false) String trangThai,
                @RequestParam(required = false) LocalDate tuNgay,
                @RequestParam(required = false) LocalDate denNgay,
                @RequestParam(defaultValue = "0") int page,
                Model model) {
            dotGiamGiaService.capNhatTrangThai();

            Pageable pageable = PageRequest.of(
                    page,
                    5,
                    Sort.by(Sort.Direction.DESC, "maGiamGia")
            );

            Page<DotGiamGia> dggPage =
                    dotGiamGiaService.filterPaging(
                            keyword,
                            trangThai,
                            tuNgay,
                            denNgay,
                            pageable
                    );

            model.addAttribute("listDGG", dggPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", dggPage.getTotalPages());
            model.addAttribute("totalItems", dggPage.getTotalElements());

            model.addAttribute("keyword", keyword);
            model.addAttribute("trangThai", trangThai);
            model.addAttribute("tuNgay", tuNgay);
            model.addAttribute("denNgay", denNgay);

            model.addAttribute("dgg", new DotGiamGia());

            return "dotgiamgia/index";
        }

        @GetMapping("/dot-giam-gia/detail/{id}")
        public String detail(@PathVariable("id") Integer id, Model model,
                             RedirectAttributes redirectAttributes) {

            DotGiamGia dgg = dotGiamGiaService.getById(id);

            if (dgg == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đợt giảm giá!");
                return "redirect:/dot-giam-gia/hien-thi";
            }

            if (!"Sắp hoạt động".equals(dgg.getTrangThai())) {
                redirectAttributes.addFlashAttribute("error",
                        "Chỉ được thao tác khi đợt giảm giá ở trạng thái Sắp hoạt động!");
                return "redirect:/dot-giam-gia/hien-thi";
            }

            DotGiamGiaDTO dto = new DotGiamGiaDTO();
            dto.setDotGiamGia(dgg);

            dto.setListMaSanPham(chiTietDotGiamGiaService.getSanPhamByDot(id));
            dto.setListMaSanPhamChiTiet(chiTietDotGiamGiaService.getSanPhamChiTietByDot(id));

            model.addAttribute("dggDTO", dto);
            model.addAttribute("listSP", sanPhamservice.getAll());

            return "chitietdotgiamgia/index";
        }

        @GetMapping("/dot-giam-gia/delete/{id}")
        public String delete(@PathVariable("id") Integer id, Model model) {
            dotGiamGiaService.suaa(id);
            return "redirect:/dot-giam-gia/hien-thi";
        }

        @PostMapping("/dot-giam-gia/save-all")
        public String saveAll(@Valid @ModelAttribute("dggDTO") DotGiamGiaDTO dggDTO, BindingResult result, Model model) {
            System.out.println("Tên giảm giá nhận được: " + (dggDTO.getDotGiamGia() != null ? dggDTO.getDotGiamGia().getTenGiamGia() : "NULL OBJECT"));
            if (dggDTO.getDotGiamGia().getNgayBatDau() != null
                    && dggDTO.getDotGiamGia().getNgayKetThuc() != null
                    && dggDTO.getDotGiamGia().getNgayKetThuc()
                    .isBefore(dggDTO.getDotGiamGia().getNgayBatDau())) {

                result.rejectValue(
                        "dotGiamGia.ngayKetThuc",
                        "error.ngayKetThuc",
                        "Ngày kết thúc phải lớn hơn ngày bắt đầu");
            }

            if (dggDTO.getDotGiamGia().getNgayBatDau() != null
                    && dggDTO.getDotGiamGia().getNgayBatDau().isBefore(LocalDate.now())) {

                result.rejectValue(
                        "dotGiamGia.ngayBatDau",
                        "error.ngayBatDau",
                        "Ngày bắt đầu phải từ hôm nay trở đi");
            }

            if (result.hasErrors()) {
                model.addAttribute("listSP", sanPhamservice.getAll());

                if (dggDTO.getListMaSanPhamChiTiet() == null
                        || dggDTO.getListMaSanPhamChiTiet().isEmpty()) {

                    model.addAttribute("errorBienThe",
                            "Vui lòng chọn ít nhất một sản phẩm chi tiết");
                }

                return "chitietdotgiamgia/index";
            }

            if (dggDTO.getListMaSanPhamChiTiet() == null
                    || dggDTO.getListMaSanPhamChiTiet().isEmpty()) {

                model.addAttribute("listSP", sanPhamservice.getAll());
                model.addAttribute("errorBienThe",
                        "Vui lòng chọn ít nhất một sản phẩm chi tiết");

                return "chitietdotgiamgia/index";
            }

            DotGiamGia dgg = dggDTO.getDotGiamGia();
            dgg.setNgayTao(LocalDate.now()); // Thiết lập ngày tạo
            dgg.setTrangThai("Sắp hoạt động");
            DotGiamGia savedDGG = dotGiamGiaService.save(dggDTO.getDotGiamGia());

            if (dggDTO.getListMaSanPham() != null && dggDTO.getListMaSanPhamChiTiet() != null) {
                for (Integer maSP : dggDTO.getListMaSanPham()) {
                    chiTietDotGiamGiaService.saveAllDetails(
                            savedDGG.getMaGiamGia(),
                            maSP,
                            dggDTO.getListMaSanPhamChiTiet()
                    );
                }
            }
            return "redirect:/dot-giam-gia/hien-thi";
        }

        @GetMapping("/chi-tiet-dot-giam-gia/them-moi")
        public String showPageThemMoi(Model model) {

            model.addAttribute("dggDTO", new DotGiamGiaDTO());

            model.addAttribute("listSP", sanPhamservice.getAll());

            return "chitietdotgiamgia/index";
        }

        @GetMapping("/api/get-bien-the-list")
        @ResponseBody
        public List<SanPhamChiTietDTOgg> getBienTheList(@RequestParam List<Integer> listMaSanPham) {
            List<SanPhamChiTiet> listEntity = sanPhamChiTietService.findsp(listMaSanPham);

            return listEntity.stream().map(e -> {
                SanPhamChiTietDTOgg dto = new SanPhamChiTietDTOgg();
                dto.setMaSanPhamChiTiet(e.getMaSanPhamChiTiet());
                dto.setTenSanPham(e.getSanPham().getTenSanPham());
                dto.setTenKichThuoc(e.getKichThuoc().getTenKichThuoc());
                dto.setTenMauSac(e.getMauSac().getTenMauSac());
                dto.setGiaNhap(e.getGiaNhap());
                dto.setGiaBan(e.getGiaBan());
                dto.setSoLuongTon(e.getSoLuongTon());
                dto.setTrangThai(e.getTrangThai());
                dto.setDuongDanAnh(e.getDuongDanAnh());
                return dto;
            }).collect(Collectors.toList());
        }

        @PostMapping("/dot-giam-gia/add")
        public String add(@Valid @ModelAttribute("dgg") DotGiamGia dgg,
                          BindingResult result,
                          HttpSession session,
                          Model model) {


            if (result.hasErrors()) {
                model.addAttribute("showModal", true);
                model.addAttribute("listDGG", dotGiamGiaService.getAll());
                return "dotgiamgia/index";
            }
            dgg.setNgayTao(LocalDate.now());
            dotGiamGiaService.save(dgg);

            return "redirect:/dot-giam-gia/hien-thi";
        }

        @PostMapping("/dot-giam-gia/update")
        public String update(@Valid @ModelAttribute("dgg") DotGiamGia dgg,
                             BindingResult result,
                             Model model) {


            if (result.hasErrors()) {
                model.addAttribute("showModal", true);
                model.addAttribute("listDGG", dotGiamGiaService.getAll());
                return "dotgiamgia/index";
            }

            dotGiamGiaService.save(dgg);
            return "redirect:/dot-giam-gia/hien-thi";
        }




    }

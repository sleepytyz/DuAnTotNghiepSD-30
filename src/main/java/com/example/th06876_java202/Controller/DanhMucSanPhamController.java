    package com.example.th06876_java202.Controller;

    import com.example.th06876_java202.Entity.DanhMucSanPham;
    import com.example.th06876_java202.Service.DanhMucSanPhamService;
    import jakarta.validation.Valid;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.PageRequest;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.validation.Errors;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.servlet.mvc.support.RedirectAttributes;

    import java.util.List;

    @Controller
    @RequestMapping("/danhmucsp")
    public class DanhMucSanPhamController {

        private final DanhMucSanPhamService danhMucSanPhamService;

        public DanhMucSanPhamController( DanhMucSanPhamService danhMucSanPhamService ){
            this.danhMucSanPhamService = danhMucSanPhamService;
        }

        @GetMapping("/index")
        public String index(
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "5") int size,
                Model model) {

            Page<DanhMucSanPham> pageData =
                    danhMucSanPhamService.getallpage(PageRequest.of(page, size));

            model.addAttribute("listdmsp", pageData.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", pageData.getTotalPages());
            model.addAttribute("danhmuc", new DanhMucSanPham());

            return "danhmucsp/index";
        }

        @GetMapping("/edit/{id}")
        public String edit(@PathVariable("id") int id, Model model) {
            DanhMucSanPham dmsp = danhMucSanPhamService.findById(id).orElse(null);
            model.addAttribute("danhmuc", dmsp);
            List<DanhMucSanPham> listdmsp = danhMucSanPhamService.getAll();
            model.addAttribute("listdmsp", listdmsp);
            return "danhmucsp/index";
        }

        @PostMapping("/add")
        public String add(@ModelAttribute("danhmuc") @Valid DanhMucSanPham dmsp, Errors errors, Model model, RedirectAttributes redirectAttributes) {
            if (dmsp.getTenDanhMuc() != null) {
                dmsp.setTenDanhMuc(dmsp.getTenDanhMuc().trim());
            }

            if (errors.hasErrors()) {
                Page<DanhMucSanPham> pageData = danhMucSanPhamService.getallpage(PageRequest.of(0, 5));
                model.addAttribute("listdmsp", pageData.getContent());
                model.addAttribute("currentPage", 0);
                model.addAttribute("totalPages", pageData.getTotalPages());
                return "danhmucsp/index";
            }

            if (danhMucSanPhamService.ktraten(dmsp.getTenDanhMuc())){
                redirectAttributes.addFlashAttribute("mess", "Tên danh mục đã tồn tại");
                return "redirect:/danhmucsp/index";
            }

            dmsp.setTrangThai(true);
            danhMucSanPhamService.them(dmsp);
            redirectAttributes.addFlashAttribute("successMess", "Thêm mới danh mục thành công!");
            return "redirect:/danhmucsp/index";
        }

        @GetMapping("/capnhatt/{id}")
        public String capnhatt(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
            try {
                DanhMucSanPham dmsp = danhMucSanPhamService.doiTrangThai(id);

                if (dmsp != null) {
                    if (dmsp.isTrangThai()) {
                        redirectAttributes.addFlashAttribute("successMess", "Mở hoạt động thành công");
                    } else {
                        redirectAttributes.addFlashAttribute("successMess", "Ngừng hoạt động thành công");
                    }
                } else {
                    redirectAttributes.addFlashAttribute("mess", "Không tìm thấy danh mục sản phẩm!");
                }
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("mess", "Cập nhật trạng thái thất bại: " + e.getMessage());
            }
            return "redirect:/danhmucsp/index";
        }

    }

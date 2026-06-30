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

@Controller
@RequestMapping("/danhmucsp")
public class DanhMucSanPhamController {

    private final DanhMucSanPhamService danhMucSanPhamService;

    public DanhMucSanPhamController(DanhMucSanPhamService danhMucSanPhamService) {
        this.danhMucSanPhamService = danhMucSanPhamService;
    }

    @GetMapping("/index")
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Page<DanhMucSanPham> pageData = danhMucSanPhamService.getallpage(PageRequest.of(page, size));

        model.addAttribute("listdmsp", pageData.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalItems", pageData.getTotalElements());

        // Tạo mã tự động khi vào index
        String generatedCode = danhMucSanPhamService.generateMaDanhMuc();

        if (!model.containsAttribute("danhmuc")) {
            DanhMucSanPham newDanhMuc = new DanhMucSanPham();
            newDanhMuc.setMaDanhMuc(generatedCode);
            model.addAttribute("danhmuc", newDanhMuc);
        } else {
            DanhMucSanPham existing = (DanhMucSanPham) model.getAttribute("danhmuc");
            if (existing != null && (existing.getMaDanhMuc() == null || existing.getMaDanhMuc().isEmpty())) {
                existing.setMaDanhMuc(generatedCode);
            }
        }

        return "danhmucsp/index";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") String id, Model model) {
        DanhMucSanPham dmsp = danhMucSanPhamService.findById(id).orElse(null);
        model.addAttribute("danhmuc", dmsp);

        Page<DanhMucSanPham> pageData = danhMucSanPhamService.getallpage(PageRequest.of(0, 5));
        model.addAttribute("listdmsp", pageData.getContent());
        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalItems", pageData.getTotalElements());

        return "danhmucsp/index";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("danhmuc") @Valid DanhMucSanPham dmsp,
                      Errors errors,
                      Model model,
                      RedirectAttributes redirectAttributes) {

        // Chuẩn hóa tên
        String normalizedTen = danhMucSanPhamService.normalizeTenDanhMuc(dmsp.getTenDanhMuc());
        dmsp.setTenDanhMuc(normalizedTen);

        System.out.println("=== ADD DANH MUC ===");
        System.out.println("Ten: " + dmsp.getTenDanhMuc());
        System.out.println("Errors: " + errors.hasErrors());

        if (errors.hasErrors()) {
            String newCode = danhMucSanPhamService.generateMaDanhMuc();
            dmsp.setMaDanhMuc(newCode);

            Page<DanhMucSanPham> pageData = danhMucSanPhamService.getallpage(PageRequest.of(0, 5));
            model.addAttribute("listdmsp", pageData.getContent());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", pageData.getTotalPages());
            model.addAttribute("totalItems", pageData.getTotalElements());
            model.addAttribute("danhmuc", dmsp);
            return "danhmucsp/index";
        }

        // Kiểm tra tên đã tồn tại
        if (danhMucSanPhamService.existsByTenDanhMuc(dmsp.getTenDanhMuc())) {
            String newCode = danhMucSanPhamService.generateMaDanhMuc();
            dmsp.setMaDanhMuc(newCode);

            System.out.println(" Tên đã tồn tại: " + dmsp.getTenDanhMuc());
            redirectAttributes.addFlashAttribute("mess", " Danh mục '" + dmsp.getTenDanhMuc() + "' đã tồn tại!");
            redirectAttributes.addFlashAttribute("danhmuc", dmsp);
            return "redirect:/danhmucsp/index";
        }

        if (dmsp.getMaDanhMuc() == null || dmsp.getMaDanhMuc().isEmpty()) {
            String newCode = danhMucSanPhamService.generateMaDanhMuc();
            dmsp.setMaDanhMuc(newCode);
        }

        dmsp.setTrangThai(true);
        danhMucSanPhamService.them(dmsp);

        System.out.println(" Thêm thành công: " + dmsp.getTenDanhMuc());
        redirectAttributes.addFlashAttribute("successMess",
                " Thêm danh mục '" + dmsp.getTenDanhMuc() + "' (mã: " + dmsp.getMaDanhMuc() + ") thành công!");
        return "redirect:/danhmucsp/index";
    }

    @GetMapping("/capnhatt/{id}")
    public String capnhatt(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        try {
            DanhMucSanPham dmsp = danhMucSanPhamService.doiTrangThai(id);

            if (dmsp != null) {
                if (dmsp.isTrangThai()) {
                    redirectAttributes.addFlashAttribute("successMess", " Đã kích hoạt danh mục '" + dmsp.getTenDanhMuc() + "'!");
                } else {
                    redirectAttributes.addFlashAttribute("successMess", " Đã ngừng hoạt động danh mục '" + dmsp.getTenDanhMuc() + "'!");
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
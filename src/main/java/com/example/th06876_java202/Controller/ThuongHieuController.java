package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.DanhMucSanPham;
import com.example.th06876_java202.Entity.ThuongHieu;
import com.example.th06876_java202.Service.ThuongHieuService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/thuonghieu")
public class ThuongHieuController {

    @Autowired
    private ThuongHieuService thuongHieuService;

    @GetMapping("/index")
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Page<ThuongHieu> pageData = thuongHieuService.getallpage(PageRequest.of(page, size));

        model.addAttribute("listth", pageData.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalItems", pageData.getTotalElements());

        if (!model.containsAttribute("thuonghieu")) {
            model.addAttribute("thuonghieu", new ThuongHieu());
        }
        return "thuonghieu/index";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("thuonghieu") @Valid ThuongHieu thuongHieu,
                      Errors errors,
                      RedirectAttributes redirectAttributes,
                      Model model) {

        String tenmoine = thuongHieu.getTenThuongHieu().trim();
        thuongHieu.setTenThuongHieu(tenmoine);

        if (errors.hasErrors()) {
            Page<ThuongHieu> pageData = thuongHieuService.getallpage(PageRequest.of(0, 5));
            model.addAttribute("listth", pageData.getContent());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", pageData.getTotalPages());
            model.addAttribute("totalItems", pageData.getTotalElements());
            return "thuonghieu/index";
        }

        if (thuongHieuService.ktraten(thuongHieu.getTenThuongHieu())) {
            redirectAttributes.addFlashAttribute("mess", "Thương hiệu này đã tồn tại!");
            return "redirect:/thuonghieu/index";
        }

        thuongHieu.setTrangThai(true);

        thuongHieuService.them(thuongHieu);
        redirectAttributes.addFlashAttribute("successMess", "Thêm thương hiệu thành công!");
        return "redirect:/thuonghieu/index";
    }

    @GetMapping("/capnhatt/{id}")
    public String capnhatt(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            ThuongHieu dmsp = thuongHieuService.doiTrangThai(id);

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
        return "redirect:/thuonghieu/index";
    }
}
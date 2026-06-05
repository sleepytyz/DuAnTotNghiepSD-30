package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.DanhMucSanPham;
import com.example.th06876_java202.Entity.SanPham;
import com.example.th06876_java202.Entity.SanPhamChiTiet;
import com.example.th06876_java202.Service.DanhMucSanPhamService;
import com.example.th06876_java202.Service.SanPhamChiTietService;
import com.example.th06876_java202.Service.SanPhamHinhAnhService;
import com.example.th06876_java202.Service.SanPhamService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/sanphamct")
public class SanPhamChiTietController {

    private final SanPhamChiTietService sanPhamChiTietService;
    private final DanhMucSanPhamService danhMucSanPhamService;
    private final SanPhamService sanPhamService;
    private final SanPhamHinhAnhService sanPhamHinhAnhService;

    public SanPhamChiTietController( SanPhamChiTietService sanPhamChiTietService, DanhMucSanPhamService danhMucSanPhamService, SanPhamService sanPhamService, SanPhamHinhAnhService sanPhamHinhAnhService ) {
        this.sanPhamChiTietService = sanPhamChiTietService;
        this.danhMucSanPhamService = danhMucSanPhamService;
        this.sanPhamService = sanPhamService;
        this.sanPhamHinhAnhService = sanPhamHinhAnhService;
    }

    @GetMapping("/image/{id}")
    @ResponseBody
    public byte[] image(@PathVariable Integer id){
        return sanPhamHinhAnhService
                .findById(id)
                .orElseThrow()
                .getHinhAnh();
    }

    @GetMapping("/index")
    public String index( Model model ) {
        List<SanPhamChiTiet> listspct = sanPhamChiTietService.getall();
        List<SanPham> Listsp = sanPhamService.getAll();
        List<DanhMucSanPham> listdmsp = danhMucSanPhamService.getAll();
        model.addAttribute("listspct", listspct);
        model.addAttribute("listsp", Listsp);
        model.addAttribute("listdmsp", listdmsp);
        model.addAttribute("sanphamct", new SanPhamChiTiet());
        return "sanphamct/index";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") int id, Model model ) {
        SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietService.findbyId(id).orElse(null);
        model.addAttribute("sanphamct", sanPhamChiTiet);
        List<SanPhamChiTiet> listspct = sanPhamChiTietService.getall();
        List<SanPham> Listsp = sanPhamService.getAll();
        List<DanhMucSanPham> listdmsp = danhMucSanPhamService.getAll();
        model.addAttribute("listspct", listspct);
        model.addAttribute("listsp", Listsp);
        model.addAttribute("listdmsp", listdmsp);
        return "sanphamct/index";
    }

    @PostMapping("/add")
    public String add (@ModelAttribute("sanphamct")@Valid SanPhamChiTiet sanPhamChiTiet, Errors errors, Model model) {
        if(errors.hasErrors()) {
            List<SanPhamChiTiet> listspct = sanPhamChiTietService.getall();
            List<SanPham> Listsp = sanPhamService.getAll();
            List<DanhMucSanPham> listdmsp = danhMucSanPhamService.getAll();
            model.addAttribute("listspct", listspct);
            model.addAttribute("listsp", Listsp);
            model.addAttribute("listdmsp", listdmsp);
            return "sanphamct/index";
        }
        sanPhamChiTietService.capNhatTrangThai(sanPhamChiTiet);
        sanPhamChiTiet.setNgayTao(LocalDate.now());
        sanPhamChiTietService.them(sanPhamChiTiet);
        return "redirect:/sanphamct/index";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute("sanphamct")@Valid SanPhamChiTiet sanPhamChiTiet, Errors errors, Model model){
        if(errors.hasErrors()) {
            List<SanPhamChiTiet> listspct = sanPhamChiTietService.getall();
            List<SanPham> Listsp = sanPhamService.getAll();
            List<DanhMucSanPham> listdmsp = danhMucSanPhamService.getAll();
            model.addAttribute("listspct", listspct);
            model.addAttribute("listsp", Listsp);
            model.addAttribute("listdmsp", listdmsp);
            return "sanphamct/index";
        }
        sanPhamChiTietService.capNhatTrangThai(sanPhamChiTiet);
        sanPhamChiTiet.setNgayCapNhat(LocalDate.now());
        sanPhamChiTietService.them(sanPhamChiTiet);
        return "redirect:/sanphamct/index";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") int id, Model model) {
        sanPhamChiTietService.suaSanPham(id);
        return "redirect:/sanphamct/index";
    }

}

package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.ChatLieu;
import com.example.th06876_java202.Entity.DanhMucSanPham;
import com.example.th06876_java202.Entity.SanPham;
import com.example.th06876_java202.Service.ChatLieuService;
import com.example.th06876_java202.Service.DanhMucSanPhamService;
import com.example.th06876_java202.Service.SanPhamChiTietService;
import com.example.th06876_java202.Service.SanPhamService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/sanpham")
public class SanPhamController {

    private final DanhMucSanPhamService danhMucSanPhamService;
    private final SanPhamService sanPhamService;
    private final ChatLieuService chatLieuService;
    private final SanPhamChiTietService sanPhamChiTietService;

    public SanPhamController( DanhMucSanPhamService danhMucSanPhamService, SanPhamService sanPhamService, ChatLieuService chatLieuService,
                              SanPhamChiTietService sanPhamChiTietService) {
        this.danhMucSanPhamService = danhMucSanPhamService;
        this.sanPhamService = sanPhamService;
        this.chatLieuService = chatLieuService;
        this.sanPhamChiTietService = sanPhamChiTietService;
    }


    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("activeMenu", "sanpham");
        List<SanPham> Listsp = sanPhamService.getAll();
        List<DanhMucSanPham> listdmsp = danhMucSanPhamService.getAll();
        List<ChatLieu> listclieu = chatLieuService.findAll();
        model.addAttribute("listsp", Listsp);
        model.addAttribute("listdmsp", listdmsp);
        model.addAttribute("listcl", listclieu);
        model.addAttribute("sanpham", new SanPham());
        return "sanpham/index";
    }


    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") int id, Model model) {
        SanPham sp = sanPhamService.findById(id).orElse(null);
        model.addAttribute("sanpham", sp);

        List<SanPham> Listsp = sanPhamService.getAll();
        List<DanhMucSanPham> listdmsp = danhMucSanPhamService.getAll();
        model.addAttribute("listsp", Listsp);
        model.addAttribute("listdmsp", listdmsp);
        List<ChatLieu> listclieu = chatLieuService.findAll();
        model.addAttribute("listcl", listclieu);
        model.addAttribute("showModal", true);
        model.addAttribute("isEdit", true);
        return "sanpham/index";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute("sanpham") @Valid SanPham sanpham,
                         Errors errors,
                         Model model) {

        if (errors.hasErrors()) {
            model.addAttribute("listsp", sanPhamService.getAll());
            model.addAttribute("listdmsp", danhMucSanPhamService.getAll());
            List<ChatLieu> listclieu = chatLieuService.findAll();
            model.addAttribute("listcl", listclieu);
            model.addAttribute("showModal", true);
            model.addAttribute("isEdit", true);

            return "sanpham/index";
        }
        SanPham spOld = sanPhamService.findById(sanpham.getMaSanPham()).orElseThrow();

        Integer macl = sanpham.getChatLieu().getMaChatLieu();

        ChatLieu cll = chatLieuService.findById(macl).orElseThrow();

        sanpham.setNgayTao(spOld.getNgayTao());
        sanpham.setNgayCapNhat(LocalDate.now());

        spOld.setTenSanPham(sanpham.getTenSanPham());
        spOld.setDanhMucSanPham(sanpham.getDanhMucSanPham());
        spOld.setMoTa(sanpham.getMoTa());
        spOld.setChatLieu(cll);
        Boolean ttc = spOld.getTrangThai();

        Boolean trangThaiMoi = sanpham.getTrangThai();

        if (!ttc.equals(trangThaiMoi)) {

            if (!trangThaiMoi) {

                sanPhamChiTietService.suaSanPham2(
                        sanpham.getMaSanPham());

            } else {

                sanPhamChiTietService.suaSanPham3(
                        sanpham.getMaSanPham());

            }
        }

        spOld.setTrangThai(sanpham.getTrangThai());
        spOld.setNgayCapNhat(LocalDate.now());

        sanPhamService.save(spOld);

        return "redirect:/sanpham/index";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("sanpham")@Valid SanPham sanpham, Errors errors, Model model , RedirectAttributes redirectAttributes) {
        if(errors.hasErrors()) {
            List<SanPham> Listsp = sanPhamService.getAll();
            List<DanhMucSanPham> listdmsp = danhMucSanPhamService.getAll();
            model.addAttribute("listsp", Listsp);
            List<ChatLieu> listclieu = chatLieuService.findAll();
            model.addAttribute("listcl", listclieu);
            model.addAttribute("listdmsp", listdmsp);
            model.addAttribute("showModal", true);
            return "sanpham/index";
        }
        if (sanPhamService.existsByTenSanPham(sanpham.getTenSanPham())) {
            redirectAttributes.addFlashAttribute("mess", "Tên sản phẩm đã tồn tại");
            return "redirect:/sanpham/index";
        }
        sanpham.setNgayTao(LocalDate.now());
        sanPhamService.save(sanpham);
        return "redirect:/sanpham/index";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") int id, Model model) {
        sanPhamService.suaSanPham(id);
        sanPhamChiTietService.suaSanPham2(id);

        return "redirect:/sanpham/index";
    }

    @GetMapping("/locdmsp")
    public String locdmsp(@RequestParam("maDanhMuc") Integer madm,
                          Model model) {

        List<SanPham> listsp = sanPhamService.findByMadm(madm);
        List<DanhMucSanPham> listdmsp = danhMucSanPhamService.getAll();
        List<ChatLieu> listclieu = chatLieuService.findAll();
        model.addAttribute("listcl", listclieu);
        model.addAttribute("listsp", listsp);
        model.addAttribute("listdmsp", listdmsp);
        model.addAttribute("sanpham", new SanPham());

        return "sanpham/index";
    }

    @GetMapping("/timkiem")
    public String timkiem(@RequestParam("t") String hoten, Model model) {

        List<SanPham> listsp = sanPhamService.findBytenhoacma(hoten);
        List<DanhMucSanPham> listdmsp = danhMucSanPhamService.getAll();
        List<ChatLieu> listclieu = chatLieuService.findAll();
        model.addAttribute("listcl", listclieu);
        model.addAttribute("listsp", listsp);
        model.addAttribute("listdmsp", listdmsp);
        model.addAttribute("sanpham", new SanPham());
        return "sanpham/index";
    }

    @GetMapping("/loctt")
    public String loctt(@RequestParam("tt") String tt, Model model) {
        List<SanPham> listsp = sanPhamService.findBytt(tt);
        List<DanhMucSanPham> listdmsp = danhMucSanPhamService.getAll();
        List<ChatLieu> listclieu = chatLieuService.findAll();
        model.addAttribute("listcl", listclieu);
        model.addAttribute("listsp", listsp);
        model.addAttribute("listdmsp", listdmsp);
        model.addAttribute("sanpham", new SanPham());
        return "sanpham/index";
    }



}

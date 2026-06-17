package com.example.th06876_java202.Controller;

import com.example.th06876_java202.Entity.Account;
import com.example.th06876_java202.Entity.DotGiamGia;
import com.example.th06876_java202.Repository.DotGiamGiaRepo;
import com.example.th06876_java202.Repository.NhanVienRepository;
import com.example.th06876_java202.Service.DotGiamGiaService;
import com.example.th06876_java202.Service.NhanVienService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class DotGiamGiaCon {
    @Autowired
    private DotGiamGiaService dotGiamGiaService;

    private NhanVienRepository nhanVienRepository;

    public DotGiamGiaCon(NhanVienRepository nhanVienRepository) {
        this.nhanVienRepository = nhanVienRepository;
    }

    @GetMapping("/dot-giam-gia/hien-thi")
    public String hienThi(Model model) {
        List<DotGiamGia> list = dotGiamGiaService.getAll();
        LocalDate homNay = LocalDate.now();

        // Duyệt qua danh sách, nếu đợt nào hết hạn thì tự chuyển sang Ngừng hoạt động
        for (DotGiamGia dgg : list) {
            if (dgg.getNgayKetThuc() != null && dgg.getNgayKetThuc().isBefore(homNay)) {
                if (dgg.getTrangThai() == null || dgg.getTrangThai()) {
                    dgg.setTrangThai(false);
                    dotGiamGiaService.save(dgg); // Cập nhật lại vào Database
                }
            }
        }

        model.addAttribute("listDGG", list);
        model.addAttribute("dgg", new DotGiamGia()); // Khởi tạo form trống cho nút Thêm
        return "dotgiamgia/index";
    }

    @GetMapping("/dot-giam-gia/detail/{id}")
    public String detail(@PathVariable("id") Integer id, Model model) {

        model.addAttribute("activeMenu", "dotgiamgia");

        model.addAttribute("dgg", dotGiamGiaService.getById(id));
        model.addAttribute("listDGG", dotGiamGiaService.getAll());

        model.addAttribute("showModal", true);

        return "dotgiamgia/index";
    }

    @GetMapping("/dot-giam-gia/delete/{id}")
    public String delete(@PathVariable("id") Integer id, Model model) {
        dotGiamGiaService.suaa(id);
        return "redirect:/dot-giam-gia/hien-thi";
    }

    @PostMapping("/dot-giam-gia/add")
    public String add(@Valid @ModelAttribute("dgg") DotGiamGia dgg,
                      BindingResult result,
                      HttpSession session,
                      Model model) {

        // 1. Kiểm tra logic giá trị giảm
        validateGiaTriGiam(dgg, result);

        // 2. Nếu có lỗi xảy ra (bao gồm cả các lỗi validation khác nếu có)
        if (result.hasErrors()) {
            model.addAttribute("showModal", true); // Kích hoạt mở lại Modal ở giao diện
            model.addAttribute("listDGG", dotGiamGiaService.getAll()); // Thay bằng hàm lấy danh sách của bạn
            return "dotgiamgia/index"; // Đổi thành tên file HTML của bạn (không dùng redirect khi có lỗi)
        }


        dgg.setNgayTao(LocalDate.now());
        dotGiamGiaService.save(dgg);

        return "redirect:/dot-giam-gia/hien-thi";
    }

    @PostMapping("/dot-giam-gia/update")
    public String update(@Valid @ModelAttribute("dgg") DotGiamGia dgg,
                         BindingResult result,
                         Model model) {

        // 1. Kiểm tra logic giá trị giảm khi cập nhật
        validateGiaTriGiam(dgg, result);

        // 2. Nếu sửa dữ liệu không hợp lệ
        if (result.hasErrors()) {
            model.addAttribute("showModal", true); // Giữ mở Modal để hiển thị lỗi đỏ
            model.addAttribute("listDGG", dotGiamGiaService.getAll());
            return "dotgiamgia/index"; // Trả về giao diện hiện tại để hiển thị lỗi
        }

        // 3. Lưu dữ liệu đã cập nhật thành công
        dotGiamGiaService.save(dgg);
        return "redirect:/dot-giam-gia/hien-thi";
    }

    @GetMapping("/dot-giam-gia/loc")
    public String locDuLieu(@RequestParam(value = "searchKeyword", required = false) String searchKeyword,
                            @RequestParam(value = "searchTrangThai", required = false) String searchTrangThai,
                            @RequestParam(value = "searchLoaiGiam", required = false) String searchLoaiGiam,
                            Model model) {

        // Gọi hàm lọc hỗn hợp từ Service
        List<DotGiamGia> ketQuaLoc = dotGiamGiaService.filter(searchKeyword, searchTrangThai, searchLoaiGiam);

        // Đẩy danh sách đã lọc ra bảng dữ liệu ngoài HTML
        model.addAttribute("listDGG", ketQuaLoc);

        // Giữ lại các giá trị ô lọc đã chọn trên giao diện để người dùng biết mình đang lọc gì
        model.addAttribute("searchKeyword", searchKeyword);
        model.addAttribute("searchTrangThai", searchTrangThai);
        model.addAttribute("searchLoaiGiam", searchLoaiGiam);

        // Các dữ liệu khởi tạo mặc định cho Modal Form Thêm/Sửa (để tránh lỗi ThymeLeaf trống đối tượng)
        model.addAttribute("activeMenu", "ctdotgiamgia");
        model.addAttribute("dgg", new DotGiamGia()); // Giả định Tên Object Đợt giảm giá của bạn là DotGiamGia

        return "dotgiamgia/index"; // Trả về trang giao diện chính
    }


    private void validateGiaTriGiam(DotGiamGia dgg, BindingResult result) {
        if (dgg.getGiaTriGiam() == null) {
            result.rejectValue("giaTriGiam", "error.dgg", "Giá trị giảm không được để trống!");
            return;
        }

        if ("Phần trăm".equals(dgg.getLoaiGiamGia())) {
            double val = dgg.getGiaTriGiam().doubleValue();
            if (val < 1 || val > 100) {
                result.rejectValue("giaTriGiam", "error.dgg", "Giá trị giảm theo phần trăm phải nằm trong khoảng từ 1% đến 100%!");
            }
        } else if ("Tiền mặt".equals(dgg.getLoaiGiamGia())) {
            if (dgg.getGiaTriGiam().compareTo(BigDecimal.ZERO) <= 0) {
                result.rejectValue("giaTriGiam", "error.dgg", "Giá trị giảm theo tiền mặt phải lớn hơn 0đ!");
            }
        }
    }
}

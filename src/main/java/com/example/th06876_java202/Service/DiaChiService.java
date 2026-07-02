package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.DiaChi;
import com.example.th06876_java202.Entity.KhachHang;
import com.example.th06876_java202.Repository.DiaChiRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DiaChiService {

    @Autowired
    private DiaChiRepo diaChiRepo;

    // ============ LƯU ĐỊA CHỈ (THÊM MỚI HOẶC CẬP NHẬT) ============
    @Transactional(rollbackFor = Exception.class)
    public DiaChi save(DiaChi diaChi) {
        // Nếu địa chỉ được đặt làm mặc định
        if (Boolean.TRUE.equals(diaChi.getDiaChiMacDinh())) {
            // Reset tất cả địa chỉ khác của khách hàng về không mặc định
            resetDiaChiMacDinh(diaChi.getKhachHang().getMaKH());
        }
        return diaChiRepo.save(diaChi);
    }


    // ============ LẤY DANH SÁCH ĐỊA CHỈ THEO MÃ KHÁCH HÀNG (String) ============
    public List<DiaChi> findByKhachHang_MaKH(String maKH) {
        return diaChiRepo.findByKhachHang_MaKH(maKH);
    }

    // ============ LẤY DANH SÁCH ĐỊA CHỈ THEO ĐỐI TƯỢNG KHÁCH HÀNG ============
    public List<DiaChi> findByKhachHang(KhachHang khachHang) {
        return diaChiRepo.findByKhachHang(khachHang);
    }

    // ============ TÌM ĐỊA CHỈ THEO ID ============
    public Optional<DiaChi> findById(Integer id) {
        return diaChiRepo.findById(id);
    }

    // ============ TÌM ĐỊA CHỈ MẶC ĐỊNH CỦA KHÁCH HÀNG ============
    public DiaChi findDefaultByKhachHang(KhachHang khachHang) {
        return diaChiRepo.findByKhachHangAndDiaChiMacDinh(khachHang, true);
    }



    // ============ XÓA ĐỊA CHỈ THEO ID ============
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Integer id) {
        // Kiểm tra xem địa chỉ có tồn tại không
        Optional<DiaChi> diaChiOpt = diaChiRepo.findById(id);
        if (diaChiOpt.isPresent()) {
            DiaChi diaChi = diaChiOpt.get();
            // Nếu địa chỉ cần xóa là mặc định, cần set địa chỉ khác làm mặc định
            if (Boolean.TRUE.equals(diaChi.getDiaChiMacDinh())) {
                String maKH = diaChi.getKhachHang().getMaKH();
                // Tìm địa chỉ khác của cùng khách hàng
                List<DiaChi> otherAddresses = diaChiRepo.findByKhachHang_MaKH(maKH);
                // Loại bỏ địa chỉ đang xóa
                otherAddresses.removeIf(dc -> dc.getMaDiaChi().equals(id));

                // Nếu còn địa chỉ khác, đặt địa chỉ đầu tiên làm mặc định
                if (!otherAddresses.isEmpty()) {
                    DiaChi newDefault = otherAddresses.get(0);
                    newDefault.setDiaChiMacDinh(true);
                    diaChiRepo.save(newDefault);
                }
            }
            diaChiRepo.deleteById(id);
        }
    }

    // ============ XÓA TẤT CẢ ĐỊA CHỈ CỦA KHÁCH HÀNG ============
    @Transactional(rollbackFor = Exception.class)
    public void deleteAllByKhachHang(String maKH) {
        List<DiaChi> list = diaChiRepo.findByKhachHang_MaKH(maKH);
        if (!list.isEmpty()) {
            diaChiRepo.deleteAll(list);
        }
    }

    // ============ KIỂM TRA KHÁCH HÀNG CÓ ĐỊA CHỈ KHÔNG ============
    public boolean hasAddress(String maKH) {
        List<DiaChi> list = diaChiRepo.findByKhachHang_MaKH(maKH);
        return list != null && !list.isEmpty();
    }

    // ============ ĐẾM SỐ LƯỢNG ĐỊA CHỈ CỦA KHÁCH HÀNG ============
    public long countByKhachHang(String maKH) {
        return diaChiRepo.countByKhachHang_MaKH(maKH);
    }

    // ============ CẬP NHẬT ĐỊA CHỈ ============
    @Transactional(rollbackFor = Exception.class)
    public DiaChi update(Integer maDiaChi, DiaChi diaChiUpdate) {
        Optional<DiaChi> optional = diaChiRepo.findById(maDiaChi);
        if (optional.isPresent()) {
            DiaChi existing = optional.get();

            // Cập nhật thông tin
            if (diaChiUpdate.getTenNguoiNhan() != null) {
                existing.setTenNguoiNhan(diaChiUpdate.getTenNguoiNhan());
            }
            if (diaChiUpdate.getSoDienThoaiNguoiNhan() != null) {
                existing.setSoDienThoaiNguoiNhan(diaChiUpdate.getSoDienThoaiNguoiNhan());
            }
            if (diaChiUpdate.getDiaChiCuThe() != null) {
                existing.setDiaChiCuThe(diaChiUpdate.getDiaChiCuThe());
            }
            if (diaChiUpdate.getPhuongXa() != null) {
                existing.setPhuongXa(diaChiUpdate.getPhuongXa());
            }
            if (diaChiUpdate.getQuanHuyen() != null) {
                existing.setQuanHuyen(diaChiUpdate.getQuanHuyen());
            }
            if (diaChiUpdate.getTinhThanh() != null) {
                existing.setTinhThanh(diaChiUpdate.getTinhThanh());
            }

            // Xử lý địa chỉ mặc định
            if (Boolean.TRUE.equals(diaChiUpdate.getDiaChiMacDinh())) {
                resetDiaChiMacDinh(existing.getKhachHang().getMaKH());
                existing.setDiaChiMacDinh(true);
            } else if (diaChiUpdate.getDiaChiMacDinh() != null) {
                existing.setDiaChiMacDinh(false);
            }

            return diaChiRepo.save(existing);
        }
        return null;
    }

    public DiaChi findByKhachHangAndDiaChiMacDinh(KhachHang khachHang, Boolean macDinh) {
        return diaChiRepo.findByKhachHangAndDiaChiMacDinh(khachHang, macDinh);
    }

    // ============ TÌM ĐỊA CHỈ THEO SỐ ĐIỆN THOẠI ============
    public List<DiaChi> findBySoDienThoai(String soDienThoai) {
        return diaChiRepo.findBySoDienThoaiNguoiNhanContaining(soDienThoai);
    }

    // ============ TÌM ĐỊA CHỈ THEO TÊN NGƯỜI NHẬN ============
    public List<DiaChi> findByTenNguoiNhan(String tenNguoiNhan) {
        return diaChiRepo.findByTenNguoiNhanContaining(tenNguoiNhan);
    }

    @Transactional
    public void resetDiaChiMacDinh(String maKH) {
        List<DiaChi> diaChiList = diaChiRepo.findByKhachHang_MaKH(maKH);
        for (DiaChi dc : diaChiList) {
            if (dc.getDiaChiMacDinh() != null && dc.getDiaChiMacDinh()) {
                dc.setDiaChiMacDinh(false);
                diaChiRepo.save(dc);
            }
        }
    }

    // Tìm địa chỉ mặc định của khách hàng
    public DiaChi findDefaultByMaKH(String maKH) {
        List<DiaChi> list = diaChiRepo.findByKhachHang_MaKH(maKH);
        for (DiaChi dc : list) {
            if (dc.getDiaChiMacDinh() != null && dc.getDiaChiMacDinh()) {
                return dc;
            }
        }
        return null;
    }
}
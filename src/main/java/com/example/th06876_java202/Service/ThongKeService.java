package com.example.th06876_java202.Service;

import com.example.th06876_java202.Entity.ThongKeDoanhThuDTO;
import com.example.th06876_java202.Entity.ThongKeTheoThangDTO;
import com.example.th06876_java202.Entity.ThongKeTongQuanDTO;
import com.example.th06876_java202.Repository.HoaDonRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ThongKeService {
    private final HoaDonRepo hoaDonRepository;

    // Thống kê doanh thu theo ngày
    public List<ThongKeDoanhThuDTO> thongKeDoanhThuTheoNgay(
            LocalDate startDate, LocalDate endDate) {

        List<Object[]> results = hoaDonRepository.thongKeDoanhThuTheoNgay(startDate, endDate);
        List<ThongKeDoanhThuDTO> list = new ArrayList<>();

        if (results != null) {
            for (Object[] row : results) {
                if (row != null && row.length >= 4) {
                    try {
                        ThongKeDoanhThuDTO dto = new ThongKeDoanhThuDTO();

                        Object ngayObj = row[0];
                        if (ngayObj != null) {
                            if (ngayObj instanceof LocalDate) {
                                dto.setNgay((LocalDate) ngayObj);
                            } else if (ngayObj instanceof java.sql.Date) {
                                dto.setNgay(((java.sql.Date) ngayObj).toLocalDate());
                            } else if (ngayObj instanceof java.sql.Timestamp) {
                                dto.setNgay(((java.sql.Timestamp) ngayObj).toLocalDateTime().toLocalDate());
                            }
                        }

                        dto.setSoDonHang(row[1] != null ? ((Number) row[1]).intValue() : 0);
                        dto.setDoanhThu(row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO);
                        dto.setTrungBinhDon(row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO);

                        // CHỈ THÊM VÀO LIST NẾU NGÀY KHÔNG NULL
                        if (dto.getNgay() != null) {
                            list.add(dto);
                        }
                    } catch (Exception e) {
                        // Bỏ qua lỗi
                    }
                }
            }
        }
        return list;
    }

    // Thống kê doanh thu theo tháng
    public List<ThongKeTheoThangDTO> thongKeDoanhThuTheoThang(
            LocalDate startDate, LocalDate endDate) {

        List<Object[]> results = hoaDonRepository.thongKeDoanhThuTheoThang(startDate, endDate);
        List<ThongKeTheoThangDTO> list = new ArrayList<>();

        if (results != null) {
            for (Object[] row : results) {
                if (row != null && row.length >= 4) {
                    try {
                        ThongKeTheoThangDTO dto = new ThongKeTheoThangDTO();
                        dto.setNam(row[0] != null ? ((Number) row[0]).intValue() : 0);
                        dto.setThang(row[1] != null ? ((Number) row[1]).intValue() : 0);
                        dto.setSoDonHang(row[2] != null ? ((Number) row[2]).intValue() : 0);
                        dto.setDoanhThu(row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO);
                        list.add(dto);
                    } catch (Exception e) {
                        // Bỏ qua lỗi
                    }
                }
            }
        }
        return list;
    }

    // Thống kê tổng quan
    public ThongKeTongQuanDTO thongKeTongQuan() {
        try {
            List<Object[]> results = hoaDonRepository.thongKeTongQuan();

            if (results == null || results.isEmpty()) {
                return new ThongKeTongQuanDTO();
            }

            Object[] row = results.get(0);
            if (row == null || row.length < 5) {
                return new ThongKeTongQuanDTO();
            }

            ThongKeTongQuanDTO dto = new ThongKeTongQuanDTO();

            if (row[0] != null) {
                dto.setTongDonHang(((Number) row[0]).intValue());
            }

            if (row[1] != null) {
                dto.setTongDoanhThu((BigDecimal) row[1]);
            }

            if (row[2] != null) {
                dto.setTrungBinhDon((BigDecimal) row[2]);
            }

            // Xử lý ngày
            Object ngayDauObj = row[3];
            if (ngayDauObj != null) {
                if (ngayDauObj instanceof LocalDate) {
                    dto.setNgayDau((LocalDate) ngayDauObj);
                } else if (ngayDauObj instanceof java.sql.Date) {
                    dto.setNgayDau(((java.sql.Date) ngayDauObj).toLocalDate());
                } else if (ngayDauObj instanceof java.sql.Timestamp) {
                    dto.setNgayDau(((java.sql.Timestamp) ngayDauObj).toLocalDateTime().toLocalDate());
                } else if (ngayDauObj instanceof java.time.LocalDateTime) {
                    dto.setNgayDau(((java.time.LocalDateTime) ngayDauObj).toLocalDate());
                }
            }

            Object ngayCuoiObj = row[4];
            if (ngayCuoiObj != null) {
                if (ngayCuoiObj instanceof LocalDate) {
                    dto.setNgayCuoi((LocalDate) ngayCuoiObj);
                } else if (ngayCuoiObj instanceof java.sql.Date) {
                    dto.setNgayCuoi(((java.sql.Date) ngayCuoiObj).toLocalDate());
                } else if (ngayCuoiObj instanceof java.sql.Timestamp) {
                    dto.setNgayCuoi(((java.sql.Timestamp) ngayCuoiObj).toLocalDateTime().toLocalDate());
                } else if (ngayCuoiObj instanceof java.time.LocalDateTime) {
                    dto.setNgayCuoi(((java.time.LocalDateTime) ngayCuoiObj).toLocalDate());
                }
            }

            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            return new ThongKeTongQuanDTO();
        }
    }
}
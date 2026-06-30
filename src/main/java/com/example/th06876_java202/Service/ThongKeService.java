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


    public List<ThongKeDoanhThuDTO> thongKeDoanhThuTheoNgay(
            LocalDate startDate, LocalDate endDate) {

        List<Object[]> results = hoaDonRepository.thongKeDoanhThuTheoNgay(startDate, endDate);
        List<ThongKeDoanhThuDTO> list = new ArrayList<>();

        if (results != null) {
            for (Object[] row : results) {
                if (row != null && row.length >= 4) {
                    try {
                        ThongKeDoanhThuDTO dto = new ThongKeDoanhThuDTO();

                        // Ngày
                        Object ngayObj = row[0];
                        if (ngayObj != null) {
                            if (ngayObj instanceof LocalDate) {
                                dto.setNgay((LocalDate) ngayObj);
                            } else if (ngayObj instanceof java.sql.Date) {
                                dto.setNgay(((java.sql.Date) ngayObj).toLocalDate());
                            } else if (ngayObj instanceof String) {
                                dto.setNgay(LocalDate.parse((String) ngayObj));
                            }
                        }

                        // Số đơn hàng
                        if (row[1] != null) {
                            dto.setSoDonHang(((Number) row[1]).intValue());
                        }

                        // Doanh thu
                        if (row[2] != null) {
                            dto.setDoanhThu((BigDecimal) row[2]);
                        }

                        // Trung bình đơn
                        if (row[3] != null) {
                            dto.setTrungBinhDon((BigDecimal) row[3]);
                        }

                        if (dto.getNgay() != null) {
                            list.add(dto);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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

                        // Năm
                        if (row[0] != null) {
                            dto.setNam(((Number) row[0]).intValue());
                        }

                        // Tháng
                        if (row[1] != null) {
                            dto.setThang(((Number) row[1]).intValue());
                        }

                        // Số đơn hàng
                        if (row[2] != null) {
                            dto.setSoDonHang(((Number) row[2]).intValue());
                        }

                        // Doanh thu
                        if (row[3] != null) {
                            dto.setDoanhThu((BigDecimal) row[3]);
                        }

                        list.add(dto);
                    } catch (Exception e) {
                        e.printStackTrace();
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

            // Tổng đơn hàng
            if (row[0] != null) {
                dto.setTongDonHang(((Number) row[0]).intValue());
            }

            // Tổng doanh thu
            if (row[1] != null) {
                dto.setTongDoanhThu((BigDecimal) row[1]);
            }

            // Trung bình đơn
            if (row[2] != null) {
                dto.setTrungBinhDon((BigDecimal) row[2]);
            }

            // Ngày đầu
            Object ngayDauObj = row[3];
            if (ngayDauObj != null) {
                if (ngayDauObj instanceof LocalDate) {
                    dto.setNgayDau((LocalDate) ngayDauObj);
                } else if (ngayDauObj instanceof java.sql.Date) {
                    dto.setNgayDau(((java.sql.Date) ngayDauObj).toLocalDate());
                } else if (ngayDauObj instanceof String) {
                    dto.setNgayDau(LocalDate.parse((String) ngayDauObj));
                }
            }

            // Ngày cuối
            Object ngayCuoiObj = row[4];
            if (ngayCuoiObj != null) {
                if (ngayCuoiObj instanceof LocalDate) {
                    dto.setNgayCuoi((LocalDate) ngayCuoiObj);
                } else if (ngayCuoiObj instanceof java.sql.Date) {
                    dto.setNgayCuoi(((java.sql.Date) ngayCuoiObj).toLocalDate());
                } else if (ngayCuoiObj instanceof String) {
                    dto.setNgayCuoi(LocalDate.parse((String) ngayCuoiObj));
                }
            }

            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            return new ThongKeTongQuanDTO();
        }
    }
}
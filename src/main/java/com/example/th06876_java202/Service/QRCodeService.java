package com.example.th06876_java202.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class QRCodeService {

    private static final String QR_DIR = "qrcodes/";
    private static final int QR_WIDTH = 300;
    private static final int QR_HEIGHT = 300;

    /**
     * Tạo QR Code từ text và lưu vào file
     */
    public String generateQRCode(String text, String fileName) throws WriterException, IOException {
        // Tạo thư mục nếu chưa tồn tại
        Path qrPath = Paths.get(QR_DIR);
        if (!Files.exists(qrPath)) {
            Files.createDirectories(qrPath);
        }

        // Tạo QR Code
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT);

        // Lưu file
        String fullFileName = fileName + ".png";
        Path filePath = qrPath.resolve(fullFileName);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", filePath);

        return "/qrcodes/" + fullFileName;
    }

    /**
     * Tạo QR Code và trả về dạng byte array (để tải về)
     */
    public byte[] generateQRCodeAsBytes(String text) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        return outputStream.toByteArray();
    }

    /**
     * Tạo QR Code cho biến thể sản phẩm
     */
    public String generateVariantQRCode(String maBienThe) throws WriterException, IOException {
        // Nội dung QR: URL đến trang chi tiết sản phẩm
        String qrContent = "https://fsshop.com/sanpham/detail/" + maBienThe;
        // Hoặc dùng localhost khi phát triển:
        // String qrContent = "http://localhost:8080/sanpham/detail/" + maBienThe;

        String fileName = "qr_" + maBienThe + "_" + System.currentTimeMillis();
        return generateQRCode(qrContent, fileName);
    }

    /**
     * Xóa QR Code cũ
     */
    public void deleteQRCode(String fileName) throws IOException {
        Path filePath = Paths.get(QR_DIR + fileName);
        Files.deleteIfExists(filePath);
    }
}
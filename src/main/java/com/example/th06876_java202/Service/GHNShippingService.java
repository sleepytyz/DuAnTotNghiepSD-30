package com.example.th06876_java202.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
public class GHNShippingService {

    @Value("${ghn.api.url}")
    private String apiUrl;

    @Value("${ghn.api.token}")
    private String apiToken;

    @Value("${ghn.shop.id}")
    private String shopId;

    @Value("${ghn.shop.district.id}")
    private int shopDistrictId;

    @Value("${ghn.shop.ward.code}")
    private String shopWardCode;

    @Value("${ghn.shop.province.id}")
    private int shopProvinceId;

    // Map tên quận/huyện sang mã GHN (nếu không có API)
    private static final Map<String, Integer> DISTRICT_NAME_TO_ID = new HashMap<>();
    private static final Map<String, String> WARD_NAME_TO_CODE = new HashMap<>();

    static {
        // Hà Nội - Quận Ba Đình
        DISTRICT_NAME_TO_ID.put("Ba Đình", 1442);
        DISTRICT_NAME_TO_ID.put("Hoàn Kiếm", 1444);
        DISTRICT_NAME_TO_ID.put("Tây Hồ", 1446);
        DISTRICT_NAME_TO_ID.put("Cầu Giấy", 1448);
        DISTRICT_NAME_TO_ID.put("Đống Đa", 1450);
        DISTRICT_NAME_TO_ID.put("Hai Bà Trưng", 1452);
        DISTRICT_NAME_TO_ID.put("Hoàng Mai", 1454);
        DISTRICT_NAME_TO_ID.put("Thanh Xuân", 1456);
        DISTRICT_NAME_TO_ID.put("Long Biên", 1458);
        DISTRICT_NAME_TO_ID.put("Bắc Từ Liêm", 1460);
        DISTRICT_NAME_TO_ID.put("Nam Từ Liêm", 1462);
        DISTRICT_NAME_TO_ID.put("Hà Đông", 1464);
        // Thêm các quận/huyện khác nếu cần

        // Phường/xã - chỉ thêm một số ví dụ
        WARD_NAME_TO_CODE.put("Điện Biên", "1A0401");
        WARD_NAME_TO_CODE.put("Cống Vị", "1A0404");
        WARD_NAME_TO_CODE.put("Ngọc Khánh", "1A0407");
        WARD_NAME_TO_CODE.put("Giảng Võ", "1A0410");
        WARD_NAME_TO_CODE.put("Kim Mã", "1A0413");
        WARD_NAME_TO_CODE.put("Thành Công", "1A0416");
        WARD_NAME_TO_CODE.put("Trúc Bạch", "1A0112");
        WARD_NAME_TO_CODE.put("Quán Thánh", "1A0115");
    }

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GHNShippingService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Lấy phí ship mặc định với service_type_id = 2 (tiết kiệm)
     */
    public BigDecimal calculateShippingFeeDefault(String toDistrict, String toWard,
                                                  int weight, BigDecimal totalAmount) {
        return calculateShippingFee(toDistrict, toWard, weight, totalAmount, 2);
    }

    public BigDecimal calculateShippingFee(String toDistrict, String toWard,
                                           int weight, BigDecimal totalAmount,
                                           int serviceTypeId) {
        try {
            System.out.println("========== GHN SHIPPING REQUEST ==========");
            System.out.println("📤 To District: " + toDistrict);
            System.out.println("📤 To Ward: " + toWard);
            System.out.println("⚖️ Weight: " + weight + "g");
            System.out.println("💰 Total Amount: " + totalAmount);

            // Chuyển đổi tên quận/huyện sang mã GHN
            int toDistrictId = getDistrictId(toDistrict);
            System.out.println("📍 District ID: " + toDistrictId);

            if (toDistrictId <= 0) {
                System.err.println("⚠️ Không tìm thấy mã quận: " + toDistrict);
                return calculateFallbackShippingFee(totalAmount, weight);
            }

            // Chuyển đổi tên phường/xã sang mã GHN
            String toWardCode = getWardCode(toWard);
            System.out.println("📍 Ward Code: " + toWardCode);

            if (toWardCode == null || toWardCode.trim().isEmpty()) {
                System.err.println("⚠️ Không tìm thấy mã phường: " + toWard);
                return calculateFallbackShippingFee(totalAmount, weight);
            }

            // Tạo request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("shop_id", Integer.parseInt(shopId));
            requestBody.put("service_type_id", serviceTypeId);
            requestBody.put("from_district_id", shopDistrictId);
            requestBody.put("from_ward_code", shopWardCode);
            requestBody.put("to_district_id", toDistrictId);
            requestBody.put("to_ward_code", toWardCode);
            requestBody.put("weight", weight);
            requestBody.put("length", 30);
            requestBody.put("width", 20);
            requestBody.put("height", 15);

//            if (totalAmount != null && totalAmount.compareTo(BigDecimal.ZERO) > 0) {
//                requestBody.put("insurance_value", totalAmount.intValue());
//            }

            requestBody.put("insurance_value", 0);

            System.out.println("📦 Request Body: " + objectMapper.writeValueAsString(requestBody));

            // Tạo headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Token", apiToken);
            headers.set("ShopId", shopId);

            System.out.println("🔑 Token: " + apiToken.substring(0, 10) + "...");
            System.out.println("🏪 ShopId: " + shopId);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // Gọi API
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            System.out.println("📥 Response Status: " + response.getStatusCode());
            System.out.println("📥 Response Body: " + response.getBody());

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                int code = root.path("code").asInt();
                String message = root.path("message").asText();

                System.out.println("🔍 Code: " + code);
                System.out.println("🔍 Message: " + message);

                if (code == 200) {
                    JsonNode data = root.path("data");
                    if (data != null && !data.isMissingNode()) {
                        int totalFee = data.path("total").asInt();
                        System.out.println("✅ Shipping Fee: " + totalFee + "đ");
                        return BigDecimal.valueOf(totalFee);
                    }
                } else {
                    System.err.println("❌ GHN Error: " + message);
                }
            }

            System.out.println("⚠️ Sử dụng fallback shipping fee");
            return calculateFallbackShippingFee(totalAmount, weight);

        } catch (Exception e) {
            System.err.println("❌ Exception khi tính phí ship:");
            e.printStackTrace();
            return calculateFallbackShippingFee(totalAmount, weight);
        }
    }

    /**
     * Phương thức fallback khi API không hoạt động
     * ĐÃ SỬA: từ private thành public
     */
    public BigDecimal calculateFallbackShippingFee(BigDecimal totalAmount, int weight) {
        BigDecimal baseFee = BigDecimal.valueOf(30000);

        if (totalAmount != null) {
            // 5% giá trị đơn hàng
            baseFee = baseFee.add(totalAmount.multiply(BigDecimal.valueOf(0.05)));
        }

        // 2000đ/kg
        baseFee = baseFee.add(BigDecimal.valueOf((weight / 1000) * 2000));

        // Giới hạn tối đa 100.000đ
        baseFee = baseFee.min(BigDecimal.valueOf(100000));

        // Làm tròn đến hàng nghìn
        baseFee = baseFee.divide(BigDecimal.valueOf(1000), 0, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(1000));

        return baseFee.max(BigDecimal.valueOf(30000));
    }

    // ===== GETTERS =====
    public String getApiToken() {
        return apiToken;
    }

    public int getShopId() {
        return Integer.parseInt(shopId);
    }

    // ===== PRIVATE HELPERS =====
    private int getDistrictId(String districtName) {
        if (districtName == null || districtName.trim().isEmpty()) {
            return 0;
        }

        // Tìm chính xác
        Integer id = DISTRICT_NAME_TO_ID.get(districtName.trim());
        if (id != null) {
            return id;
        }

        // Tìm tương đối (chứa từ khóa)
        for (Map.Entry<String, Integer> entry : DISTRICT_NAME_TO_ID.entrySet()) {
            if (districtName.toLowerCase().contains(entry.getKey().toLowerCase()) ||
                    entry.getKey().toLowerCase().contains(districtName.toLowerCase())) {
                return entry.getValue();
            }
        }

        return 0;
    }

    private String getWardCode(String wardName) {
        if (wardName == null || wardName.trim().isEmpty()) {
            return null;
        }

        // Tìm chính xác
        String code = WARD_NAME_TO_CODE.get(wardName.trim());
        if (code != null) {
            return code;
        }

        // Tìm tương đối (chứa từ khóa)
        for (Map.Entry<String, String> entry : WARD_NAME_TO_CODE.entrySet()) {
            if (wardName.toLowerCase().contains(entry.getKey().toLowerCase()) ||
                    entry.getKey().toLowerCase().contains(wardName.toLowerCase())) {
                return entry.getValue();
            }
        }

        return null;
    }
}
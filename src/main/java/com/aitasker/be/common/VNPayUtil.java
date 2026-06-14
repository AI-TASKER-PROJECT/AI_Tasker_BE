package com.aitasker.be.common;

import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

public final class VNPayUtil {
    private static final String HMAC_SHA512 = "HmacSHA512";

    private VNPayUtil() {
    }

    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance(HMAC_SHA512);
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA512);
            hmac512.init(secretKey);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) {
                hash.append(String.format("%02x", value));
            }
            return hash.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Khong tao duoc chu ky VNPAY", ex);
        }
    }

    public static String getIpAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return normalizeIp(forwardedFor.split(",")[0].trim());
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return normalizeIp(realIp.trim());
        }

        return normalizeIp(request.getRemoteAddr());
    }

    public static String buildQueryString(Map<String, String> params) {
        return sortedEntries(params)
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    public static String buildHashData(Map<String, String> params) {
        return sortedHashableEntries(params)
                .map(entry -> entry.getKey() + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    private static java.util.stream.Stream<Map.Entry<String, String>> sortedHashableEntries(Map<String, String> params) {
        return sortedEntries(params)
                .filter(entry -> entry.getValue() != null && !entry.getValue().isBlank())
                .filter(entry -> !"vnp_SecureHash".equals(entry.getKey()))
                .filter(entry -> !"vnp_SecureHashType".equals(entry.getKey()));
    }

    private static java.util.stream.Stream<Map.Entry<String, String>> sortedEntries(Map<String, String> params) {
        return params.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isBlank())
                .sorted(Comparator.comparing(Map.Entry::getKey));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.US_ASCII);
    }

    private static String normalizeIp(String ipAddress) {
        if ("::1".equals(ipAddress) || "0:0:0:0:0:0:0:1".equals(ipAddress)) {
            return "127.0.0.1";
        }
        return ipAddress;
    }
}

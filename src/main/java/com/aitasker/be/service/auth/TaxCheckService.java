/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/auth/TaxCheckService.java
 * Day la file gi: File service chua nghiep vu chinh, dieu phoi repository va kiem tra luat xu ly cua he thong.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.service.auth;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.common.exception.BadGatewayException;
import com.aitasker.be.common.exception.NotFoundException;
import com.aitasker.be.dto.auth.TaxCheckResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Note: Annotation nay cho Spring quan ly class nhu mot service chua nghiep vu.
@Service
// Note: Annotation nay giup Lombok sinh constructor cho cac dependency final.
@RequiredArgsConstructor
public class TaxCheckService {
    private static final String VIETQR_BUSINESS_API_URL = "https://api.vietqr.io/v2/business/";
    private static final String FOUND_CODE = "00";
    private static final String FOUND_STATUS = "FOUND";
    private static final long CACHE_TTL_MILLIS = 10 * 60 * 1000L;

    private final RestTemplate restTemplate;
    private final Map<String, CachedTaxCheck> successfulChecks = new ConcurrentHashMap<>();

    // Note: Ham `checkTaxCode` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public TaxCheckResponse checkTaxCode(String mst) {
        validateTaxCode(mst);
        String normalizedTaxCode = mst.trim();

        TaxCheckResponse cached = findCached(normalizedTaxCode);
        if (cached != null) {
            return cached;
        }

        Map<String, Object> responseBody = fetchBusiness(normalizedTaxCode);
        if (responseBody == null || !FOUND_CODE.equals(String.valueOf(responseBody.get("code")))) {
            throw new NotFoundException("Khong tim thay doanh nghiep voi ma so thue: " + normalizedTaxCode);
        }

        Object dataValue = responseBody.get("data");
        if (!(dataValue instanceof Map<?, ?> data)) {
            throw new NotFoundException("Khong tim thay doanh nghiep voi ma so thue: " + normalizedTaxCode);
        }

        TaxCheckResponse result = TaxCheckResponse.builder()
                .taxCode(defaultIfBlank(getString(data, "id"), normalizedTaxCode))
                .companyName(getString(data, "name"))
                .address(getString(data, "address"))
                .representative(getString(data, "legalName"))
                .status(FOUND_STATUS)
                .build();
        successfulChecks.put(normalizedTaxCode, new CachedTaxCheck(result, System.currentTimeMillis() + CACHE_TTL_MILLIS));
        return result;
    }

    // Note: Ham `validateTaxCode` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private void validateTaxCode(String mst) {
        if (mst == null || !mst.matches("\\d{10}|\\d{13}")) {
            throw new AppException("Ma so thue khong hop le");
        }
    }

    // Note: Ham `fetchBusiness` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private Map<String, Object> fetchBusiness(String mst) {
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    VIETQR_BUSINESS_API_URL + mst,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );
            return response.getBody();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new NotFoundException("Khong tim thay doanh nghiep voi ma so thue: " + mst);
            }
            throw new BadGatewayException("Khong goi duoc API VietQR");
        } catch (RestClientException ex) {
            throw new BadGatewayException("Khong goi duoc API VietQR");
        }
    }

    // Note: Ham `getString` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String getString(Map<?, ?> source, String key) {
        Object value = source.get(key);
        if (value == null) {
            return null;
        }

        String text = value.toString();
        return text.isBlank() ? null : text;
    }

    // Note: Ham `defaultIfBlank` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private TaxCheckResponse findCached(String mst) {
        CachedTaxCheck cached = successfulChecks.get(mst);
        if (cached == null) {
            return null;
        }
        if (cached.expiresAtMillis() <= System.currentTimeMillis()) {
            successfulChecks.remove(mst, cached);
            return null;
        }
        return cached.response();
    }

    private record CachedTaxCheck(TaxCheckResponse response, long expiresAtMillis) {
    }
}

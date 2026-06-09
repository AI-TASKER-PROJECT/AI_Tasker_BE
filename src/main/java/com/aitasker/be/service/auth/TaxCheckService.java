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

@Service
@RequiredArgsConstructor
public class TaxCheckService {
    private static final String VIETQR_BUSINESS_API_URL = "https://api.vietqr.io/v2/business/";
    private static final String FOUND_CODE = "00";
    private static final String FOUND_STATUS = "FOUND";

    private final RestTemplate restTemplate;

    public TaxCheckResponse checkTaxCode(String mst) {
        validateTaxCode(mst);

        Map<String, Object> responseBody = fetchBusiness(mst);
        if (responseBody == null || !FOUND_CODE.equals(String.valueOf(responseBody.get("code")))) {
            throw new NotFoundException("Khong tim thay doanh nghiep voi ma so thue: " + mst);
        }

        Object dataValue = responseBody.get("data");
        if (!(dataValue instanceof Map<?, ?> data)) {
            throw new NotFoundException("Khong tim thay doanh nghiep voi ma so thue: " + mst);
        }

        return TaxCheckResponse.builder()
                .taxCode(defaultIfBlank(getString(data, "id"), mst))
                .companyName(getString(data, "name"))
                .address(getString(data, "address"))
                .representative(getString(data, "legalName"))
                .status(FOUND_STATUS)
                .build();
    }

    private void validateTaxCode(String mst) {
        if (mst == null || !mst.matches("\\d{10}|\\d{13}")) {
            throw new AppException("Ma so thue khong hop le");
        }
    }

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

    private String getString(Map<?, ?> source, String key) {
        Object value = source.get(key);
        if (value == null) {
            return null;
        }

        String text = value.toString();
        return text.isBlank() ? null : text;
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}

package com.aitasker.be.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxCheckResponse {
    private String taxCode;
    private String companyName;
    private String address;
    private String representative;
    private String status;
}

package com.aitasker.be.dto.admin;

import lombok.Data;

import java.util.List;

@Data
public class StaffRequest {
    private Integer accountId;
    private String specialization;
    private List<Integer> domainIds;
    private List<Integer> skillIds;
}

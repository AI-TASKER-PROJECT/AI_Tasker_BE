package com.aitasker.be.dto.admin;

import lombok.Data;

@Data
public class AccountRequest {
    private String email;
    private String password;
    private String phone;
    private String fullName;
    private String role;
    private String status;
}

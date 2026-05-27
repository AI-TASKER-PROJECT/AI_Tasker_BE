package com.aitasker.be.controller.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
// CHỈ DÀNH ĐỂ TEST TOKEN JWT
public class TestController {
    @GetMapping("/secure")
    public String secure() {
        return "secure ok";
    }
}

/*
 * NOTE FILE: src/main/java/com/aitasker/be/controller/core/ChatbotController.java
 * Day la file gi: File controller nhan request HTTP, goi service phu hop va tra response cho client.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.controller.core;

import com.aitasker.be.dto.core.ChatRequest;
import com.aitasker.be.dto.core.ChatResponse;
import com.aitasker.be.service.core.ChatbotService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Note: Annotation nay bien class thanh REST controller de nhan request va tra JSON.
@RestController
// Note: Annotation nay dat prefix duong dan API cho controller hoac method.
@RequestMapping("/api/chatbot")
// Note: Annotation nay giup Lombok sinh constructor cho cac dependency final.
@RequiredArgsConstructor
// Note: Annotation nay ghi ro endpoint/class nay khong ap dung security scheme mac dinh tren Swagger.
@SecurityRequirements
public class ChatbotController {
    private final ChatbotService chatbotService;

    // Note: Annotation nay khai bao API tao moi hoac gui du lieu bang HTTP POST.
    @PostMapping("/ask")
    // Note: Ham `ask` xu ly mot API endpoint, nhan request, goi service va tra ket qua cho client.
    public ChatResponse ask(@RequestBody ChatRequest request) {
        return chatbotService.ask(request);
    }
}

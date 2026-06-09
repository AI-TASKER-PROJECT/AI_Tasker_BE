package com.aitasker.be.controller.core;

import com.aitasker.be.dto.core.ChatRequest;
import com.aitasker.be.dto.core.ChatResponse;
import com.aitasker.be.service.core.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {
    private final ChatbotService chatbotService;

    @PostMapping("/ask")
    public ChatResponse ask(@RequestBody ChatRequest request) {
        return chatbotService.ask(request);
    }
}

package com.aitasker.be.service.ai;

import com.aitasker.be.dto.sow.GenerateSowRequest;

public interface RagRetrievalService {
    String retrieveContext(GenerateSowRequest request);
}

/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/ai/RagRetrievalService.java
 * Day la file gi: File service chua nghiep vu chinh, dieu phoi repository va kiem tra luat xu ly cua he thong.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.service.ai;

import com.aitasker.be.dto.sow.GenerateSowRequest;

public interface RagRetrievalService {
    String retrieveContext(GenerateSowRequest request);
}

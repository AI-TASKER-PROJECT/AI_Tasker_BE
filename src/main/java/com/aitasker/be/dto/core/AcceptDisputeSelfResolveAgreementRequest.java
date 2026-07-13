package com.aitasker.be.dto.core;

import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AcceptDisputeSelfResolveAgreementRequest {
    private Long acceptedReplyId;
    private String finalAction;
    private String message;
}

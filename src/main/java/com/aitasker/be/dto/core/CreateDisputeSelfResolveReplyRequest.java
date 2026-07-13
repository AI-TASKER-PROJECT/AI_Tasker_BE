package com.aitasker.be.dto.core;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CreateDisputeSelfResolveReplyRequest {
    private String replyType;
    private String proposedAction;
    private String message;
    private LocalDateTime proposedDueAt;
}

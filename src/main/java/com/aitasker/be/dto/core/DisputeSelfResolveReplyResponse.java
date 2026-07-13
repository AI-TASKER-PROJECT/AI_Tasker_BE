package com.aitasker.be.dto.core;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DisputeSelfResolveReplyResponse {
    private Long replyId;
    private Integer disputeId;
    private String actorRole;
    private String actorDisplayName;
    private String replyType;
    private String proposedAction;
    private String message;
    private LocalDateTime proposedDueAt;
    private LocalDateTime createdAt;
}

package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "dispute_self_resolve_replies")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DisputeSelfResolveReplyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reply_id")
    private Long replyId;

    @Column(name = "dispute_id", nullable = false)
    private Integer disputeId;

    @Column(name = "actor_account_id", nullable = false)
    private Integer actorAccountId;

    @Column(name = "actor_role", nullable = false, length = 30)
    private String actorRole;

    @Column(name = "reply_type", nullable = false, length = 50)
    private String replyType;

    @Column(name = "proposed_action", length = 50)
    private String proposedAction;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "proposed_due_at")
    private LocalDateTime proposedDueAt;

    @Column(name = "accepted_reply_id")
    private Long acceptedReplyId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

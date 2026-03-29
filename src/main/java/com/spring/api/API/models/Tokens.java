package com.spring.api.API.models;

import com.spring.api.API.models.DTOs.Tokens.TokenCreateDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table (
        name = "tokens",
        indexes = {
                @Index(name = "ix_tokens_token_hash", columnList = "token_hash"),
                @Index(name = "ix_Tokens_id", columnList = "id"),
                @Index(name = "ix_tokens_assigned_to", columnList = "assigned_to")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_active_token", columnNames = {"assigned_to", "token_type"})
        }
)
public class Tokens {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "token_hash", nullable = false, length = Integer.MAX_VALUE)
    private String tokenHash;

    @CreationTimestamp
    @Column(name = "date_create", nullable = false)
    private OffsetDateTime dateCreate;

    @NotNull
    @Column(name = "expire_at", nullable = false)
    private OffsetDateTime expireAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assigned_to", nullable = false)
    private User assignedTo;

    @NotNull
    @Column(name = "token_type", nullable = false)
    private Short tokenType;

    @NotNull
    @Column(name = "revoked", nullable = false)
    private Boolean revoked;

    public Tokens(TokenCreateDTO tokenCreateDTO){
        this.tokenHash = tokenCreateDTO.getToken_hash();
        this.expireAt = (OffsetDateTime.now().plusHours(1));
        this.assignedTo = tokenCreateDTO.getAssigned_to();
        this.tokenType = tokenCreateDTO.getToken_type();
        this.revoked = tokenCreateDTO.getRevoked();
    }

    public Tokens(){}

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public OffsetDateTime getDateCreate() {
        return dateCreate;
    }

    public void setDateCreate(OffsetDateTime dateCreate) {
        this.dateCreate = dateCreate;
    }

    public OffsetDateTime getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(OffsetDateTime expireAt) {
        this.expireAt = expireAt;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Short getTokenType(int i) {
        return tokenType;
    }

    public void setTokenType(Short tokenType) {
        this.tokenType = tokenType;
    }

    public Boolean getRevoked() {
        return revoked;
    }

    public void setRevoked(Boolean revoked) {
        this.revoked = revoked;
    }
}

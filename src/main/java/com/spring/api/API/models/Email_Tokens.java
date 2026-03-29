package com.spring.api.API.models;

import java.time.OffsetDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "email_tokens")
public class Email_Tokens {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "token_hash", nullable = false)
    private String token_hash;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime create_at;

    @Column(name = "expire_at", nullable = false)
    private OffsetDateTime expire_at;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assigned_to", nullable = false)
    private User assignedTo;

    @Column(name = "token_type", nullable = false)
    private Short tokenType;

    @Column(name = "used", nullable = false)
    private boolean used;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken_hash() {
        return token_hash;
    }

    public void setToken_hash(String token_hash) {
        this.token_hash = token_hash;
    }

    public OffsetDateTime getCreate_at() {
        return create_at;
    }

    public void setCreate_at(OffsetDateTime create_at) {
        this.create_at = create_at;
    }

    public OffsetDateTime getExpire_at() {
        return expire_at;
    }

    public void setExpire_at(OffsetDateTime expire_at) {
        this.expire_at = expire_at;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Short getTokenType() {
        return tokenType;
    }

    public void setTokenType(Short tokenType) {
        this.tokenType = tokenType;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

}


package com.um.springbootprojstructure.auth.domain;

import com.um.springbootprojstructure.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "mfa_challenges",
        indexes = {
            @Index(name = "idx_mfa_pre_auth", columnList = "preAuthToken"),
            @Index(name = "idx_mfa_user_id", columnList = "user_id")
        }
)
public class MfaChallenge {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String preAuthToken;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = true, length = 100)
    private String otpHash;

    @Column(nullable = true)
    private Instant otpExpiresAt;

    @Column(nullable = false)
    private Instant preAuthExpiresAt;

    @Column(nullable = false)
    private int attempts = 0;

    @Column(nullable = true)
    private Instant verifiedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}


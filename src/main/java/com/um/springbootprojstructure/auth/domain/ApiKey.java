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
        name = "api_keys",
        indexes = {
            @Index(name = "idx_api_keys_user_id", columnList = "user_id"),
            @Index(name = "idx_api_keys_prefix", columnList = "keyPrefix")
        }
)
public class ApiKey {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 12)
    private String keyPrefix;

    @Column(nullable = false, length = 64)
    private String keyHashSha256;

    @Column(nullable = true, length = 100)
    private String name;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = true)
    private Instant expiresAt;

    @Column(nullable = true)
    private Instant revokedAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}


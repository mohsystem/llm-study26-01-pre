package com.um.springbootprojstructure.auth.repository;

import com.um.springbootprojstructure.auth.domain.PasswordResetToken;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    @Query("""
            select prt from PasswordResetToken prt
            join fetch prt.user u
            where prt.token = :token
              and prt.usedAt is null
              and prt.expiresAt > :now
            """)
    Optional<PasswordResetToken> findValidByToken(@Param("token") String token, @Param("now") Instant now);

    @Modifying
    @Query("""
            update PasswordResetToken prt
               set prt.usedAt = :now
             where prt.user.id = :userId
               and prt.usedAt is null
            """)
    int invalidateAllForUser(@Param("userId") UUID userId, @Param("now") Instant now);
}


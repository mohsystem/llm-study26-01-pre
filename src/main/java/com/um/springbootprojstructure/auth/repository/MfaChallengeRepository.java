package com.um.springbootprojstructure.auth.repository;

import com.um.springbootprojstructure.auth.domain.MfaChallenge;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MfaChallengeRepository extends JpaRepository<MfaChallenge, UUID> {
    @Query("""
            select c from MfaChallenge c
            join fetch c.user u
            where c.preAuthToken = :token
              and c.verifiedAt is null
              and c.preAuthExpiresAt > :now
            """)
    Optional<MfaChallenge> findPendingByPreAuthToken(@Param("token") String token, @Param("now") Instant now);

    @Query("""
            select c from MfaChallenge c
            join fetch c.user u
            where c.preAuthToken = :token
              and c.verifiedAt is null
              and c.preAuthExpiresAt > :now
              and c.otpExpiresAt is not null
              and c.otpExpiresAt > :now
            """)
    Optional<MfaChallenge> findPendingWithValidOtp(@Param("token") String token, @Param("now") Instant now);
}


package com.um.springbootprojstructure.auth.repository;

import com.um.springbootprojstructure.auth.domain.SessionToken;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessionTokenRepository extends JpaRepository<SessionToken, UUID> {
    @Query("""
            select st from SessionToken st
            join fetch st.user u
            where st.token = :token
              and st.revoked = false
              and st.expiresAt > :now
            """)
    Optional<SessionToken> findValidByToken(@Param("token") String token, @Param("now") Instant now);

    @Modifying
    @Query("""
            update SessionToken st
               set st.revoked = true
             where st.user.id = :userId
            """)
    int revokeAllForUser(@Param("userId") UUID userId);
}


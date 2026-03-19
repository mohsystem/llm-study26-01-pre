package com.um.springbootprojstructure.auth.repository;

import com.um.springbootprojstructure.auth.domain.ApiKey;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
    @Query("""
            select k from ApiKey k
            where k.user.id = :userId
            order by k.createdAt desc
            """)
    List<ApiKey> findAllByUserId(@Param("userId") UUID userId);

    @Query("""
            select k from ApiKey k
            where k.id = :keyId and k.user.id = :userId
            """)
    Optional<ApiKey> findByIdAndUserId(@Param("keyId") UUID keyId, @Param("userId") UUID userId);
}


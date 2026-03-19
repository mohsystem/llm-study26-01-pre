package com.um.springbootprojstructure.user.repository;

import com.um.springbootprojstructure.user.domain.UserIdentityDocument;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserIdentityDocumentRepository extends JpaRepository<UserIdentityDocument, UUID> {
    @Query("""
            select d from UserIdentityDocument d
            join fetch d.user u
            where u.id = :userId
            """)
    Optional<UserIdentityDocument> findByUserId(@Param("userId") UUID userId);
}


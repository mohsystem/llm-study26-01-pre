package com.um.springbootprojstructure.user.service;

import com.um.springbootprojstructure.auth.web.AuthenticatedUser;
import com.um.springbootprojstructure.common.exception.BadRequestException;
import com.um.springbootprojstructure.common.exception.NotFoundException;
import com.um.springbootprojstructure.common.exception.UnauthorizedException;
import com.um.springbootprojstructure.common.exception.ForbiddenException;
import com.um.springbootprojstructure.user.domain.UserIdentityDocument;
import com.um.springbootprojstructure.user.domain.UserRole;
import com.um.springbootprojstructure.user.repository.UserIdentityDocumentRepository;
import com.um.springbootprojstructure.user.repository.UserRepository;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class UserDocumentServiceImpl implements UserDocumentService {
    private static final long MAX_DOCUMENT_BYTES = 10L * 1024L * 1024L; // 10MB

    private final UserRepository userRepository;
    private final UserIdentityDocumentRepository documentRepository;

    @Override
    @Transactional(readOnly = true)
    public UserIdentityDocument getDocument(UUID userId, AuthenticatedUser principal) {
        var user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user not found"));
        ensureAuthorized(user.getId(), principal);
        return documentRepository.findByUserId(user.getId()).orElseThrow(() -> new NotFoundException("document not found"));
    }

    @Override
    public void updateDocument(UUID userId, MultipartFile file, AuthenticatedUser principal) {
        var user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user not found"));
        ensureAuthorized(user.getId(), principal);

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("file is required");
        }
        if (file.getSize() > MAX_DOCUMENT_BYTES) {
            throw new BadRequestException("file too large (max 10MB)");
        }

        var filename = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "document";
        var contentType = StringUtils.hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream";

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("failed to read uploaded file");
        }

        var doc = documentRepository.findByUserId(user.getId()).orElseGet(UserIdentityDocument::new);
        doc.setUser(user);
        doc.setFilename(filename);
        doc.setContentType(contentType);
        doc.setContent(bytes);

        documentRepository.save(doc);
    }

    private static void ensureAuthorized(UUID targetUserId, AuthenticatedUser principal) {
        if (principal == null) {
            throw new UnauthorizedException("missing session token");
        }
        if (principal.userId().equals(targetUserId)) return;
        if (principal.role() == UserRole.ADMIN) return;
        throw new ForbiddenException("not allowed");
    }
}


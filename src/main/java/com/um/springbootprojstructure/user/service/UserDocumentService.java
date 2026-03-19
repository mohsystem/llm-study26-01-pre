package com.um.springbootprojstructure.user.service;

import com.um.springbootprojstructure.auth.web.AuthenticatedUser;
import com.um.springbootprojstructure.user.domain.UserIdentityDocument;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface UserDocumentService {
    UserIdentityDocument getDocument(UUID userId, AuthenticatedUser principal);

    void updateDocument(UUID userId, MultipartFile file, AuthenticatedUser principal);
}


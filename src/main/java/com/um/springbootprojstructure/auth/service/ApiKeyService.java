package com.um.springbootprojstructure.auth.service;

import com.um.springbootprojstructure.auth.dto.ApiKeyItemResponse;
import com.um.springbootprojstructure.auth.dto.CreateApiKeyRequest;
import com.um.springbootprojstructure.auth.dto.CreateApiKeyResponse;
import com.um.springbootprojstructure.auth.dto.StatusResponse;
import java.util.List;
import java.util.UUID;

public interface ApiKeyService {
    CreateApiKeyResponse issue(UUID authenticatedUserId, CreateApiKeyRequest request);

    List<ApiKeyItemResponse> list(UUID authenticatedUserId);

    StatusResponse revoke(UUID authenticatedUserId, UUID keyId);
}


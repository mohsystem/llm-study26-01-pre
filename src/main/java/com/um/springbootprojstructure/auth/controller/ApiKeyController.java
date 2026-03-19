package com.um.springbootprojstructure.auth.controller;

import com.um.springbootprojstructure.auth.dto.ApiKeyItemResponse;
import com.um.springbootprojstructure.auth.dto.CreateApiKeyRequest;
import com.um.springbootprojstructure.auth.dto.CreateApiKeyResponse;
import com.um.springbootprojstructure.auth.dto.StatusResponse;
import com.um.springbootprojstructure.auth.service.ApiKeyService;
import com.um.springbootprojstructure.auth.web.AuthRequestAttributes;
import com.um.springbootprojstructure.auth.web.AuthenticatedUser;
import com.um.springbootprojstructure.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {
    private final ApiKeyService apiKeyService;

    @PostMapping
    public ResponseEntity<CreateApiKeyResponse> issue(@Valid @RequestBody CreateApiKeyRequest request, HttpServletRequest http) {
        var principal = requirePrincipal(http);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiKeyService.issue(principal.userId(), request));
    }

    @GetMapping
    public List<ApiKeyItemResponse> list(HttpServletRequest http) {
        var principal = requirePrincipal(http);
        return apiKeyService.list(principal.userId());
    }

    @DeleteMapping("/{keyId}")
    public StatusResponse revoke(@PathVariable UUID keyId, HttpServletRequest http) {
        var principal = requirePrincipal(http);
        return apiKeyService.revoke(principal.userId(), keyId);
    }

    private static AuthenticatedUser requirePrincipal(HttpServletRequest http) {
        var principal = (AuthenticatedUser) http.getAttribute(AuthRequestAttributes.AUTHENTICATED_USER);
        if (principal == null) {
            throw new UnauthorizedException("missing session token");
        }
        return principal;
    }
}


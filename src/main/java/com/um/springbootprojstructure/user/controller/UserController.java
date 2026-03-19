package com.um.springbootprojstructure.user.controller;

import com.um.springbootprojstructure.auth.web.AuthRequestAttributes;
import com.um.springbootprojstructure.auth.web.AuthenticatedUser;
import com.um.springbootprojstructure.auth.dto.StatusResponse;
import com.um.springbootprojstructure.common.exception.BadRequestException;
import com.um.springbootprojstructure.common.exception.UnauthorizedException;
import com.um.springbootprojstructure.user.domain.UserRole;
import com.um.springbootprojstructure.user.dto.UserResponse;
import com.um.springbootprojstructure.user.service.UserDocumentService;
import com.um.springbootprojstructure.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserDocumentService userDocumentService;

    @GetMapping
    public Page<UserResponse> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) String status
    ) {
        if (page < 0) throw new BadRequestException("page must be >= 0");
        if (size < 1 || size > 200) throw new BadRequestException("size must be between 1 and 200");

        var active = parseStatus(status);
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return userService.list(pageable, role, active);
    }

    @GetMapping("/{publicRef}/document")
    public ResponseEntity<byte[]> getIdentityDocument(@PathVariable UUID publicRef, HttpServletRequest http) {
        var principal = requirePrincipal(http);
        var doc = userDocumentService.getDocument(publicRef, principal);

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(doc.getContentType());
        } catch (Exception ignored) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        var headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDisposition(ContentDisposition.attachment().filename(doc.getFilename()).build());
        headers.setCacheControl("no-store");

        return ResponseEntity.ok().headers(headers).body(doc.getContent());
    }

    @PutMapping(
            path = "/{publicRef}/document",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<StatusResponse> updateIdentityDocument(
            @PathVariable UUID publicRef,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest http
    ) {
        var principal = requirePrincipal(http);
        userDocumentService.updateDocument(publicRef, file, principal);
        return ResponseEntity.ok(new StatusResponse("DOCUMENT_UPDATED"));
    }

    private static AuthenticatedUser requirePrincipal(HttpServletRequest http) {
        var principal = (AuthenticatedUser) http.getAttribute(AuthRequestAttributes.AUTHENTICATED_USER);
        if (principal == null) {
            throw new UnauthorizedException("missing session token");
        }
        return principal;
    }

    private static Boolean parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        var normalized = status.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "active", "enabled", "true", "1", "yes" -> true;
            case "inactive", "disabled", "false", "0", "no" -> false;
            default -> throw new BadRequestException("invalid status; use active/inactive");
        };
    }
}


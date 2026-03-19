package com.um.springbootprojstructure.admin.directory.controller;

import com.um.springbootprojstructure.admin.directory.dto.DirectoryUserSearchResponse;
import com.um.springbootprojstructure.admin.directory.service.DirectoryLookupService;
import com.um.springbootprojstructure.auth.web.AuthRequestAttributes;
import com.um.springbootprojstructure.auth.web.AuthenticatedUser;
import com.um.springbootprojstructure.common.exception.ApiException;
import com.um.springbootprojstructure.common.exception.UnauthorizedException;
import com.um.springbootprojstructure.user.domain.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/directory")
@RequiredArgsConstructor
public class AdminDirectoryController {
    private final DirectoryLookupService directoryLookupService;

    @GetMapping("/user-search")
    public DirectoryUserSearchResponse userSearch(
            @RequestParam String dc,
            @RequestParam String username,
            HttpServletRequest http
    ) {
        var principal = (AuthenticatedUser) http.getAttribute(AuthRequestAttributes.AUTHENTICATED_USER);
        if (principal == null) {
            throw new UnauthorizedException("missing session token");
        }
        if (principal.role() != UserRole.ADMIN) {
            throw ApiException.forbidden("ADMIN_REQUIRED", "admin access required");
        }

        return directoryLookupService.searchUser(dc, username);
    }
}


package com.um.springbootprojstructure.auth.controller;

import com.um.springbootprojstructure.auth.dto.ChangePasswordRequest;
import com.um.springbootprojstructure.auth.dto.LoginRequest;
import com.um.springbootprojstructure.auth.dto.LoginResponse;
import com.um.springbootprojstructure.auth.dto.MfaChallengeRequest;
import com.um.springbootprojstructure.auth.dto.MfaChallengeResponse;
import com.um.springbootprojstructure.auth.dto.MfaVerifyRequest;
import com.um.springbootprojstructure.auth.dto.MfaVerifyResponse;
import com.um.springbootprojstructure.auth.dto.RegisterRequest;
import com.um.springbootprojstructure.auth.dto.RegisterResponse;
import com.um.springbootprojstructure.auth.dto.ResetConfirmRequest;
import com.um.springbootprojstructure.auth.dto.ResetRequestRequest;
import com.um.springbootprojstructure.auth.dto.ResetRequestResponse;
import com.um.springbootprojstructure.auth.dto.StatusResponse;
import com.um.springbootprojstructure.auth.service.AuthService;
import com.um.springbootprojstructure.auth.web.AuthRequestAttributes;
import com.um.springbootprojstructure.auth.web.AuthenticatedUser;
import com.um.springbootprojstructure.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/mfa/challenge")
    public MfaChallengeResponse mfaChallenge(@Valid @RequestBody MfaChallengeRequest request) {
        return authService.mfaChallenge(request);
    }

    @PostMapping("/mfa/verify")
    public MfaVerifyResponse mfaVerify(@Valid @RequestBody MfaVerifyRequest request) {
        return authService.mfaVerify(request);
    }

    @PostMapping("/change-password")
    public StatusResponse changePassword(@Valid @RequestBody ChangePasswordRequest request, HttpServletRequest http) {
        var principal = (AuthenticatedUser) http.getAttribute(AuthRequestAttributes.AUTHENTICATED_USER);
        if (principal == null) {
            throw new UnauthorizedException("missing session token");
        }
        return authService.changePassword(principal.userId(), request);
    }

    @PostMapping("/reset-request")
    public ResetRequestResponse resetRequest(@Valid @RequestBody ResetRequestRequest request) {
        return authService.resetRequest(request);
    }

    @PostMapping("/reset-confirm")
    public StatusResponse resetConfirm(@Valid @RequestBody ResetConfirmRequest request) {
        return authService.resetConfirm(request);
    }
}


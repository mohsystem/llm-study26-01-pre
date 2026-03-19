package com.um.springbootprojstructure.auth.service;

import com.um.springbootprojstructure.auth.domain.MfaChallenge;
import com.um.springbootprojstructure.auth.domain.SessionToken;
import com.um.springbootprojstructure.auth.domain.PasswordResetToken;
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
import com.um.springbootprojstructure.auth.gateway.NotificationGatewayClient;
import com.um.springbootprojstructure.auth.repository.MfaChallengeRepository;
import com.um.springbootprojstructure.auth.repository.PasswordResetTokenRepository;
import com.um.springbootprojstructure.auth.repository.SessionTokenRepository;
import com.um.springbootprojstructure.common.exception.ApiException;
import com.um.springbootprojstructure.user.domain.User;
import com.um.springbootprojstructure.user.domain.UserRole;
import com.um.springbootprojstructure.user.repository.UserRepository;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {
    private static final Duration SESSION_TTL = Duration.ofHours(24);
    private static final Duration RESET_TTL = Duration.ofMinutes(15);
    private static final Duration MFA_OTP_TTL = Duration.ofMinutes(5);
    private static final Duration MFA_PREAUTH_TTL = Duration.ofMinutes(10);
    private static final int MFA_MAX_ATTEMPTS = 5;
    private static final SecureRandom secureRandom = new SecureRandom();

    private final UserRepository userRepository;
    private final SessionTokenRepository sessionTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final MfaChallengeRepository mfaChallengeRepository;
    private final NotificationGatewayClient notificationGatewayClient;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public RegisterResponse register(RegisterRequest request) {
        var username = normalize(request.username());
        var email = normalizeEmail(request.email());

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw ApiException.conflict("DUPLICATE_USERNAME", "username already exists");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("DUPLICATE_EMAIL", "email already exists");
        }

        var user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setDisplayName(trimToNull(request.displayName()));
        user.setPhoneNumber(trimToNull(request.phoneNumber()));
        user.setMfaEnabled(Boolean.TRUE.equals(request.mfaEnabled()));
        user.setActive(true);
        user.setRole(UserRole.USER);
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        user = userRepository.save(user);
        return new RegisterResponse(user.getId(), "REGISTERED");
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        var login = normalize(request.login());
        var user = userRepository.findByUsernameIgnoreCase(login)
                .or(() -> userRepository.findByEmailIgnoreCase(normalizeEmail(login)))
                .orElseThrow(() -> ApiException.unauthorized("INVALID_CREDENTIALS", "invalid credentials"));

        if (!user.isActive()) {
            throw ApiException.forbidden("ACCOUNT_INACTIVE", "account is inactive");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized("INVALID_CREDENTIALS", "invalid credentials");
        }

        if (user.isMfaEnabled()) {
            var preAuthToken = generateToken();
            var now = Instant.now();
            var challenge = new MfaChallenge();
            challenge.setPreAuthToken(preAuthToken);
            challenge.setUser(user);
            challenge.setPreAuthExpiresAt(now.plus(MFA_PREAUTH_TTL));
            mfaChallengeRepository.save(challenge);
            return new LoginResponse("MFA_REQUIRED", null, null, preAuthToken);
        }

        var session = createSession(user);
        return new LoginResponse("AUTHENTICATED", session.token, session.expiresAt, null);
    }

    @Override
    public MfaChallengeResponse mfaChallenge(MfaChallengeRequest request) {
        var now = Instant.now();
        var challenge = mfaChallengeRepository
                .findPendingByPreAuthToken(request.preAuthToken().trim(), now)
                .orElseThrow(() -> ApiException.unauthorized("PREAUTH_TOKEN_INVALID", "invalid preAuthToken"));

        var user = challenge.getUser();
        if (!user.isActive()) {
            throw ApiException.forbidden("ACCOUNT_INACTIVE", "account is inactive");
        }
        if (!user.isMfaEnabled()) {
            throw ApiException.badRequest("MFA_NOT_ENABLED", "mfa is not enabled for this account");
        }
        if (user.getPhoneNumber() == null || user.getPhoneNumber().isBlank()) {
            throw ApiException.badRequest("MFA_PHONE_REQUIRED", "phoneNumber is required for mfa");
        }

        var otp = generateOtp();
        challenge.setOtpHash(passwordEncoder.encode(otp));
        challenge.setOtpExpiresAt(now.plus(MFA_OTP_TTL));
        challenge.setAttempts(0);
        mfaChallengeRepository.save(challenge);

        notificationGatewayClient.sendOtp(user.getPhoneNumber(), "Your one-time passcode is: " + otp);

        return new MfaChallengeResponse("OTP_SENT");
    }

    @Override
    public MfaVerifyResponse mfaVerify(MfaVerifyRequest request) {
        var now = Instant.now();
        var challenge = mfaChallengeRepository
                .findPendingWithValidOtp(request.preAuthToken().trim(), now)
                .orElseThrow(() -> ApiException.unauthorized("PREAUTH_TOKEN_INVALID", "invalid or expired preAuthToken"));

        if (challenge.getAttempts() >= MFA_MAX_ATTEMPTS) {
            throw ApiException.forbidden("MFA_TOO_MANY_ATTEMPTS", "too many attempts");
        }

        var ok = challenge.getOtpHash() != null && passwordEncoder.matches(request.passcode(), challenge.getOtpHash());
        if (!ok) {
            challenge.setAttempts(challenge.getAttempts() + 1);
            mfaChallengeRepository.save(challenge);
            throw ApiException.unauthorized("MFA_PASSCODE_INVALID", "invalid passcode");
        }

        challenge.setVerifiedAt(now);
        mfaChallengeRepository.save(challenge);

        var session = createSession(challenge.getUser());
        return new MfaVerifyResponse("AUTHENTICATED", session.token, session.expiresAt);
    }

    private SessionInfo createSession(User user) {
        var now = Instant.now();
        var tokenValue = generateToken();

        var session = new SessionToken();
        session.setToken(tokenValue);
        session.setUser(user);
        session.setExpiresAt(now.plus(SESSION_TTL));
        session.setRevoked(false);
        sessionTokenRepository.save(session);
        return new SessionInfo(tokenValue, session.getExpiresAt());
    }

    @Override
    public StatusResponse changePassword(UUID authenticatedUserId, ChangePasswordRequest request) {
        var user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "user not found"));

        if (!user.isActive()) {
            throw ApiException.forbidden("ACCOUNT_INACTIVE", "account is inactive");
        }
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw ApiException.unauthorized("CURRENT_PASSWORD_INVALID", "invalid current password");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw ApiException.conflict("NEW_PASSWORD_REUSE", "new password must be different");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        sessionTokenRepository.revokeAllForUser(user.getId());

        return new StatusResponse("PASSWORD_CHANGED");
    }

    @Override
    public ResetRequestResponse resetRequest(ResetRequestRequest request) {
        var login = normalize(request.login());

        var userOpt = userRepository.findByUsernameIgnoreCase(login)
                .or(() -> userRepository.findByEmailIgnoreCase(normalizeEmail(login)));

        // Always return a generic success status to avoid account enumeration.
        if (userOpt.isEmpty()) {
            return new ResetRequestResponse("RESET_REQUESTED", null);
        }

        var user = userOpt.get();
        var now = Instant.now();

        passwordResetTokenRepository.invalidateAllForUser(user.getId(), now);

        var tokenValue = generateToken();
        var prt = new PasswordResetToken();
        prt.setToken(tokenValue);
        prt.setUser(user);
        prt.setExpiresAt(now.plus(RESET_TTL));
        passwordResetTokenRepository.save(prt);

        // For development/testing we return the token (in a real system you'd email it).
        return new ResetRequestResponse("RESET_REQUESTED", tokenValue);
    }

    @Override
    public StatusResponse resetConfirm(ResetConfirmRequest request) {
        var now = Instant.now();
        var prt = passwordResetTokenRepository
                .findValidByToken(request.resetToken().trim(), now)
                .orElseThrow(() -> ApiException.unauthorized("RESET_TOKEN_INVALID", "invalid or expired reset token"));

        var user = prt.getUser();
        if (!user.isActive()) {
            throw ApiException.forbidden("ACCOUNT_INACTIVE", "account is inactive");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        prt.setUsedAt(now);
        passwordResetTokenRepository.save(prt);

        sessionTokenRepository.revokeAllForUser(user.getId());

        return new StatusResponse("PASSWORD_RESET");
    }

    private static String generateToken() {
        // UUID is sufficient for a simple session token in this module.
        return UUID.randomUUID().toString().replace("-", "");
    }

    private static String generateOtp() {
        // 6-digit numeric code
        int code = secureRandom.nextInt(1_000_000);
        return String.format("%06d", code);
    }

    private record SessionInfo(String token, Instant expiresAt) {}

    private static String normalize(String value) {
        if (value == null) return null;
        return value.trim();
    }

    private static String normalizeEmail(String value) {
        var trimmed = normalize(value);
        return trimmed == null ? null : trimmed.toLowerCase();
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        var trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}


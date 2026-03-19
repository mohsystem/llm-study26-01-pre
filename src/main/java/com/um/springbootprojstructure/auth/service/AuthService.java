package com.um.springbootprojstructure.auth.service;

import com.um.springbootprojstructure.auth.dto.LoginRequest;
import com.um.springbootprojstructure.auth.dto.LoginResponse;
import com.um.springbootprojstructure.auth.dto.ChangePasswordRequest;
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
import java.util.UUID;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    MfaChallengeResponse mfaChallenge(MfaChallengeRequest request);

    MfaVerifyResponse mfaVerify(MfaVerifyRequest request);

    StatusResponse changePassword(UUID authenticatedUserId, ChangePasswordRequest request);

    ResetRequestResponse resetRequest(ResetRequestRequest request);

    StatusResponse resetConfirm(ResetConfirmRequest request);
}


package com.um.springbootprojstructure.user.service;

import com.um.springbootprojstructure.common.exception.ApiException;
import com.um.springbootprojstructure.common.exception.NotFoundException;
import com.um.springbootprojstructure.user.domain.User;
import com.um.springbootprojstructure.user.domain.UserRole;
import com.um.springbootprojstructure.user.dto.CreateUserRequest;
import com.um.springbootprojstructure.user.dto.UpdateUserRequest;
import com.um.springbootprojstructure.user.dto.UserResponse;
import com.um.springbootprojstructure.user.repository.UserRepository;
import com.um.springbootprojstructure.user.repository.UserSpecifications;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserResponse create(CreateUserRequest request) {
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
        user.setActive(true);
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        return toResponse(
                userRepository.findById(id).orElseThrow(() -> new NotFoundException("user not found"))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getByUsername(String username) {
        return toResponse(
                userRepository
                        .findByUsernameIgnoreCase(normalize(username))
                        .orElseThrow(() -> new NotFoundException("user not found"))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return userRepository.findAll().stream().map(UserServiceImpl::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> list(Pageable pageable, UserRole role, Boolean active) {
        var spec = UserSpecifications.hasRole(role);
        spec = spec == null ? UserSpecifications.isActive(active) : spec.and(UserSpecifications.isActive(active));

        return userRepository.findAll(spec, pageable).map(UserServiceImpl::toResponse);
    }

    @Override
    public UserResponse update(UUID id, UpdateUserRequest request) {
        var user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("user not found"));

        if (request.username() != null) {
            var username = normalize(request.username());
            if (!username.equalsIgnoreCase(user.getUsername()) && userRepository.existsByUsernameIgnoreCase(username)) {
                throw ApiException.conflict("DUPLICATE_USERNAME", "username already exists");
            }
            user.setUsername(username);
        }

        if (request.email() != null) {
            var email = normalizeEmail(request.email());
            if (!email.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmailIgnoreCase(email)) {
                throw ApiException.conflict("DUPLICATE_EMAIL", "email already exists");
            }
            user.setEmail(email);
        }

        if (request.displayName() != null) {
            user.setDisplayName(trimToNull(request.displayName()));
        }

        if (request.active() != null) {
            user.setActive(request.active());
        }

        return toResponse(userRepository.save(user));
    }

    @Override
    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("user not found");
        }
        userRepository.deleteById(id);
    }

    private static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole() == null ? null : user.getRole().name(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

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


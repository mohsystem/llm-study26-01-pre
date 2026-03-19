package com.um.springbootprojstructure.user.service;

import com.um.springbootprojstructure.user.dto.CreateUserRequest;
import com.um.springbootprojstructure.user.dto.UpdateUserRequest;
import com.um.springbootprojstructure.user.dto.UserResponse;
import com.um.springbootprojstructure.user.domain.UserRole;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse create(CreateUserRequest request);

    UserResponse getById(UUID id);

    UserResponse getByUsername(String username);

    List<UserResponse> list();

    Page<UserResponse> list(Pageable pageable, UserRole role, Boolean active);

    UserResponse update(UUID id, UpdateUserRequest request);

    void delete(UUID id);
}


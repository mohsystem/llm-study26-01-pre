package com.um.springbootprojstructure.admin.directory.dto;

import java.util.List;

public record DirectoryUserSearchResponse(
        String status,
        List<DirectoryUserResponse> users
) {}


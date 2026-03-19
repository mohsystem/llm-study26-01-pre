package com.um.springbootprojstructure.admin.directory.dto;

public record DirectoryUserResponse(
        String dn,
        String username,
        String displayName,
        String email,
        String phoneNumber
) {}


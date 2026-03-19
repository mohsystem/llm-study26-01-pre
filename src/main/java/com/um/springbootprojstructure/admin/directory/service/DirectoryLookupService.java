package com.um.springbootprojstructure.admin.directory.service;

import com.um.springbootprojstructure.admin.directory.dto.DirectoryUserSearchResponse;

public interface DirectoryLookupService {
    DirectoryUserSearchResponse searchUser(String dc, String username);
}


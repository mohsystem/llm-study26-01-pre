package com.um.springbootprojstructure.admin.directory.service;

import com.um.springbootprojstructure.admin.directory.dto.DirectoryUserResponse;
import com.um.springbootprojstructure.admin.directory.dto.DirectoryUserSearchResponse;
import com.um.springbootprojstructure.common.exception.ApiException;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import javax.naming.directory.SearchControls;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectoryLookupServiceImpl implements DirectoryLookupService {
    private final ObjectProvider<LdapTemplate> ldapTemplateProvider;

    @Override
    public DirectoryUserSearchResponse searchUser(String dc, String username) {
        var ldapTemplate = ldapTemplateProvider.getIfAvailable();
        if (ldapTemplate == null) {
            throw ApiException.badRequest("DIRECTORY_NOT_CONFIGURED", "ldap is not configured");
        }

        var baseDn = toBaseDn(dc);
        var user = normalize(username);
        if (!StringUtils.hasText(user)) {
            throw ApiException.badRequest("USERNAME_REQUIRED", "username is required");
        }

        var encoded = LdapEncoder.filterEncode(user);
        var filter = "(&(objectClass=person)(|(uid=" + encoded + ")(sAMAccountName=" + encoded + ")(cn=" + encoded + ")))";

        var sc = new SearchControls();
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
        sc.setCountLimit(10);
        sc.setTimeLimit(3_000);

        List<DirectoryUserResponse> users = ldapTemplate.search(baseDn, filter, sc, mapper());

        return new DirectoryUserSearchResponse(users.isEmpty() ? "NOT_FOUND" : "FOUND", users);
    }

    private static AttributesMapper<DirectoryUserResponse> mapper() {
        return attrs -> new DirectoryUserResponse(
                stringAttr(attrs, "distinguishedName"),
                firstNonBlank(stringAttr(attrs, "uid"), stringAttr(attrs, "sAMAccountName"), stringAttr(attrs, "cn")),
                firstNonBlank(stringAttr(attrs, "displayName"), stringAttr(attrs, "cn")),
                firstNonBlank(stringAttr(attrs, "mail"), stringAttr(attrs, "email")),
                firstNonBlank(stringAttr(attrs, "telephoneNumber"), stringAttr(attrs, "mobile"))
        );
    }

    private static String stringAttr(javax.naming.directory.Attributes attrs, String name) {
        try {
            var a = attrs.get(name);
            if (a == null) return null;
            var v = a.get();
            return v == null ? null : v.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (StringUtils.hasText(v)) return v;
        }
        return null;
    }

    private static String toBaseDn(String dc) {
        var raw = normalize(dc);
        if (!StringUtils.hasText(raw)) {
            throw ApiException.badRequest("DC_REQUIRED", "dc is required");
        }

        var normalized = raw.trim();

        // Accept "example.com" or "dc=example,dc=com" or "example,com"
        if (normalized.toLowerCase(Locale.ROOT).contains("dc=")) {
            return normalized;
        }

        var parts = normalized.contains(".") ? normalized.split("\\.") : normalized.split(",");
        var sb = new StringBuilder();
        for (String p : parts) {
            var part = normalize(p);
            if (!StringUtils.hasText(part)) continue;
            if (!sb.isEmpty()) sb.append(",");
            sb.append("dc=").append(part);
        }

        if (sb.isEmpty()) {
            throw ApiException.badRequest("DC_INVALID", "dc is invalid");
        }

        return sb.toString();
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim();
    }
}


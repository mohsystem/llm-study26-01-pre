package com.um.springbootprojstructure.admin.directory.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration
@ConditionalOnProperty(prefix = "ldap", name = "enabled", havingValue = "true")
public class LdapDirectoryConfig {

    @Bean
    public LdapContextSource ldapContextSource(
            @Value("${ldap.url}") String url,
            @Value("${ldap.bind-dn}") String bindDn,
            @Value("${ldap.bind-password}") String bindPassword
    ) {
        var contextSource = new LdapContextSource();
        contextSource.setUrl(url);
        contextSource.setUserDn(bindDn);
        contextSource.setPassword(bindPassword);
        contextSource.setPooled(false);
        contextSource.afterPropertiesSet();
        return contextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate(LdapContextSource contextSource) {
        return new LdapTemplate(contextSource);
    }
}


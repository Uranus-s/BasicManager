package com.basic.core.security.service;

import com.basic.core.security.config.DevMasterTokenProperties;
import com.basic.core.security.model.LoginUser;
import com.basic.core.security.spi.SecurityUserQueryService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class DevMasterTokenService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final DevMasterTokenProperties properties;
    private final ObjectProvider<SecurityUserQueryService> securityUserQueryServiceProvider;

    public DevMasterTokenService(DevMasterTokenProperties properties,
                                 Environment environment,
                                 ObjectProvider<SecurityUserQueryService> securityUserQueryServiceProvider) {
        this.properties = properties;
        this.securityUserQueryServiceProvider = securityUserQueryServiceProvider;
        validate(environment);
    }

    public Optional<Authentication> authenticate(String authorizationHeader) {
        if (!properties.isEnabled() || !StringUtils.hasText(authorizationHeader)
                || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (!properties.getToken().equals(token)) {
            return Optional.empty();
        }

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(properties.getUserId());
        loginUser.setUsername(properties.getUsername());
        loginUser.setEnabled(true);
        loginUser.setRoles(List.of("admin", "dev-master"));
        loginUser.setPermissions(securityUserQueryServiceProvider.getObject().listAllPermissionCodes());

        return Optional.of(new UsernamePasswordAuthenticationToken(
                loginUser,
                null,
                loginUser.getAuthorities()));
    }

    private void validate(Environment environment) {
        if (!properties.isEnabled()) {
            return;
        }
        if (!StringUtils.hasText(properties.getToken())) {
            throw new IllegalStateException("Dev master token is enabled but token is empty.");
        }
        if (Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
            throw new IllegalStateException("Dev master token must not be enabled in prod profile.");
        }
    }
}

package com.basic.core.security.filter;

import com.basic.common.result.ResultEnum;
import com.basic.core.security.model.LoginUser;
import com.basic.core.security.service.AuthTokenService;
import com.basic.core.security.service.DevMasterTokenService;
import com.basic.core.security.service.SecurityUserDetailsService;
import com.basic.core.security.util.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesOnlyWhenTokenMatchesRedisTokenForUserId() throws Exception {
        SecurityUserDetailsService userDetailsService = mock(SecurityUserDetailsService.class);
        AuthTokenService authTokenService = mock(AuthTokenService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(userDetailsService, authTokenService, disabledDevMasterTokenService());

        String token = JwtUtil.generateToken(10L, "old-admin", List.of("研发部"));
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(10L);
        loginUser.setUsername("admin");
        loginUser.setPermissions(List.of("system:user:list"));

        when(authTokenService.isCurrentLoginToken(10L, token)).thenReturn(true);
        when(userDetailsService.loadUserByUserId(10L)).thenReturn(loginUser);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/info");
        request.addHeader("Authorization", "Bearer " + token);

        filter.doFilterInternal(request, new MockHttpServletResponse(), mock(FilterChain.class));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isSameAs(loginUser);
        assertThat(loginUser.getDeptNames()).containsExactly("研发部");
        verify(userDetailsService).loadUserByUserId(10L);
    }

    @Test
    void ignoresJwtWhenRedisTokenDoesNotMatch() throws Exception {
        SecurityUserDetailsService userDetailsService = mock(SecurityUserDetailsService.class);
        AuthTokenService authTokenService = mock(AuthTokenService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(userDetailsService, authTokenService, disabledDevMasterTokenService());

        String token = JwtUtil.generateToken(10L, "admin");
        when(authTokenService.isCurrentLoginToken(10L, token)).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/info");
        request.addHeader("Authorization", "Bearer " + token);

        filter.doFilterInternal(request, new MockHttpServletResponse(), mock(FilterChain.class));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(request.getAttribute(JwtAuthenticationFilter.AUTH_ERROR_ATTRIBUTE)).isEqualTo(ResultEnum.TOKEN_INVALID);
        verify(userDetailsService, never()).loadUserByUserId(10L);
    }

    @Test
    void marksJwtAsExpiredWhenTokenIsExpired() throws Exception {
        SecurityUserDetailsService userDetailsService = mock(SecurityUserDetailsService.class);
        AuthTokenService authTokenService = mock(AuthTokenService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(userDetailsService, authTokenService, disabledDevMasterTokenService());
        String token = expiredToken();

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/info");
        request.addHeader("Authorization", "Bearer " + token);

        filter.doFilterInternal(request, new MockHttpServletResponse(), mock(FilterChain.class));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(request.getAttribute(JwtAuthenticationFilter.AUTH_ERROR_ATTRIBUTE)).isEqualTo(ResultEnum.TOKEN_EXPIRED);
        verify(userDetailsService, never()).loadUserByUserId(10L);
    }

    @Test
    void usesDevMasterTokenBeforeJwtValidation() throws Exception {
        SecurityUserDetailsService userDetailsService = mock(SecurityUserDetailsService.class);
        AuthTokenService authTokenService = mock(AuthTokenService.class);
        DevMasterTokenService devMasterTokenService = mock(DevMasterTokenService.class);
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(0L);
        loginUser.setUsername("dev-master");
        loginUser.setPermissions(List.of("auth:online:list"));
        Authentication devAuthentication = new UsernamePasswordAuthenticationToken(
                loginUser,
                null,
                loginUser.getAuthorities());
        when(devMasterTokenService.authenticate("Bearer dev-token")).thenReturn(Optional.of(devAuthentication));
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(userDetailsService, authTokenService, devMasterTokenService);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/online");
        request.addHeader("Authorization", "Bearer dev-token");

        filter.doFilterInternal(request, new MockHttpServletResponse(), mock(FilterChain.class));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(devAuthentication);
        verify(authTokenService, never()).isCurrentLoginToken(0L, "dev-token");
        verify(userDetailsService, never()).loadUserByUserId(0L);
    }

    private String expiredToken() {
        byte[] secret = "abc123!@#abc123!@#abc123!@#abc123!@#".getBytes(StandardCharsets.UTF_8);
        Date now = new Date();
        return Jwts.builder()
                .subject("admin")
                .claim("uid", 10L)
                .issuedAt(new Date(now.getTime() - 2_000L))
                .expiration(new Date(now.getTime() - 1_000L))
                .signWith(Keys.hmacShaKeyFor(secret), Jwts.SIG.HS256)
                .compact();
    }

    private DevMasterTokenService disabledDevMasterTokenService() {
        DevMasterTokenService devMasterTokenService = mock(DevMasterTokenService.class);
        when(devMasterTokenService.authenticate(org.mockito.ArgumentMatchers.any())).thenReturn(Optional.empty());
        return devMasterTokenService;
    }
}

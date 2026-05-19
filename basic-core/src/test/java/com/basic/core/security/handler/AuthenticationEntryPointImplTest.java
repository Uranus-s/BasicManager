package com.basic.core.security.handler;

import com.basic.common.result.ResultEnum;
import com.basic.core.security.filter.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AuthenticationEntryPointImplTest {

    @Test
    void returnsTokenExpiredCodeWhenRequestCarriesExpiredTokenError() throws Exception {
        AuthenticationEntryPointImpl entryPoint = new AuthenticationEntryPointImpl();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setAttribute(JwtAuthenticationFilter.AUTH_ERROR_ATTRIBUTE, ResultEnum.TOKEN_EXPIRED);

        entryPoint.commence(request, response, mock(AuthenticationException.class));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("\"code\":20004");
    }

    @Test
    void returnsUnauthorizedCodeWhenNoTokenErrorExists() throws Exception {
        AuthenticationEntryPointImpl entryPoint = new AuthenticationEntryPointImpl();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, mock(AuthenticationException.class));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("\"code\":20001");
    }
}

package com.basic.sericve.auth.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRequestUtilsTest {

    @Test
    void maskIpMasksMiddleSegmentsForIpv4() {
        assertThat(LoginRequestUtils.maskIp("192.168.1.25")).isEqualTo("192.168.*.25");
    }

    @Test
    void maskIpMasksIpv6Address() {
        assertThat(LoginRequestUtils.maskIp("2001:db8:85a3::8a2e:370:7334")).isEqualTo("2001:*:7334");
    }

    @Test
    void resolveMaskedIpUsesFirstForwardedForValueFirst() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "10.0.0.8, 172.16.0.9");
        request.addHeader("X-Real-IP", "192.168.1.25");
        request.setRemoteAddr("127.0.0.1");

        assertThat(LoginRequestUtils.resolveMaskedIp(request)).isEqualTo("10.0.*.8");
    }

    @Test
    void resolveMaskedIpFallsBackToRealIp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Real-IP", "192.168.1.25");
        request.setRemoteAddr("127.0.0.1");

        assertThat(LoginRequestUtils.resolveMaskedIp(request)).isEqualTo("192.168.*.25");
    }

    @Test
    void resolveMaskedIpFallsBackToRemoteAddr() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("172.16.5.20");

        assertThat(LoginRequestUtils.resolveMaskedIp(request)).isEqualTo("172.16.*.20");
    }

    @Test
    void resolvesChromeBrowserAndWindowsOs() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                        + "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
        );

        assertThat(LoginRequestUtils.resolveBrowser(request)).isEqualTo("Chrome");
        assertThat(LoginRequestUtils.resolveOs(request)).isEqualTo("Windows");
    }

    @Test
    void resolvesEdgeBeforeChrome() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                        + "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36 Edg/124.0.0.0"
        );

        assertThat(LoginRequestUtils.resolveBrowser(request)).isEqualTo("Edge");
    }
}

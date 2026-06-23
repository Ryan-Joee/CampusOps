package com.campusops.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret("test-secret-key-for-jwt-testing-must-be-long-enough-for-hmacsha");
        props.setExpirationSeconds(3600);
        provider = new JwtTokenProvider(props);
    }

    @Test
    void generateAndValidate_validToken_shouldPass() {
        String token = provider.generateToken(5L, "student001", List.of("normal_user"));

        assertThat(provider.validateToken(token)).isTrue();
        assertThat(provider.getUserId(token)).isEqualTo(5L);
        assertThat(provider.getUsername(token)).isEqualTo("student001");
        assertThat(provider.getRoles(token)).containsExactly("normal_user");
    }

    @Test
    void validate_expiredToken_shouldFail() {
        JwtProperties expiredProps = new JwtProperties();
        expiredProps.setSecret("test-secret-key-for-jwt-testing-must-be-long-enough-for-hmacsha");
        expiredProps.setExpirationSeconds(-1);
        JwtTokenProvider expiredProvider = new JwtTokenProvider(expiredProps);
        String expiredToken = expiredProvider.generateToken(1L, "test", List.of("user"));

        assertThat(provider.validateToken(expiredToken)).isFalse();
    }

    @Test
    void validate_wrongSignature_shouldFail() {
        JwtProperties otherProps = new JwtProperties();
        otherProps.setSecret("other-secret-key-other-secret-key-other-secret-key-ok");
        otherProps.setExpirationSeconds(3600);
        JwtTokenProvider otherProvider = new JwtTokenProvider(otherProps);
        String token = otherProvider.generateToken(1L, "test", List.of("user"));

        assertThat(provider.validateToken(token)).isFalse();
    }

    @Test
    void validate_tamperedToken_shouldFail() {
        String token = provider.generateToken(5L, "student001", List.of("normal_user"));
        String tampered = token.substring(0, token.length() - 4) + "XXXX";

        assertThat(provider.validateToken(tampered)).isFalse();
    }
}

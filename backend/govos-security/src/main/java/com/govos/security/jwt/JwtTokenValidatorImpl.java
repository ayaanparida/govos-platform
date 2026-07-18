package com.govos.security.jwt;

import org.springframework.stereotype.Service;

@Service
public class JwtTokenValidatorImpl implements JwtTokenValidator {

    private final JwtTokenFactory jwtTokenFactory;

    public JwtTokenValidatorImpl(JwtTokenFactory jwtTokenFactory) {
        this.jwtTokenFactory = jwtTokenFactory;
    }

    @Override
    public boolean validateAccessToken(String token) {
        try {
            jwtTokenFactory.parseClaims(token);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }
}

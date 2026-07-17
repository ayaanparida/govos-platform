package com.govos.security.jwt;

public class JwtExpiredException extends JwtException {

    public JwtExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}

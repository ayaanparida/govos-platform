package com.govos.security.jwt;

public class JwtMalformedTokenException extends JwtException {

    public JwtMalformedTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}

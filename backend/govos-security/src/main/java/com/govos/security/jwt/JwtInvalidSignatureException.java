package com.govos.security.jwt;

public class JwtInvalidSignatureException extends JwtException {

    public JwtInvalidSignatureException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.govos.security.password;

/**
 * Password hashing and verification contract for GovOS authentication.
 */
public interface PasswordEncoderService {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}

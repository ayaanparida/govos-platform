package com.govos.security.password;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * BCrypt-backed {@link PasswordEncoderService} delegating to the platform {@link PasswordEncoder} bean.
 */
@Service
public class BCryptPasswordEncoderService implements PasswordEncoderService {

    private final PasswordEncoder passwordEncoder;

    public BCryptPasswordEncoderService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}

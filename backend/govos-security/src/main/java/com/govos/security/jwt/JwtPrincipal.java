package com.govos.security.jwt;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

/**
 * Authenticated principal reconstructed from JWT access token claims.
 */
public class JwtPrincipal implements UserDetails {

    private final UUID userId;
    private final String username;
    private final String sessionId;
    private final String tokenId;
    private final Collection<? extends GrantedAuthority> authorities;
    private final String rawToken;

    public JwtPrincipal(
            UUID userId,
            String username,
            String sessionId,
            String tokenId,
            Collection<? extends GrantedAuthority> authorities,
            String rawToken) {
        this.userId = userId;
        this.username = username;
        this.sessionId = sessionId;
        this.tokenId = tokenId;
        this.authorities = authorities;
        this.rawToken = rawToken;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getTokenId() {
        return tokenId;
    }

    public String getRawToken() {
        return rawToken;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

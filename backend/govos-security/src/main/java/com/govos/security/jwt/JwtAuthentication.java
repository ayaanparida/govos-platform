package com.govos.security.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Spring Security {@link org.springframework.security.core.Authentication} holder for JWT requests.
 */
public class JwtAuthentication extends AbstractAuthenticationToken {

    private final JwtPrincipal principal;
    private final String credentials;

    public JwtAuthentication(
            JwtPrincipal principal,
            String rawToken,
            Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = rawToken;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public JwtPrincipal getPrincipal() {
        return principal;
    }
}

package com.govos.security.jwt;

import com.govos.security.constant.SecurityConstants;
import com.govos.security.provider.GovosUserPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JwtTokenProviderImpl implements JwtTokenProvider {

    private final JwtTokenFactory jwtTokenFactory;

    public JwtTokenProviderImpl(JwtTokenFactory jwtTokenFactory) {
        this.jwtTokenFactory = jwtTokenFactory;
    }

    @Override
    public String createAccessToken(GovosUserPrincipal principal, String sessionId) {
        List<String> roles = new ArrayList<>();
        List<String> permissions = new ArrayList<>();

        for (GrantedAuthority authority : principal.getAuthorities()) {
            String value = authority.getAuthority();
            if (value.startsWith(SecurityConstants.ROLE_PREFIX)) {
                roles.add(value.substring(SecurityConstants.ROLE_PREFIX.length()));
            } else {
                permissions.add(value);
            }
        }

        return jwtTokenFactory.generateAccessToken(
                principal.getUserId(),
                principal.getUsername(),
                roles,
                permissions,
                sessionId);
    }
}

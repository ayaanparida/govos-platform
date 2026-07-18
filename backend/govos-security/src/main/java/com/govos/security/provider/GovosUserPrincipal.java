package com.govos.security.provider;

import com.govos.idm.dto.UserDto;
import com.govos.idm.entity.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

/**
 * Spring Security principal adapter for an IDM user.
 */
public class GovosUserPrincipal implements UserDetails {

    private final UUID userId;
    private final String username;
    private final String password;
    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean accountNonLocked;
    private final boolean enabled;

    public GovosUserPrincipal(
            UUID userId,
            String username,
            String password,
            String email,
            Collection<? extends GrantedAuthority> authorities,
            boolean accountNonLocked,
            boolean enabled) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.authorities = authorities;
        this.accountNonLocked = accountNonLocked;
        this.enabled = enabled;
    }

    public static GovosUserPrincipal from(
            UserDto user,
            Collection<? extends GrantedAuthority> authorities,
            String passwordHash) {
        boolean enabled = Boolean.TRUE.equals(user.active()) && user.status() == UserStatus.ACTIVE;
        boolean accountNonLocked = !Boolean.TRUE.equals(user.accountLocked());

        return new GovosUserPrincipal(
                user.id(),
                user.username(),
                passwordHash != null ? passwordHash : "",
                user.email(),
                authorities,
                accountNonLocked,
                enabled);
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
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
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}

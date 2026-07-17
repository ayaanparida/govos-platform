package com.govos.security.provider;

import com.govos.idm.dto.PermissionDto;
import com.govos.idm.dto.RoleDto;
import com.govos.idm.dto.RolePermissionDto;
import com.govos.idm.dto.UserDto;
import com.govos.idm.dto.UserRoleDto;
import com.govos.idm.exception.UserNotFoundException;
import com.govos.idm.service.PermissionService;
import com.govos.idm.service.RolePermissionService;
import com.govos.idm.service.RoleService;
import com.govos.idm.service.UserRoleService;
import com.govos.idm.service.UserService;
import com.govos.security.util.AuthorityUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Loads {@link GovosUserPrincipal} instances via IDM domain services — no direct repository access.
 */
@Service
public class GovosUserDetailsService implements UserDetailsService {

    private final UserService userService;
    private final UserRoleService userRoleService;
    private final RoleService roleService;
    private final RolePermissionService rolePermissionService;
    private final PermissionService permissionService;

    public GovosUserDetailsService(
            UserService userService,
            UserRoleService userRoleService,
            RoleService roleService,
            RolePermissionService rolePermissionService,
            PermissionService permissionService) {
        this.userService = userService;
        this.userRoleService = userRoleService;
        this.roleService = roleService;
        this.rolePermissionService = rolePermissionService;
        this.permissionService = permissionService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            UserDto user = userService.getByUsername(username);
            Set<GrantedAuthority> authorities = resolveAuthorities(user.id());
            return GovosUserPrincipal.from(user, authorities, null);
        } catch (UserNotFoundException ex) {
            throw new UsernameNotFoundException(ex.getMessage(), ex);
        }
    }

    private Set<GrantedAuthority> resolveAuthorities(UUID userId) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        Instant now = Instant.now();

        for (UserRoleDto userRole : userRoleService.getByUserId(userId)) {
            if (!isActiveAssignment(userRole.active(), userRole.expiryDate(), now)) {
                continue;
            }

            RoleDto role = roleService.getById(userRole.roleId());
            if (!Boolean.TRUE.equals(role.active())) {
                continue;
            }

            authorities.add(new SimpleGrantedAuthority(AuthorityUtils.toRoleAuthority(role)));

            List<RolePermissionDto> rolePermissions = rolePermissionService.getByRoleId(role.id());
            for (RolePermissionDto rolePermission : rolePermissions) {
                if (!Boolean.TRUE.equals(rolePermission.active())) {
                    continue;
                }

                PermissionDto permission = permissionService.getById(rolePermission.permissionId());
                if (!Boolean.TRUE.equals(permission.active())) {
                    continue;
                }

                authorities.add(new SimpleGrantedAuthority(AuthorityUtils.toPermissionAuthority(permission)));
            }
        }

        return authorities;
    }

    private boolean isActiveAssignment(Boolean active, Instant expiryDate, Instant now) {
        if (!Boolean.TRUE.equals(active)) {
            return false;
        }
        return expiryDate == null || expiryDate.isAfter(now);
    }
}

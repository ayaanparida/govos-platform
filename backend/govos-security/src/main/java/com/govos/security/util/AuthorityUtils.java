package com.govos.security.util;

import com.govos.idm.dto.PermissionDto;
import com.govos.idm.dto.RoleDto;
import com.govos.security.constant.SecurityConstants;

/**
 * Helpers for Spring Security authority construction.
 */
public final class AuthorityUtils {

    private AuthorityUtils() {
    }

    public static String toRoleAuthority(RoleDto role) {
        return SecurityConstants.ROLE_PREFIX + role.code();
    }

    public static String toPermissionAuthority(PermissionDto permission) {
        if (permission.code() != null && !permission.code().isBlank()) {
            return permission.code();
        }
        return permission.module() + ":" + permission.resource() + ":" + permission.action();
    }
}

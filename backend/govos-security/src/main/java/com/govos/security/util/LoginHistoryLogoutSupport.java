package com.govos.security.util;

import com.govos.idm.dto.CreateLoginHistoryRequest;
import com.govos.idm.dto.LoginHistoryDto;
import com.govos.idm.service.LoginHistoryService;

import java.time.Instant;
import java.util.UUID;

/**
 * Closes open IDM login history sessions by recording a completion entry with logout time.
 * <p>
 * IDM {@link LoginHistoryService} does not yet expose an update API; this records session
 * closure metadata using the existing {@code record} operation.
 */
public final class LoginHistoryLogoutSupport {

    private LoginHistoryLogoutSupport() {
    }

    public static void closeOpenSession(
            LoginHistoryService loginHistoryService,
            UUID userId,
            Instant logoutTime,
            String ipAddress,
            String device,
            String browser) {
        loginHistoryService.getByUserId(userId).stream()
                .filter(entry -> Boolean.TRUE.equals(entry.success()))
                .filter(entry -> entry.logoutTime() == null)
                .findFirst()
                .ifPresent(open -> recordClosure(
                        loginHistoryService,
                        userId,
                        open,
                        logoutTime,
                        ipAddress,
                        device,
                        browser));
    }

    private static void recordClosure(
            LoginHistoryService loginHistoryService,
            UUID userId,
            LoginHistoryDto open,
            Instant logoutTime,
            String ipAddress,
            String device,
            String browser) {
        loginHistoryService.record(new CreateLoginHistoryRequest(
                userId,
                open.loginTime(),
                logoutTime,
                ipAddress != null ? ipAddress : open.ipAddress(),
                device != null ? device : open.device(),
                browser != null ? browser : open.browser(),
                true,
                true));
    }
}

package com.govos.idm.exception;

public class RefreshTokenNotFoundException extends IdmException {

    public RefreshTokenNotFoundException(String token) {
        super("Refresh token not found or revoked");
    }
}

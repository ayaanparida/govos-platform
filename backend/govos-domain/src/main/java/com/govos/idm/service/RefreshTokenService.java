package com.govos.idm.service;

import com.govos.idm.dto.CreateRefreshTokenRequest;
import com.govos.idm.dto.RefreshTokenDto;

import java.util.List;
import java.util.UUID;

public interface RefreshTokenService {

    RefreshTokenDto getByToken(String token);

    List<RefreshTokenDto> getActiveByUserId(UUID userId);

    RefreshTokenDto create(CreateRefreshTokenRequest request);

    void revoke(UUID id);

    void revokeByToken(String token);
}

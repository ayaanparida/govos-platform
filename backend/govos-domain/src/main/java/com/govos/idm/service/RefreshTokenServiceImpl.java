package com.govos.idm.service;

import com.govos.idm.dto.CreateRefreshTokenRequest;
import com.govos.idm.dto.RefreshTokenDto;
import com.govos.idm.entity.RefreshToken;
import com.govos.idm.entity.User;
import com.govos.idm.exception.RefreshTokenNotFoundException;
import com.govos.idm.exception.UserNotFoundException;
import com.govos.idm.mapper.RefreshTokenMapper;
import com.govos.idm.repository.RefreshTokenRepository;
import com.govos.idm.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final RefreshTokenMapper refreshTokenMapper;

    public RefreshTokenServiceImpl(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            RefreshTokenMapper refreshTokenMapper) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.refreshTokenMapper = refreshTokenMapper;
    }

    @Override
    public RefreshTokenDto getByToken(String token) {
        return refreshTokenMapper.toDto(
                refreshTokenRepository.findByTokenAndDeletedFalseAndRevokedFalse(token)
                        .orElseThrow(() -> new RefreshTokenNotFoundException(token)));
    }

    @Override
    public List<RefreshTokenDto> getActiveByUserId(UUID userId) {
        return refreshTokenRepository.findByUser_IdAndDeletedFalseAndRevokedFalse(userId).stream()
                .map(refreshTokenMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public RefreshTokenDto create(CreateRefreshTokenRequest request) {
        User user = userRepository.findByIdAndDeletedFalse(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));

        RefreshToken entity = refreshTokenMapper.toEntity(request);
        entity.setUser(user);
        entity.setRevoked(request.revoked() != null ? request.revoked() : false);
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return refreshTokenMapper.toDto(refreshTokenRepository.save(entity));
    }

    @Override
    @Transactional
    public void revoke(UUID id) {
        RefreshToken entity = refreshTokenRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RefreshTokenNotFoundException(id.toString()));
        entity.setRevoked(true);
        entity.setActive(false);
        refreshTokenRepository.save(entity);
    }

    @Override
    @Transactional
    public void revokeByToken(String token) {
        RefreshToken entity = refreshTokenRepository.findByTokenAndDeletedFalseAndRevokedFalse(token)
                .orElseThrow(() -> new RefreshTokenNotFoundException(token));
        entity.setRevoked(true);
        entity.setActive(false);
        refreshTokenRepository.save(entity);
    }
}

package com.govos.idm.service;

import com.govos.idm.dto.CreateLoginHistoryRequest;
import com.govos.idm.dto.LoginHistoryDto;
import com.govos.idm.entity.LoginHistory;
import com.govos.idm.entity.User;
import com.govos.idm.exception.AssignmentNotFoundException;
import com.govos.idm.exception.UserNotFoundException;
import com.govos.idm.mapper.LoginHistoryMapper;
import com.govos.idm.repository.LoginHistoryRepository;
import com.govos.idm.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class LoginHistoryServiceImpl implements LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;
    private final UserRepository userRepository;
    private final LoginHistoryMapper loginHistoryMapper;

    public LoginHistoryServiceImpl(
            LoginHistoryRepository loginHistoryRepository,
            UserRepository userRepository,
            LoginHistoryMapper loginHistoryMapper) {
        this.loginHistoryRepository = loginHistoryRepository;
        this.userRepository = userRepository;
        this.loginHistoryMapper = loginHistoryMapper;
    }

    @Override
    public LoginHistoryDto getById(UUID id) {
        return loginHistoryMapper.toDto(findActiveById(id));
    }

    @Override
    public List<LoginHistoryDto> getByUserId(UUID userId) {
        return loginHistoryRepository.findByUser_IdOrderByLoginTimeDesc(userId).stream()
                .map(loginHistoryMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public LoginHistoryDto record(CreateLoginHistoryRequest request) {
        User user = userRepository.findByIdAndDeletedFalse(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));

        LoginHistory entity = loginHistoryMapper.toEntity(request);
        entity.setUser(user);
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return loginHistoryMapper.toDto(loginHistoryRepository.save(entity));
    }

    private LoginHistory findActiveById(UUID id) {
        return loginHistoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AssignmentNotFoundException("LoginHistory", id));
    }
}

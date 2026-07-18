package com.govos.idm.service;

import com.govos.idm.dto.CreatePasswordHistoryRequest;
import com.govos.idm.dto.PasswordHistoryDto;
import com.govos.idm.entity.PasswordHistory;
import com.govos.idm.entity.User;
import com.govos.idm.exception.AssignmentNotFoundException;
import com.govos.idm.exception.UserNotFoundException;
import com.govos.idm.mapper.PasswordHistoryMapper;
import com.govos.idm.repository.PasswordHistoryRepository;
import com.govos.idm.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PasswordHistoryServiceImpl implements PasswordHistoryService {

    private final PasswordHistoryRepository passwordHistoryRepository;
    private final UserRepository userRepository;
    private final PasswordHistoryMapper passwordHistoryMapper;

    public PasswordHistoryServiceImpl(
            PasswordHistoryRepository passwordHistoryRepository,
            UserRepository userRepository,
            PasswordHistoryMapper passwordHistoryMapper) {
        this.passwordHistoryRepository = passwordHistoryRepository;
        this.userRepository = userRepository;
        this.passwordHistoryMapper = passwordHistoryMapper;
    }

    @Override
    public PasswordHistoryDto getById(UUID id) {
        return passwordHistoryMapper.toDto(findActiveById(id));
    }

    @Override
    public List<PasswordHistoryDto> getByUserId(UUID userId) {
        return passwordHistoryRepository.findByUser_IdOrderByChangedDateDesc(userId).stream()
                .map(passwordHistoryMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public PasswordHistoryDto record(CreatePasswordHistoryRequest request) {
        User user = userRepository.findByIdAndDeletedFalse(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));

        PasswordHistory entity = passwordHistoryMapper.toEntity(request);
        entity.setUser(user);
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return passwordHistoryMapper.toDto(passwordHistoryRepository.save(entity));
    }

    private PasswordHistory findActiveById(UUID id) {
        return passwordHistoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AssignmentNotFoundException("PasswordHistory", id));
    }
}

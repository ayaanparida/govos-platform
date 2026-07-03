package com.govos.idm.service;

import com.govos.idm.dto.CreatePasswordHistoryRequest;
import com.govos.idm.dto.PasswordHistoryDto;

import java.util.List;
import java.util.UUID;

public interface PasswordHistoryService {

    PasswordHistoryDto getById(UUID id);

    List<PasswordHistoryDto> getByUserId(UUID userId);

    PasswordHistoryDto record(CreatePasswordHistoryRequest request);
}

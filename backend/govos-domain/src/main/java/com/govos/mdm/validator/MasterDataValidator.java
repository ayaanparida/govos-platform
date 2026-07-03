package com.govos.mdm.validator;

import com.govos.mdm.dto.CreateMasterDataRequest;
import com.govos.mdm.dto.UpdateMasterDataRequest;
import com.govos.mdm.exception.DuplicateMasterDataException;
import com.govos.mdm.repository.MasterDataRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MasterDataValidator {

    private final MasterDataRepository masterDataRepository;

    public MasterDataValidator(MasterDataRepository masterDataRepository) {
        this.masterDataRepository = masterDataRepository;
    }

    public void validateCreate(CreateMasterDataRequest request) {
        if (masterDataRepository.existsByTypeAndKeyAndDeletedFalse(request.type(), request.key())) {
            throw new DuplicateMasterDataException(request.type(), request.key());
        }
    }

    public void validateUpdate(UUID id, UpdateMasterDataRequest request) {
        masterDataRepository.findByTypeAndKeyAndDeletedFalse(request.type(), request.key())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateMasterDataException(request.type(), request.key());
                });
    }
}

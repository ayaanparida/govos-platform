package com.govos.mdm.service;

import com.govos.mdm.dto.CreateMasterDataRequest;
import com.govos.mdm.dto.MasterDataDto;
import com.govos.mdm.dto.UpdateMasterDataRequest;
import com.govos.mdm.entity.MasterData;
import com.govos.mdm.exception.MasterDataNotFoundException;
import com.govos.mdm.exception.SystemDefinedMasterDataException;
import com.govos.mdm.mapper.MasterDataMapper;
import com.govos.mdm.repository.MasterDataRepository;
import com.govos.mdm.validator.MasterDataValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class MasterDataServiceImpl implements MasterDataService {

    private final MasterDataRepository masterDataRepository;
    private final MasterDataMapper masterDataMapper;
    private final MasterDataValidator masterDataValidator;

    public MasterDataServiceImpl(
            MasterDataRepository masterDataRepository,
            MasterDataMapper masterDataMapper,
            MasterDataValidator masterDataValidator) {
        this.masterDataRepository = masterDataRepository;
        this.masterDataMapper = masterDataMapper;
        this.masterDataValidator = masterDataValidator;
    }

    @Override
    public MasterDataDto getById(UUID id) {
        return masterDataMapper.toDto(findActiveById(id));
    }

    @Override
    public MasterDataDto getByTypeAndKey(String type, String key) {
        return masterDataMapper.toDto(findActiveByTypeAndKey(type, key));
    }

    @Override
    public List<MasterDataDto> getByType(String type) {
        return masterDataRepository.findByTypeAndDeletedFalseOrderByDisplayOrderAsc(type).stream()
                .map(masterDataMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public MasterDataDto create(CreateMasterDataRequest request) {
        masterDataValidator.validateCreate(request);

        MasterData entity = masterDataMapper.toEntity(request);
        applyDefaults(entity, request.active(), request.systemDefined());

        return masterDataMapper.toDto(masterDataRepository.save(entity));
    }

    @Override
    @Transactional
    public MasterDataDto update(UUID id, UpdateMasterDataRequest request) {
        MasterData entity = findActiveById(id);
        assertNotSystemDefined(entity, "updated");

        if (request.version() != null && !request.version().equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "Master data version mismatch for id: " + id);
        }

        masterDataValidator.validateUpdate(id, request);
        masterDataMapper.updateEntity(request, entity);
        applyDefaults(entity, request.active(), request.systemDefined());

        return masterDataMapper.toDto(masterDataRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        MasterData entity = findActiveById(id);
        assertNotSystemDefined(entity, "deleted");

        entity.setDeleted(true);
        entity.setActive(false);
        masterDataRepository.save(entity);
    }

    private MasterData findActiveById(UUID id) {
        return masterDataRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new MasterDataNotFoundException(id));
    }

    private MasterData findActiveByTypeAndKey(String type, String key) {
        return masterDataRepository.findByTypeAndKeyAndDeletedFalse(type, key)
                .orElseThrow(() -> new MasterDataNotFoundException(type, key));
    }

    private void applyDefaults(MasterData entity, Boolean active, Boolean systemDefined) {
        if (active != null) {
            entity.setActive(active);
        } else if (entity.getActive() == null) {
            entity.setActive(true);
        }
        if (systemDefined != null) {
            entity.setSystemDefined(systemDefined);
        } else if (entity.getSystemDefined() == null) {
            entity.setSystemDefined(false);
        }
        if (entity.getDeleted() == null) {
            entity.setDeleted(false);
        }
    }

    private void assertNotSystemDefined(MasterData entity, String operation) {
        if (Boolean.TRUE.equals(entity.getSystemDefined())) {
            throw new SystemDefinedMasterDataException(operation);
        }
    }
}

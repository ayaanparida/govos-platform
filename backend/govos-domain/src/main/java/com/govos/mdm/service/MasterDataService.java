package com.govos.mdm.service;

import com.govos.mdm.dto.CreateMasterDataRequest;
import com.govos.mdm.dto.MasterDataDto;
import com.govos.mdm.dto.UpdateMasterDataRequest;

import java.util.List;
import java.util.UUID;

public interface MasterDataService {

    MasterDataDto getById(UUID id);

    MasterDataDto getByTypeAndKey(String type, String key);

    List<MasterDataDto> getByType(String type);

    MasterDataDto create(CreateMasterDataRequest request);

    MasterDataDto update(UUID id, UpdateMasterDataRequest request);

    void softDelete(UUID id);
}

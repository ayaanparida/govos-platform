package com.govos.org.validator;

import com.govos.mdm.repository.MasterDataRepository;
import com.govos.org.dto.CreateOrganizationRequest;
import com.govos.org.dto.UpdateOrganizationRequest;
import com.govos.org.exception.DuplicateCodeException;
import com.govos.org.exception.InvalidOrganizationTypeException;
import com.govos.org.mdm.OrgMasterDataTypes;
import com.govos.org.repository.OrganizationRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Component
public class OrganizationValidator {

    private final OrganizationRepository organizationRepository;
    private final MasterDataRepository masterDataRepository;

    public OrganizationValidator(
            OrganizationRepository organizationRepository,
            MasterDataRepository masterDataRepository) {
        this.organizationRepository = organizationRepository;
        this.masterDataRepository = masterDataRepository;
    }

    public void validateCreate(CreateOrganizationRequest request) {
        if (organizationRepository.existsByCodeAndDeletedFalse(request.code())) {
            throw new DuplicateCodeException("Organization", request.code());
        }
        validateOrganizationType(request.type());
    }

    public void validateUpdate(UUID id, UpdateOrganizationRequest request) {
        organizationRepository.findByCodeAndDeletedFalse(request.code())
                .filter(org -> !org.getId().equals(id))
                .ifPresent(org -> {
                    throw new DuplicateCodeException("Organization", request.code());
                });
        validateOrganizationType(request.type());
    }

    private void validateOrganizationType(String type) {
        if (!StringUtils.hasText(type)) {
            return;
        }
        masterDataRepository.findByTypeAndKeyAndDeletedFalse(OrgMasterDataTypes.ORGANIZATION_TYPE, type)
                .orElseThrow(() -> new InvalidOrganizationTypeException(type));
    }
}

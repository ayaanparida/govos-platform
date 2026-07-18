package com.govos.api.mdm.search;

import java.util.UUID;

/**
 * SRH-013 extension point for MDM product search synchronization.
 * Implementation deferred until master data records are indexed through {@code SearchApplicationService}.
 */
public interface MasterDataSearchIntegration {

    void onMasterDataCreated(String masterDataType, UUID recordId, UUID organizationId);

    void onMasterDataUpdated(String masterDataType, UUID recordId, UUID organizationId);

    void onMasterDataArchived(String masterDataType, UUID recordId, UUID organizationId);

    void onMasterDataSoftDeleted(String masterDataType, UUID recordId, UUID organizationId);
}

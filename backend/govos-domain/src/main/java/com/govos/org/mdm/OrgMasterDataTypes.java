package com.govos.org.mdm;

/**
 * Master Data type keys used by the Organization bounded context.
 * Values are maintained in {@code mdm_master_data} — not hard-coded enums.
 */
public final class OrgMasterDataTypes {

    private OrgMasterDataTypes() {
    }

    /**
     * Organization classification (e.g. STATE_GOVERNMENT, DISTRICT, MUNICIPALITY).
     */
    public static final String ORGANIZATION_TYPE = "ORGANIZATION_TYPE";
}

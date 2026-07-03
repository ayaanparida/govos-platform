package com.govos.mdm.exception;

import java.util.UUID;

public class MasterDataNotFoundException extends MdmException {

    public MasterDataNotFoundException(UUID id) {
        super("Master data not found with id: " + id);
    }

    public MasterDataNotFoundException(String type, String key) {
        super("Master data not found for type '%s' and key '%s'".formatted(type, key));
    }
}

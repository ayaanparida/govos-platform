package com.govos.mdm.exception;

public class DuplicateMasterDataException extends MdmException {

    public DuplicateMasterDataException(String type, String key) {
        super("Master data already exists for type '%s' and key '%s'".formatted(type, key));
    }
}

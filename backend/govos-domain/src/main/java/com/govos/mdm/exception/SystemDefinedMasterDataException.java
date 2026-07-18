package com.govos.mdm.exception;

public class SystemDefinedMasterDataException extends MdmException {

    public SystemDefinedMasterDataException(String operation) {
        super("System-defined master data cannot be " + operation);
    }
}

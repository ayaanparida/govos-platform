package com.govos.mdm.exception;

/**
 * Base runtime exception for the MDM bounded context.
 */
public class MdmException extends RuntimeException {

    public MdmException(String message) {
        super(message);
    }

    public MdmException(String message, Throwable cause) {
        super(message, cause);
    }
}

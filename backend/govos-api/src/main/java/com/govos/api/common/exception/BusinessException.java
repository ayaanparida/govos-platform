package com.govos.api.common.exception;

public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String message) {
        this(null, message);
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

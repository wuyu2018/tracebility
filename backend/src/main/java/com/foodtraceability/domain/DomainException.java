package com.foodtraceability.domain;

import com.foodtraceability.exception.BusinessException;

public class DomainException extends BusinessException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}

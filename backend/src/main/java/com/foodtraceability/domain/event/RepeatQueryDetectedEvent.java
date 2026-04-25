package com.foodtraceability.domain.event;

import com.foodtraceability.domain.DomainEvent;

public class RepeatQueryDetectedEvent extends DomainEvent {

    private final Long securityCodeId;
    private final String code;
    private final Integer scanCount;

    public RepeatQueryDetectedEvent(Long securityCodeId, String code, Integer scanCount) {
        this.securityCodeId = securityCodeId;
        this.code = code;
        this.scanCount = scanCount;
    }

    public Long getSecurityCodeId() {
        return securityCodeId;
    }

    public String getCode() {
        return code;
    }

    public Integer getScanCount() {
        return scanCount;
    }
}

package com.foodtraceability.domain.event;

import com.foodtraceability.domain.DomainEvent;

public class SecurityCodeActivatedEvent extends DomainEvent {

    private final Long securityCodeId;
    private final String code;

    public SecurityCodeActivatedEvent(Long securityCodeId, String code) {
        this.securityCodeId = securityCodeId;
        this.code = code;
    }

    public Long getSecurityCodeId() {
        return securityCodeId;
    }

    public String getCode() {
        return code;
    }
}

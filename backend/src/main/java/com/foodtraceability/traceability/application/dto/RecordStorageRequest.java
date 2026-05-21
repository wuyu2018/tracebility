package com.foodtraceability.traceability.application.dto;

import java.time.LocalDateTime;

public class RecordStorageRequest {
    private final Long batchId;
    private final LocalDateTime storageTime;
    private final Double quantity;
    private final String unit;
    private final String warehouseLocation;
    private final Long companyId;

    public RecordStorageRequest(Long batchId, LocalDateTime storageTime, Double quantity,
                                 String unit, String warehouseLocation, Long companyId) {
        this.batchId = batchId;
        this.storageTime = storageTime;
        this.quantity = quantity;
        this.unit = unit;
        this.warehouseLocation = warehouseLocation;
        this.companyId = companyId;
    }

    public Long batchId() { return batchId; }
    public LocalDateTime storageTime() { return storageTime; }
    public Double quantity() { return quantity; }
    public String unit() { return unit; }
    public String warehouseLocation() { return warehouseLocation; }
    public Long companyId() { return companyId; }
}

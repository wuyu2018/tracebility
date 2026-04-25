package com.foodtraceability.domain.valueobject;

import com.foodtraceability.entity.Storage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StorageInfo {

    private final LocalDateTime storageTime;
    private final LocalDateTime outboundTime;
    private final String warehouseLocation;

    public static StorageInfo from(Storage storage) {
        if (storage == null) {
            return null;
        }
        return new StorageInfo(
            storage.getStorageTime(),
            storage.getOutboundTime(),
            storage.getWarehouseLocation()
        );
    }
}

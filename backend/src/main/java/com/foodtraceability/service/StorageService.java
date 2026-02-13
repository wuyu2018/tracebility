package com.foodtraceability.service;

import com.foodtraceability.dto.InsertDataDTO.*;
import com.foodtraceability.entity.*;

public interface StorageService {
    Storage createStorage(StorageDTO dto);
}

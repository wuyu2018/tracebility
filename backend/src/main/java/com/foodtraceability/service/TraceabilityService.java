package com.foodtraceability.service;

import com.foodtraceability.dto.TraceInfoDTO;
import com.foodtraceability.dto.PurchaseInfoDTO;

import java.util.List;
import java.util.Optional;

public interface TraceabilityService {

    Optional<TraceInfoDTO> verifyAndGetTraceInfo(String antiFakeCode);

    Optional<PurchaseInfoDTO> getPurchaseInfo(String antiFakeCode);

    List<PurchaseInfoDTO> listAllProducts();
}

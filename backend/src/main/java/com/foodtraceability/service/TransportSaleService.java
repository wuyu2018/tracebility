package com.foodtraceability.service;

import com.foodtraceability.dto.InsertDataDTO.*;
import com.foodtraceability.entity.*;

public interface TransportSaleService {
    TransportSale createTransportSale(TransportSaleDTO dto);
    void deleteById(Long id);
}

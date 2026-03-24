package com.foodtraceability.service;

import com.foodtraceability.dto.InsertDataDTO.*;
import com.foodtraceability.entity.*;

public interface InspectionService {
    Inspection createInspection(InspectionDTO dto);
    void deleteById(Long id);
}
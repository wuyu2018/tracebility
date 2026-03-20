package com.foodtraceability.service;

public interface ProductCleanupService {
    void cleanupExpiredProducts();
    
    void updateQueryTime(String antiFakeCode);
}

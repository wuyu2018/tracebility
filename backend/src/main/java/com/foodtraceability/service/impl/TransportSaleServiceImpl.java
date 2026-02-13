package com.foodtraceability.service.impl;

import com.foodtraceability.dto.InsertDataDTO.TransportSaleDTO;
import com.foodtraceability.entity.TransportSale;
import com.foodtraceability.repository.TransportSaleRepository;
import com.foodtraceability.service.TransportSaleService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransportSaleServiceImpl implements TransportSaleService {
    @Autowired
    private TransportSaleRepository repository;

    @Override
    @Transactional
    public TransportSale createTransportSale(TransportSaleDTO dto) {
        TransportSale entity = new TransportSale();
        BeanUtils.copyProperties(dto, entity);
        return repository.save(entity);
    }
}
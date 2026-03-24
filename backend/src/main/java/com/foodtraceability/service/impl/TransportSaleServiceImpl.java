package com.foodtraceability.service.impl;

import com.foodtraceability.dto.InsertDataDTO.TransportSaleDTO;
import com.foodtraceability.entity.TransportSale;
import com.foodtraceability.repository.TransportSaleRepository;
import com.foodtraceability.repository.ProductRepository;
import com.foodtraceability.service.TransportSaleService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransportSaleServiceImpl implements TransportSaleService {
    @Autowired
    private TransportSaleRepository repository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional
    public TransportSale createTransportSale(TransportSaleDTO dto) {
        if (!productRepository.existsByAntiFakeCode(dto.getAntiFakeCode())) {
            throw new IllegalArgumentException("防伪码不存在，请先录入产品信息");
        }
        TransportSale entity = new TransportSale();
        BeanUtils.copyProperties(dto, entity);
        return repository.save(entity);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
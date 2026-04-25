package com.foodtraceability.service;

import com.foodtraceability.domain.DomainException;
import com.foodtraceability.dto.ProductDTO;
import com.foodtraceability.entity.Product;
import com.foodtraceability.repository.ProductRepository;
import com.foodtraceability.service.domain.ProductDeletionService;
import com.foodtraceability.service.impl.ProductApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductApplicationServiceTest {

    @Mock
    private ProductRepository repository;

    @Mock
    private ProductDeletionService deletionService;

    @InjectMocks
    private ProductApplicationService service;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("测试产品");
        testProduct.setSpecification("规格A");
        testProduct.setIsDeleted(false);
    }

    @Test
    void testCreateProduct() {
        ProductDTO dto = new ProductDTO();
        dto.setName("新产品");
        dto.setSpecification("规格B");

        when(repository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        Product result = service.createProduct(dto);

        assertNotNull(result);
        assertEquals("新产品", result.getName());
        verify(repository).save(any(Product.class));
    }

    @Test
    void testUpdateProduct() {
        ProductDTO dto = new ProductDTO();
        dto.setName("更新后的产品");

        when(repository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(repository.save(any(Product.class))).thenReturn(testProduct);

        Product result = service.updateProduct(1L, dto);

        assertNotNull(result);
        verify(repository).findById(1L);
        verify(repository).save(any(Product.class));
    }

    @Test
    void testUpdateProduct_NotFound() {
        ProductDTO dto = new ProductDTO();
        dto.setName("更新后的产品");

        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> {
            service.updateProduct(999L, dto);
        });
    }

    @Test
    void testDeleteProduct() {
        when(repository.findById(1L)).thenReturn(Optional.of(testProduct));
        doNothing().when(deletionService).deleteProduct(testProduct);

        service.deleteProduct(1L);

        verify(repository).findById(1L);
        verify(deletionService).deleteProduct(testProduct);
    }

    @Test
    void testDeleteProduct_NotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> {
            service.deleteProduct(999L);
        });
    }

    @Test
    void testClearQrCode() {
        testProduct.setAntiFakeCode("SC123");

        when(repository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(repository.save(any(Product.class))).thenReturn(testProduct);

        service.clearQrCode(1L);

        assertNull(testProduct.getAntiFakeCode());
        verify(repository).save(testProduct);
    }

    @Test
    void testListAllProducts() {
        when(repository.findByIsDeletedFalse()).thenReturn(List.of(testProduct));

        List<Product> result = service.listAllProducts();

        assertEquals(1, result.size());
        verify(repository).findByIsDeletedFalse();
    }

    @Test
    void testSearchProducts() {
        when(repository.findByNameContainingAndIsDeletedFalse("测试")).thenReturn(List.of(testProduct));

        List<Product> result = service.searchProducts("测试");

        assertEquals(1, result.size());
        verify(repository).findByNameContainingAndIsDeletedFalse("测试");
    }

    @Test
    void testSearchProducts_WithBlankKeyword() {
        when(repository.findByIsDeletedFalse()).thenReturn(List.of(testProduct));

        List<Product> result = service.searchProducts("   ");

        assertEquals(1, result.size());
        verify(repository).findByIsDeletedFalse();
    }

    @Test
    void testGetProductById() {
        when(repository.findById(1L)).thenReturn(Optional.of(testProduct));

        Product result = service.getProductById(1L);

        assertNotNull(result);
        assertEquals("测试产品", result.getName());
    }

    @Test
    void testGetProductByAntiFakeCode() {
        when(repository.findByAntiFakeCode("SC123")).thenReturn(Optional.of(testProduct));

        Optional<Product> result = service.getProductByAntiFakeCode("SC123");

        assertTrue(result.isPresent());
        assertEquals("测试产品", result.get().getName());
    }
}

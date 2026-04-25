package com.foodtraceability.entity;

import com.foodtraceability.domain.DomainEvent;
import com.foodtraceability.domain.DomainException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String specification;

    @Column(name = "shelf_life", length = 50)
    private String shelfLife;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "anti_fake_code", length = 100)
    private String antiFakeCode;

    @Column(name = "qr_code_url", length = 500)
    private String qrCodeUrl;

    @Transient
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.isDeleted);
    }

    public void softDelete() {
        this.isDeleted = true;
    }

    public void clearQrCode() {
        this.antiFakeCode = null;
        this.qrCodeUrl = null;
    }

    public void assignQrCode(String antiFakeCode, String qrCodeUrl) {
        this.antiFakeCode = antiFakeCode;
        this.qrCodeUrl = qrCodeUrl;
    }

    public boolean hasBatches() {
        return false;
    }

    public boolean hasAntiFakeCode() {
        return this.antiFakeCode != null && !this.antiFakeCode.isEmpty();
    }

    public boolean canBeDeleted() {
        return !this.isDeleted;
    }

    public List<DomainEvent> getDomainEvents() {
        return new ArrayList<>(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    protected void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public static Product createWithAntiFakeCode(
            String name,
            String specification,
            String shelfLife,
            String imageUrl,
            String contactPhone,
            String contactEmail) {
        Product product = new Product();
        product.name = name;
        product.specification = specification;
        product.shelfLife = shelfLife;
        product.imageUrl = imageUrl;
        product.contactPhone = contactPhone;
        product.contactEmail = contactEmail;
        product.isDeleted = false;
        return product;
    }
}

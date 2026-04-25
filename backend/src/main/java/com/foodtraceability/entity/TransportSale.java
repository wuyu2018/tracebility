package com.foodtraceability.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.foodtraceability.domain.DomainEvent;
import com.foodtraceability.domain.valueobject.TransportSaleInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transport_sale")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TransportSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProductionBatch batch;

    @Column(name = "environment_temperature")
    private Double environmentTemperature;

    @Column(name = "product_temperature")
    private Double productTemperature;

    @Column
    private LocalDateTime time;

    @Column(name = "transport_company", length = 100)
    private String transportCompany;

    @Column(name = "vehicle_number", length = 50)
    private String vehicleNumber;

    @Column(name = "sales_region", length = 255)
    private String salesRegion;

    @Column(name = "receiver_name", length = 100)
    private String receiverName;

    @Column(name = "receiver_contact", length = 50)
    private String receiverContact;

    @Transient
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public void associateBatch(ProductionBatch batch) {
        this.batch = batch;
    }

    public static TransportSale create(
            ProductionBatch batch,
            LocalDateTime time,
            String transportCompany,
            String vehicleNumber,
            String receiverName,
            String receiverContact,
            String salesRegion,
            Double environmentTemperature,
            Double productTemperature) {
        TransportSale transportSale = new TransportSale();
        transportSale.batch = batch;
        transportSale.time = time;
        transportSale.transportCompany = transportCompany;
        transportSale.vehicleNumber = vehicleNumber;
        transportSale.receiverName = receiverName;
        transportSale.receiverContact = receiverContact;
        transportSale.salesRegion = salesRegion;
        transportSale.environmentTemperature = environmentTemperature;
        transportSale.productTemperature = productTemperature;
        return transportSale;
    }

    public TransportSaleInfo toTransportSaleInfo(boolean forAdmin) {
        return TransportSaleInfo.from(this, forAdmin);
    }

    public boolean isValid() {
        return this.batch != null && this.time != null;
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
}

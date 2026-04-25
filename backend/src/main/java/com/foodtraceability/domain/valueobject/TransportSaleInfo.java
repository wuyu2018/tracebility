package com.foodtraceability.domain.valueobject;

import com.foodtraceability.entity.TransportSale;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TransportSaleInfo {

    private final LocalDateTime transportTime;
    private final String transportCompany;
    private final String vehicleNumber;
    private final String receiverName;
    private final String receiverContact;
    private final String salesRegion;

    public static TransportSaleInfo from(TransportSale transportSale, boolean forAdmin) {
        if (transportSale == null) {
            return null;
        }
        return new TransportSaleInfo(
            transportSale.getTime(),
            forAdmin ? transportSale.getTransportCompany() : null,
            forAdmin ? transportSale.getVehicleNumber() : null,
            forAdmin ? transportSale.getReceiverName() : null,
            forAdmin ? transportSale.getReceiverContact() : null,
            transportSale.getSalesRegion()
        );
    }
}

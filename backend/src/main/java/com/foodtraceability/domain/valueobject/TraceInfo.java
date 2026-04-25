package com.foodtraceability.domain.valueobject;

import com.foodtraceability.entity.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class TraceInfo {

    private final ProductInfo product;
    private final BatchInfo batch;
    private final List<MaterialInfo> materials;
    private final InspectionInfo inspection;
    private final StorageInfo storage;
    private final TransportSaleInfo transportSale;

    public static TraceInfo create(
            Product product,
            ProductionBatch batch,
            List<MaterialPurchase> materialPurchases,
            Inspection inspection,
            Storage storage,
            TransportSale transportSale,
            boolean forAdmin) {

        List<MaterialInfo> materialInfos = materialPurchases != null
            ? materialPurchases.stream()
                .map(MaterialInfo::from)
                .collect(Collectors.toList())
            : List.of();

        return new TraceInfo(
            ProductInfo.from(product),
            BatchInfo.from(batch),
            materialInfos,
            InspectionInfo.from(inspection),
            StorageInfo.from(storage),
            TransportSaleInfo.from(transportSale, forAdmin)
        );
    }
}

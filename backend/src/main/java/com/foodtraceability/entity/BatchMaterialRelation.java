package com.foodtraceability.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "batch_material_relation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BatchMaterialRelation {

    @EmbeddedId
    private BatchMaterialRelationId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("batchId")
    @JoinColumn(name = "batch_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProductionBatch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("materialPurchaseId")
    @JoinColumn(name = "material_purchase_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private MaterialPurchase materialPurchase;

    public static BatchMaterialRelation create(ProductionBatch batch, MaterialPurchase materialPurchase) {
        BatchMaterialRelation relation = new BatchMaterialRelation();
        relation.id = new BatchMaterialRelationId(batch.getId(), materialPurchase.getId());
        relation.batch = batch;
        relation.materialPurchase = materialPurchase;
        return relation;
    }
}

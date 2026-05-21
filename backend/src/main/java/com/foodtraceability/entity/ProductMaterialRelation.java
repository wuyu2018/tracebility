package com.foodtraceability.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "product_material_relation", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"product_id", "material_id", "company_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ProductMaterialRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Material material;

    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = false;

    @Column(name = "company_id")
    private Long companyId;

    public static ProductMaterialRelation create(Product product, Material material) {
        ProductMaterialRelation relation = new ProductMaterialRelation();
        relation.product = product;
        relation.material = material;
        relation.isHidden = false;
        return relation;
    }

    public boolean isHidden() {
        return Boolean.TRUE.equals(this.isHidden);
    }

    public void show() {
        this.isHidden = false;
    }

    public void hide() {
        this.isHidden = true;
    }
}

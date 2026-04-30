package com.foodtraceability.service;

import com.foodtraceability.dto.MaterialDTO;
import com.foodtraceability.entity.Material;

import java.util.List;

public interface MaterialService {
    Material createMaterial(MaterialDTO dto);

    Material updateMaterial(Long id, MaterialDTO dto);

    void deactivateMaterial(Long id);

    void activateMaterial(Long id);

    void deleteMaterial(Long id);

    Material getMaterialById(Long id);

    List<Material> listAllActiveMaterials();

    List<Material> listAllMaterials();
}

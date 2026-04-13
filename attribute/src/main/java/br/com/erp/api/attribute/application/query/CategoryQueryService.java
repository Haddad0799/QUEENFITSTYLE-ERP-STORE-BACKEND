package br.com.erp.api.attribute.application.query;

import br.com.erp.api.attribute.presentation.dto.category.response.CategoryDetailsDTO;
import br.com.erp.api.attribute.presentation.dto.category.response.CategoryTreeDTO;
import br.com.erp.api.attribute.presentation.dto.category.response.StoreCategoryDTO;

import java.util.List;
import java.util.Optional;

public interface CategoryQueryService {
    List<CategoryDetailsDTO> findAll();
    List<StoreCategoryDTO> findAllActive();
    List<CategoryTreeDTO> findAllActiveAsTree();
    CategoryDetailsDTO findById(Long id);
    boolean existsActiveById(Long id);
    boolean isSubcategory(Long id);
    Optional<CategoryDetailsDTO> findByName(String name);
}
